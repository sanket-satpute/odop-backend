package com.odop.root.report.controller;

import com.odop.root.report.dto.*;
import com.odop.root.report.model.Report;
import com.odop.root.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * REST Controller for reports
 */
@RestController
@RequestMapping("/odop/reports")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ReportController {
    
    private final ReportService reportService;
    
    // ==================== REPORT GENERATION ====================
    
    /**
     * Generate a new report
     */
    @PostMapping("/generate")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR')")
    public ResponseEntity<?> generateReport(
            @RequestBody ReportRequest request,
            Authentication auth) {
        
        try {
            String userId = auth.getName();
            String userRole = getUserRole(auth);
            
            // Validate report type access
            if (!canAccessReportType(request.getReportType(), userRole)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                        "success", false,
                        "message", "You don't have permission to generate this report type"
                ));
            }
            
            // For vendors, restrict to their own data
            if ("VENDOR".equals(userRole)) {
                request.setVendorId(userId);
            }
            
            // Create report job
            Report report = reportService.createReport(userId, userRole, request);
            
            // Start async generation
            reportService.generateReportAsync(report.getId(), request);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Report generation started",
                    "report", ReportResponse.started(report)
            ));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Error generating report", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Failed to generate report"
            ));
        }
    }
    
    /**
     * Get report status
     */
    @GetMapping("/{reportId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR')")
    public ResponseEntity<?> getReport(@PathVariable String reportId, Authentication auth) {
        ReportResponse response = reportService.getReport(reportId);
        
        if (response.getReportId() == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get user's report history
     */
    @GetMapping("/history")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR')")
    public ResponseEntity<?> getReportHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication auth) {
        
        String userId = auth.getName();
        List<ReportResponse> reports = reportService.getUserReports(userId, page, size);
        
        return ResponseEntity.ok(Map.of(
                "success", true,
                "reports", reports,
                "page", page,
                "size", size
        ));
    }
    
    /**
     * Download report file
     */
    @GetMapping("/download/{reportId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR')")
    public ResponseEntity<?> downloadReport(@PathVariable String reportId, Authentication auth) {
        try {
            ReportResponse reportInfo = reportService.getReport(reportId);
            
            if (reportInfo.getReportId() == null) {
                return ResponseEntity.notFound().build();
            }
            
            if (!"COMPLETED".equals(reportInfo.getStatus())) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Report is not ready for download"
                ));
            }
            
            byte[] data = reportService.downloadReport(reportId);
            
            if (data == null) {
                // For demo, regenerate the report inline
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                        "success", false,
                        "message", "Report file not available. Please regenerate."
                ));
            }
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(getMediaType(reportInfo.getFormat()));
            headers.setContentDisposition(ContentDisposition.attachment()
                    .filename(reportInfo.getFileName())
                    .build());
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(data);
                    
        } catch (Exception e) {
            log.error("Error downloading report", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Failed to download report"
            ));
        }
    }
    
    /**
     * Delete a report
     */
    @DeleteMapping("/{reportId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR')")
    public ResponseEntity<?> deleteReport(@PathVariable String reportId, Authentication auth) {
        try {
            String userId = auth.getName();
            reportService.deleteReport(reportId, userId);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Report deleted"
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Failed to delete report"
            ));
        }
    }
    
    // ==================== REPORT TYPES ====================
    
    /**
     * Get available report types
     */
    @GetMapping("/types")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR')")
    public ResponseEntity<?> getReportTypes(Authentication auth) {
        String userRole = getUserRole(auth);
        List<Map<String, Object>> types = new ArrayList<>();
        
        // Sales reports
        types.add(createTypeInfo("SALES_SUMMARY", "Sales Summary", "Overview of sales with daily breakdown", true));
        types.add(createTypeInfo("SALES_BY_PRODUCT", "Sales by Product", "Sales breakdown by individual products", true));
        types.add(createTypeInfo("SALES_BY_REGION", "Sales by Region", "Sales breakdown by geographic region", true));
        
        // Order reports
        types.add(createTypeInfo("ORDER_HISTORY", "Order History", "Complete order history", true));
        types.add(createTypeInfo("ORDER_STATUS", "Order Status", "Orders grouped by status", true));
        
        // Product reports
        types.add(createTypeInfo("PRODUCT_INVENTORY", "Product Inventory", "Current stock levels for all products", true));
        types.add(createTypeInfo("LOW_STOCK_ALERT", "Low Stock Alert", "Products with low inventory", true));
        
        // Financial reports
        types.add(createTypeInfo("REVENUE_REPORT", "Revenue Report", "Revenue summary and breakdown", true));
        types.add(createTypeInfo("TAX_REPORT", "Tax Report", "GST/Tax collected summary", true));
        
        // ODOP specific (Admin only)
        if ("ADMIN".equals(userRole)) {
            types.add(createTypeInfo("ODOP_DISTRICT_SALES", "ODOP District Sales", "Sales by ODOP origin district", true));
            types.add(createTypeInfo("GI_TAG_PRODUCTS", "GI Tag Products", "GI certified products report", true));
            types.add(createTypeInfo("SALES_BY_VENDOR", "Sales by Vendor", "Sales breakdown by vendor", true));
        }
        
        return ResponseEntity.ok(Map.of(
                "success", true,
                "reportTypes", types
        ));
    }
    
    /**
     * Get report formats
     */
    @GetMapping("/formats")
    public ResponseEntity<?> getReportFormats() {
        return ResponseEntity.ok(Map.of(
                "success", true,
                "formats", List.of(
                        Map.of("value", "EXCEL", "label", "Excel (.xlsx)", "icon", "table"),
                        Map.of("value", "CSV", "label", "CSV (.csv)", "icon", "file-text"),
                        Map.of("value", "PDF", "label", "PDF (.pdf)", "icon", "file-pdf")
                )
        ));
    }
    
    // ==================== HEALTH CHECK ====================
    
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "Reports"
        ));
    }
    
    // ==================== HELPER METHODS ====================
    
    private String getUserRole(Authentication auth) {
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith("ROLE_"))
                .map(a -> a.substring(5))
                .findFirst()
                .orElse("USER");
    }
    
    private boolean canAccessReportType(String reportType, String userRole) {
        // Admin can access all
        if ("ADMIN".equals(userRole)) {
            return true;
        }
        
        // Vendor restricted reports
        Set<String> adminOnlyReports = Set.of(
                "SALES_BY_VENDOR",
                "PAYOUT_REPORT",
                "COMMISSION_REPORT",
                "CUSTOMER_ACTIVITY",
                "TOP_CUSTOMERS"
        );
        
        return !adminOnlyReports.contains(reportType);
    }
    
    private Map<String, Object> createTypeInfo(String value, String label, String description, boolean available) {
        Map<String, Object> info = new HashMap<>();
        info.put("value", value);
        info.put("label", label);
        info.put("description", description);
        info.put("available", available);
        return info;
    }
    
    private MediaType getMediaType(String format) {
        return switch (format) {
            case "EXCEL" -> MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            case "CSV" -> MediaType.parseMediaType("text/csv");
            case "PDF" -> MediaType.APPLICATION_PDF;
            default -> MediaType.APPLICATION_OCTET_STREAM;
        };
    }
}
