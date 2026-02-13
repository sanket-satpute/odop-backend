package com.odop.root.report.service;

import com.odop.root.models.*;
import com.odop.root.repository.*;
import com.odop.root.report.dto.*;
import com.odop.root.report.model.Report;
import com.odop.root.report.model.Report.*;
import com.odop.root.report.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Service for generating reports
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {
    
    private final ReportRepository reportRepository;
    private final ExcelExportService excelExportService;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final VendorRepository vendorRepository;
    private final PaymentRepository paymentRepository;
    
    private static final int REPORT_EXPIRY_DAYS = 7;
    
    // ==================== REPORT GENERATION ====================
    
    /**
     * Generate a report
     */
    public Report createReport(String userId, String userRole, ReportRequest request) {
        String reportName = request.getReportName();
        if (reportName == null || reportName.isEmpty()) {
            reportName = generateReportName(request.getReportTypeEnum());
        }
        
        Report report = Report.builder()
                .userId(userId)
                .userRole(userRole)
                .reportName(reportName)
                .reportType(request.getReportTypeEnum())
                .format(request.getFormatEnum())
                .startDate(request.getStartDateTime())
                .endDate(request.getEndDateTime())
                .filters(request.getFilters())
                .status(ReportStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(REPORT_EXPIRY_DAYS))
                .build();
        
        return reportRepository.save(report);
    }
    
    /**
     * Process report generation asynchronously
     */
    @Async
    public CompletableFuture<Report> generateReportAsync(String reportId, ReportRequest request) {
        Report report = reportRepository.findById(reportId).orElse(null);
        if (report == null) {
            return CompletableFuture.completedFuture(null);
        }
        
        long startTime = System.currentTimeMillis();
        
        try {
            report.setStatus(ReportStatus.GENERATING);
            reportRepository.save(report);
            
            byte[] reportData = generateReportData(report, request);
            
            // Save file (in production, save to cloud storage)
            String fileName = generateFileName(report);
            String fileUrl = saveReportFile(reportId, fileName, reportData);
            
            report.setFileName(fileName);
            report.setFileUrl(fileUrl);
            report.setFileSizeBytes(reportData.length);
            report.setStatus(ReportStatus.COMPLETED);
            report.setGeneratedAt(LocalDateTime.now());
            report.setGenerationTimeMs(System.currentTimeMillis() - startTime);
            
        } catch (Exception e) {
            log.error("Error generating report: {}", reportId, e);
            report.setStatus(ReportStatus.FAILED);
            report.setErrorMessage(e.getMessage());
        }
        
        reportRepository.save(report);
        return CompletableFuture.completedFuture(report);
    }
    
    /**
     * Generate report data based on type
     */
    private byte[] generateReportData(Report report, ReportRequest request) {
        return switch (report.getReportType()) {
            case SALES_SUMMARY -> generateSalesSummary(request);
            case SALES_BY_PRODUCT -> generateSalesByProduct(request);
            case SALES_BY_REGION -> generateSalesByRegion(request);
            case ORDER_HISTORY -> generateOrderHistory(request);
            case ORDER_STATUS -> generateOrderStatus(request);
            case PRODUCT_INVENTORY -> generateProductInventory(request);
            case LOW_STOCK_ALERT -> generateLowStockAlert(request);
            case TAX_REPORT -> generateTaxReport(request);
            case REVENUE_REPORT -> generateRevenueReport(request);
            case ODOP_DISTRICT_SALES -> generateOdopDistrictSales(request);
            case GI_TAG_PRODUCTS -> generateGiTagProductsReport(request);
            default -> generateGenericReport(report.getReportType(), request);
        };
    }
    
    // ==================== SALES REPORTS ====================
    
    private byte[] generateSalesSummary(ReportRequest request) {
        List<Order> orders = getOrdersInRange(request);
        
        // Calculate summary
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("Total Orders", orders.size());
        summary.put("Total Revenue", orders.stream().mapToDouble(Order::getTotalAmount).sum());
        summary.put("Average Order Value", orders.isEmpty() ? 0 : 
                orders.stream().mapToDouble(Order::getTotalAmount).average().orElse(0));
        summary.put("Completed Orders", orders.stream().filter(o -> "DELIVERED".equals(o.getOrderStatus())).count());
        summary.put("Pending Orders", orders.stream().filter(o -> "PENDING".equals(o.getOrderStatus())).count());
        summary.put("Cancelled Orders", orders.stream().filter(o -> "CANCELLED".equals(o.getOrderStatus())).count());
        
        // Daily breakdown
        Map<String, List<Order>> byDate = orders.stream()
                .filter(o -> o.getCreatedAt() != null)
                .collect(Collectors.groupingBy(o -> o.getCreatedAt().toLocalDate().toString()));
        
        List<List<Object>> rows = new ArrayList<>();
        byDate.forEach((date, dateOrders) -> {
            rows.add(List.of(
                    date,
                    dateOrders.size(),
                    dateOrders.stream().mapToDouble(Order::getTotalAmount).sum()
            ));
        });
        
        rows.sort((a, b) -> ((String) a.get(0)).compareTo((String) b.get(0)));
        
        List<String> headers = List.of("Date", "Orders", "Revenue");
        
        Map<String, ExcelExportService.SheetData> sheets = new LinkedHashMap<>();
        
        ExcelExportService.SheetData summarySheet = new ExcelExportService.SheetData();
        summarySheet.setTitle("Sales Summary Report");
        summarySheet.setSummary(summary);
        summarySheet.setHeaders(List.of("Metric", "Value"));
        summarySheet.setRows(summary.entrySet().stream()
                .map(e -> List.<Object>of(e.getKey(), e.getValue()))
                .toList());
        sheets.put("Summary", summarySheet);
        
        ExcelExportService.SheetData detailsSheet = new ExcelExportService.SheetData();
        detailsSheet.setHeaders(headers);
        detailsSheet.setRows(rows);
        sheets.put("Daily Breakdown", detailsSheet);
        
        return excelExportService.generateMultiSheetExcel(sheets);
    }
    
    private byte[] generateSalesByProduct(ReportRequest request) {
        List<Order> orders = getOrdersInRange(request);
        
        // Aggregate by product
        Map<String, ProductSales> productSalesMap = new HashMap<>();
        
        for (Order order : orders) {
            if (order.getOrderItems() != null) {
                for (OrderItem item : order.getOrderItems()) {
                    String productId = item.getProductId();
                    productSalesMap.computeIfAbsent(productId, k -> new ProductSales(productId, item.getProductName()));
                    ProductSales ps = productSalesMap.get(productId);
                    ps.quantity += item.getQuantity();
                    ps.revenue += item.getUnitPrice() * item.getQuantity();
                    ps.orders++;
                }
            }
        }
        
        List<String> headers = List.of("Product Name", "Product ID", "Orders", "Quantity Sold", "Revenue");
        List<List<Object>> rows = productSalesMap.values().stream()
                .sorted((a, b) -> Double.compare(b.revenue, a.revenue))
                .map(ps -> List.<Object>of(ps.productName, ps.productId, ps.orders, ps.quantity, ps.revenue))
                .toList();
        
        return excelExportService.generateExcel("Sales by Product", headers, rows);
    }
    
    private byte[] generateSalesByRegion(ReportRequest request) {
        List<Order> orders = getOrdersInRange(request);
        
        // Aggregate by state/district
        Map<String, RegionSales> regionSalesMap = new HashMap<>();
        
        for (Order order : orders) {
            String state = order.getShippingAddress() != null ? 
                    extractState(order.getShippingAddress()) : "Unknown";
            
            regionSalesMap.computeIfAbsent(state, k -> new RegionSales(state));
            RegionSales rs = regionSalesMap.get(state);
            rs.orders++;
            rs.revenue += order.getTotalAmount();
        }
        
        List<String> headers = List.of("State/Region", "Orders", "Revenue", "Average Order Value");
        List<List<Object>> rows = regionSalesMap.values().stream()
                .sorted((a, b) -> Double.compare(b.revenue, a.revenue))
                .map(rs -> List.<Object>of(rs.region, rs.orders, rs.revenue, 
                        rs.orders > 0 ? rs.revenue / rs.orders : 0))
                .toList();
        
        return excelExportService.generateExcel("Sales by Region", headers, rows);
    }
    
    // ==================== ORDER REPORTS ====================
    
    private byte[] generateOrderHistory(ReportRequest request) {
        List<Order> orders = getOrdersInRange(request);
        
        List<String> headers = List.of(
                "Order ID", "Order Date", "Customer", "Status", 
                "Items", "Subtotal", "Shipping", "Discount", "Total"
        );
        
        List<List<Object>> rows = orders.stream()
                .map(o -> {
                    double subtotal = calculateSubtotal(o);
                    return List.<Object>of(
                            o.getOrderId(),
                            o.getCreatedAt() != null ? o.getCreatedAt().toLocalDate().toString() : "",
                            o.getCustomerId(),
                            o.getOrderStatus() != null ? o.getOrderStatus() : "",
                            o.getOrderItems() != null ? o.getOrderItems().size() : 0,
                            subtotal,
                            o.getDeliveryCharges(),
                            o.getDiscountAmount(),
                            o.getTotalAmount()
                    );
                })
                .toList();
        
        return excelExportService.generateExcel("Order History", headers, rows);
    }
    
    private byte[] generateOrderStatus(ReportRequest request) {
        List<Order> orders = getOrdersInRange(request);
        
        Map<String, Long> statusCounts = orders.stream()
                .collect(Collectors.groupingBy(
                        o -> o.getOrderStatus() != null ? o.getOrderStatus() : "UNKNOWN",
                        Collectors.counting()
                ));
        
        List<String> headers = List.of("Status", "Order Count", "Percentage");
        int total = orders.size();
        
        List<List<Object>> rows = statusCounts.entrySet().stream()
                .map(e -> List.<Object>of(
                        e.getKey(),
                        e.getValue(),
                        total > 0 ? String.format("%.1f%%", (e.getValue() * 100.0) / total) : "0%"
                ))
                .toList();
        
        return excelExportService.generateExcel("Orders by Status", headers, rows);
    }
    
    // ==================== PRODUCT REPORTS ====================
    
    private byte[] generateProductInventory(ReportRequest request) {
        List<Products> products;
        if (request.getVendorId() != null) {
            products = productRepository.findByVendorId(request.getVendorId());
        } else {
            products = productRepository.findAll();
        }
        
        List<String> headers = List.of(
                "Product ID", "Product Name", "Category", "Price", 
                "Stock Quantity", "Stock Status", "Origin District", "Origin State"
        );
        
        List<List<Object>> rows = products.stream()
                .map(p -> List.<Object>of(
                        p.getProductId(),
                        p.getProductName(),
                        p.getCategoryId(),
                        p.getPrice(),
                        p.getProductQuantity(),
                        p.getStockStatus(),
                        p.getOriginDistrict() != null ? p.getOriginDistrict() : "",
                        p.getOriginState() != null ? p.getOriginState() : ""
                ))
                .toList();
        
        return excelExportService.generateExcel("Product Inventory", headers, rows);
    }
    
    private byte[] generateLowStockAlert(ReportRequest request) {
        List<Products> products;
        if (request.getVendorId() != null) {
            products = productRepository.findByVendorId(request.getVendorId());
        } else {
            products = productRepository.findAll();
        }
        
        // Filter low stock (less than 10 units)
        products = products.stream()
                .filter(p -> p.getProductQuantity() < 10)
                .sorted(Comparator.comparingLong(Products::getProductQuantity))
                .toList();
        
        List<String> headers = List.of(
                "Product ID", "Product Name", "Current Stock", "Stock Status", "Price"
        );
        
        List<List<Object>> rows = products.stream()
                .map(p -> List.<Object>of(
                        p.getProductId(),
                        p.getProductName(),
                        p.getProductQuantity(),
                        p.getStockStatus(),
                        p.getPrice()
                ))
                .toList();
        
        return excelExportService.generateExcel("Low Stock Alert", headers, rows);
    }
    
    // ==================== FINANCIAL REPORTS ====================
    
    private byte[] generateTaxReport(ReportRequest request) {
        List<Order> orders = getOrdersInRange(request);
        
        // Only completed orders
        orders = orders.stream()
                .filter(o -> "DELIVERED".equals(o.getOrderStatus()) || "COMPLETED".equals(o.getOrderStatus()))
                .toList();
        
        double gstRate = 0.18;
        double totalRevenue = orders.stream().mapToDouble(Order::getTotalAmount).sum();
        double estimatedTax = totalRevenue * gstRate / (1 + gstRate);
        
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("Report Period", request.getStartDate() + " to " + request.getEndDate());
        summary.put("Total Orders", orders.size());
        summary.put("Total Revenue (incl. tax)", totalRevenue);
        summary.put("Estimated Tax Collected (18% GST)", estimatedTax);
        summary.put("Average Tax per Order", orders.isEmpty() ? 0 : estimatedTax / orders.size());
        
        List<String> headers = List.of(
                "Order ID", "Order Date", "Order Total", "Estimated Tax (18%)", "Net Amount"
        );
        
        List<List<Object>> rows = orders.stream()
                .map(o -> {
                    double total = o.getTotalAmount();
                    double tax = total * gstRate / (1 + gstRate);
                    double net = total - tax;
                    return List.<Object>of(
                            o.getOrderId(),
                            o.getCreatedAt() != null ? o.getCreatedAt().toLocalDate().toString() : "",
                            total,
                            tax,
                            net
                    );
                })
                .toList();
        
        Map<String, ExcelExportService.SheetData> sheets = new LinkedHashMap<>();
        
        ExcelExportService.SheetData summarySheet = new ExcelExportService.SheetData();
        summarySheet.setTitle("Tax Report");
        summarySheet.setSummary(summary);
        summarySheet.setHeaders(List.of("Metric", "Value"));
        summarySheet.setRows(summary.entrySet().stream()
                .map(e -> List.<Object>of(e.getKey(), e.getValue()))
                .toList());
        sheets.put("Summary", summarySheet);
        
        ExcelExportService.SheetData detailsSheet = new ExcelExportService.SheetData();
        detailsSheet.setHeaders(headers);
        detailsSheet.setRows(rows);
        sheets.put("Order Details", detailsSheet);
        
        return excelExportService.generateMultiSheetExcel(sheets);
    }
    
    private byte[] generateRevenueReport(ReportRequest request) {
        List<Order> orders = getOrdersInRange(request);
        
        double totalRevenue = orders.stream().mapToDouble(Order::getTotalAmount).sum();
        double totalDiscount = orders.stream().mapToDouble(Order::getDiscountAmount).sum();
        double totalShipping = orders.stream().mapToDouble(Order::getDeliveryCharges).sum();
        double gstRate = 0.18;
        double estimatedTax = totalRevenue * gstRate / (1 + gstRate);
        double netRevenue = totalRevenue - estimatedTax;
        
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("Gross Revenue", totalRevenue);
        summary.put("Estimated Tax (18% GST)", estimatedTax);
        summary.put("Total Discounts Given", totalDiscount);
        summary.put("Shipping Collected", totalShipping);
        summary.put("Net Revenue", netRevenue);
        summary.put("Total Orders", orders.size());
        summary.put("Average Order Value", orders.isEmpty() ? 0 : totalRevenue / orders.size());
        
        List<String> headers = List.of("Metric", "Amount (₹)");
        List<List<Object>> rows = summary.entrySet().stream()
                .map(e -> List.<Object>of(e.getKey(), e.getValue()))
                .toList();
        
        return excelExportService.generateExcel("Revenue Report", headers, rows);
    }
    
    // ==================== ODOP SPECIFIC REPORTS ====================
    
    private byte[] generateOdopDistrictSales(ReportRequest request) {
        List<Order> orders = getOrdersInRange(request);
        List<Products> products = productRepository.findAll();
        
        // Map products by ID
        Map<String, Products> productMap = products.stream()
                .collect(Collectors.toMap(Products::getProductId, p -> p, (a, b) -> a));
        
        // Aggregate by origin district
        Map<String, DistrictSales> districtSalesMap = new HashMap<>();
        
        for (Order order : orders) {
            if (order.getOrderItems() != null) {
                for (OrderItem item : order.getOrderItems()) {
                    Products product = productMap.get(item.getProductId());
                    if (product != null && product.getOriginDistrict() != null) {
                        String key = product.getOriginState() + " - " + product.getOriginDistrict();
                        districtSalesMap.computeIfAbsent(key, k -> 
                                new DistrictSales(product.getOriginDistrict(), product.getOriginState()));
                        DistrictSales ds = districtSalesMap.get(key);
                        ds.orders++;
                        ds.quantity += item.getQuantity();
                        ds.revenue += item.getUnitPrice() * item.getQuantity();
                    }
                }
            }
        }
        
        List<String> headers = List.of("State", "District", "Orders", "Items Sold", "Revenue");
        List<List<Object>> rows = districtSalesMap.values().stream()
                .sorted((a, b) -> Double.compare(b.revenue, a.revenue))
                .map(ds -> List.<Object>of(ds.state, ds.district, ds.orders, ds.quantity, ds.revenue))
                .toList();
        
        return excelExportService.generateExcel("ODOP District Sales", headers, rows);
    }
    
    private byte[] generateGiTagProductsReport(ReportRequest request) {
        List<Products> giProducts = productRepository.findByGiTagCertifiedTrue();
        
        List<String> headers = List.of(
                "Product Name", "GI Tag Number", "Origin District", "Origin State",
                "Craft Type", "Made By", "Price", "Stock"
        );
        
        List<List<Object>> rows = giProducts.stream()
                .map(p -> List.<Object>of(
                        p.getProductName(),
                        p.getGiTagNumber() != null ? p.getGiTagNumber() : "",
                        p.getOriginDistrict() != null ? p.getOriginDistrict() : "",
                        p.getOriginState() != null ? p.getOriginState() : "",
                        p.getCraftType() != null ? p.getCraftType() : "",
                        p.getMadeBy() != null ? p.getMadeBy() : "",
                        p.getPrice(),
                        p.getProductQuantity()
                ))
                .toList();
        
        return excelExportService.generateExcel("GI Tag Products", headers, rows);
    }
    
    // ==================== GENERIC ====================
    
    private byte[] generateGenericReport(ReportType type, ReportRequest request) {
        return excelExportService.generateExcel(
                type.name(),
                List.of("Message"),
                List.of(List.of("Report type " + type + " not fully implemented yet"))
        );
    }
    
    // ==================== QUERIES ====================
    
    public ReportResponse getReport(String reportId) {
        return reportRepository.findById(reportId)
                .map(ReportResponse::fromReport)
                .orElse(ReportResponse.error("Report not found"));
    }
    
    public List<ReportResponse> getUserReports(String userId, int page, int size) {
        return reportRepository.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(page, size))
                .map(ReportResponse::fromReport)
                .getContent();
    }
    
    public byte[] downloadReport(String reportId) {
        Report report = reportRepository.findById(reportId).orElse(null);
        if (report == null || report.getStatus() != ReportStatus.COMPLETED) {
            return null;
        }
        // In production, fetch from cloud storage
        return loadReportFile(reportId, report.getFileName());
    }
    
    public void deleteReport(String reportId, String userId) {
        Report report = reportRepository.findById(reportId).orElse(null);
        if (report != null && report.getUserId().equals(userId)) {
            reportRepository.delete(report);
        }
    }
    
    // ==================== HELPER METHODS ====================
    
    private List<Order> getOrdersInRange(ReportRequest request) {
        // In production, use date range query
        List<Order> allOrders = orderRepository.findAll();
        
        return allOrders.stream()
                .filter(o -> {
                    if (o.getCreatedAt() == null) return false;
                    if (request.getStartDateTime() != null && o.getCreatedAt().isBefore(request.getStartDateTime())) {
                        return false;
                    }
                    if (request.getEndDateTime() != null && o.getCreatedAt().isAfter(request.getEndDateTime())) {
                        return false;
                    }
                    if (request.getVendorId() != null && !request.getVendorId().equals(o.getVendorId())) {
                        return false;
                    }
                    return true;
                })
                .toList();
    }
    
    private String generateReportName(ReportType type) {
        String typeName = type.name().replace("_", " ");
        return typeName + " - " + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy"));
    }
    
    private String generateFileName(Report report) {
        String name = report.getReportName().replaceAll("[^a-zA-Z0-9]", "_");
        String extension = switch (report.getFormat()) {
            case EXCEL -> ".xlsx";
            case CSV -> ".csv";
            case PDF -> ".pdf";
        };
        return name + "_" + System.currentTimeMillis() + extension;
    }
    
    private String saveReportFile(String reportId, String fileName, byte[] data) {
        // In production, save to cloud storage (S3, Azure Blob, etc.)
        // For now, return a mock URL
        return "/odop/reports/download/" + reportId;
    }
    
    private byte[] loadReportFile(String reportId, String fileName) {
        // In production, load from cloud storage
        return null;
    }
    
    private String extractState(String address) {
        if (address == null) return "Unknown";
        // Simple extraction - in production use proper parsing
        String[] parts = address.split(",");
        if (parts.length >= 2) {
            return parts[parts.length - 2].trim();
        }
        return "Unknown";
    }
    
    private double calculateSubtotal(Order order) {
        if (order.getOrderItems() == null || order.getOrderItems().isEmpty()) {
            return order.getTotalAmount() - order.getDeliveryCharges() + order.getDiscountAmount();
        }
        return order.getOrderItems().stream()
                .mapToDouble(item -> item.getUnitPrice() * item.getQuantity())
                .sum();
    }
    
    // Helper classes
    private static class ProductSales {
        String productId;
        String productName;
        int orders;
        int quantity;
        double revenue;
        
        ProductSales(String id, String name) {
            this.productId = id;
            this.productName = name;
        }
    }
    
    private static class RegionSales {
        String region;
        int orders;
        double revenue;
        
        RegionSales(String region) {
            this.region = region;
        }
    }
    
    private static class DistrictSales {
        String district;
        String state;
        int orders;
        int quantity;
        double revenue;
        
        DistrictSales(String district, String state) {
            this.district = district;
            this.state = state;
        }
    }
}
