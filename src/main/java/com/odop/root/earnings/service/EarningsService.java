package com.odop.root.earnings.service;

import com.odop.root.earnings.dto.*;
import com.odop.root.models.Order;
import com.odop.root.models.Products;
import com.odop.root.repository.OrderRepository;
import com.odop.root.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for vendor earnings calculations and payout management
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EarningsService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    
    // Platform fee rate (percentage)
    private static final double PLATFORM_FEE_RATE = 5.0;
    
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Get earnings overview for a vendor
     */
    public EarningsOverviewDto getEarningsOverview(String vendorId, String period) {
        log.info("Fetching earnings overview for vendor: {}, period: {}", vendorId, period);
        
        LocalDateTime[] dateRange = getDateRange(period);
        LocalDateTime startDate = dateRange[0];
        LocalDateTime endDate = dateRange[1];
        
        // Get orders for current period
        List<Order> currentOrders = orderRepository.findByVendorIdAndCreatedAtBetweenAndOrderStatus(
            vendorId, startDate, endDate, "DELIVERED"
        );
        
        // Get orders for previous period (for comparison)
        LocalDateTime[] prevDateRange = getPreviousDateRange(period, startDate);
        List<Order> previousOrders = orderRepository.findByVendorIdAndCreatedAtBetweenAndOrderStatus(
            vendorId, prevDateRange[0], prevDateRange[1], "DELIVERED"
        );
        
        // Calculate current period metrics
        double totalEarnings = currentOrders.stream()
            .mapToDouble(Order::getFinalAmount)
            .sum();
        
        double previousEarnings = previousOrders.stream()
            .mapToDouble(Order::getFinalAmount)
            .sum();
        
        // Calculate platform fee and net earnings
        double platformFee = totalEarnings * (PLATFORM_FEE_RATE / 100);
        double netEarnings = totalEarnings - platformFee;
        
        // Calculate change percentages
        double earningsChange = calculatePercentageChange(previousEarnings, totalEarnings);
        double ordersChange = calculatePercentageChange(previousOrders.size(), currentOrders.size());
        
        // Get all-time orders for lifetime earnings
        List<Order> allTimeOrders = orderRepository.findByVendorIdAndOrderStatus(vendorId, "DELIVERED");
        double lifetimeEarnings = allTimeOrders.stream()
            .mapToDouble(Order::getFinalAmount)
            .sum();
        
        // Get pending orders
        List<Order> pendingOrders = orderRepository.findByVendorIdAndOrderStatusIn(
            vendorId, Arrays.asList("PENDING", "CONFIRMED", "PROCESSING", "SHIPPED")
        );
        
        double pendingPayouts = pendingOrders.stream()
            .mapToDouble(Order::getFinalAmount)
            .sum();
        
        // Get cancelled orders count
        List<Order> cancelledOrders = orderRepository.findByVendorIdAndCreatedAtBetweenAndOrderStatus(
            vendorId, startDate, endDate, "CANCELLED"
        );
        
        // Build earnings breakdown by day/week/month
        List<EarningsOverviewDto.EarningsBreakdownDto> breakdown = buildEarningsBreakdown(currentOrders, period);
        
        // Get top products by earnings
        List<EarningsOverviewDto.EarningsByProductDto> topProducts = getTopProductsByEarnings(currentOrders, totalEarnings);
        
        return EarningsOverviewDto.builder()
            .totalEarnings(totalEarnings)
            .pendingPayouts(pendingPayouts)
            .availableBalance(netEarnings) // Simplified - in production track actual payouts
            .lifetimeEarnings(lifetimeEarnings)
            .earningsChangePercent(earningsChange)
            .ordersChangePercent(ordersChange)
            .totalOrders(currentOrders.size() + pendingOrders.size())
            .completedOrders(currentOrders.size())
            .pendingOrders(pendingOrders.size())
            .cancelledOrders(cancelledOrders.size())
            .platformFee(platformFee)
            .platformFeeRate(PLATFORM_FEE_RATE)
            .netEarnings(netEarnings)
            .period(period)
            .startDate(startDate.format(dateFormatter))
            .endDate(endDate.format(dateFormatter))
            .breakdown(breakdown)
            .topProducts(topProducts)
            .build();
    }

    /**
     * Get transaction history for a vendor
     */
    public TransactionPageDto getTransactions(String vendorId, String type, int page, int size) {
        log.info("Fetching transactions for vendor: {}, type: {}, page: {}", vendorId, type, page);
        
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        // Get orders and map to transactions
        Page<Order> ordersPage;
        if (type != null && !type.isEmpty()) {
            ordersPage = getOrdersByType(vendorId, type, pageRequest);
        } else {
            ordersPage = orderRepository.findByVendorId(vendorId, pageRequest);
        }
        
        List<TransactionDto> transactions = ordersPage.getContent().stream()
            .map(this::mapOrderToTransaction)
            .collect(Collectors.toList());
        
        // Calculate summary
        double totalCredits = transactions.stream()
            .filter(t -> "SALE".equals(t.getType()))
            .mapToDouble(TransactionDto::getAmount)
            .sum();
        
        double totalDebits = transactions.stream()
            .filter(t -> "REFUND".equals(t.getType()) || "PLATFORM_FEE".equals(t.getType()))
            .mapToDouble(TransactionDto::getAmount)
            .sum();
        
        return TransactionPageDto.builder()
            .transactions(transactions)
            .page(page)
            .size(size)
            .totalElements(ordersPage.getTotalElements())
            .totalPages(ordersPage.getTotalPages())
            .hasNext(ordersPage.hasNext())
            .hasPrevious(ordersPage.hasPrevious())
            .totalCredits(totalCredits)
            .totalDebits(totalDebits)
            .netTotal(totalCredits - totalDebits)
            .build();
    }

    /**
     * Request a payout withdrawal
     */
    public PayoutDto requestPayout(PayoutRequestDto request) {
        log.info("Processing payout request for vendor: {}, amount: {}", request.getVendorId(), request.getAmount());
        
        // In production, validate available balance and process actual payment
        // For now, return a pending payout record
        
        return PayoutDto.builder()
            .payoutId(UUID.randomUUID().toString())
            .vendorId(request.getVendorId())
            .amount(request.getAmount())
            .status("PENDING")
            .paymentMethod(request.getPaymentMethod())
            .upiId(request.getUpiId())
            .requestedAt(LocalDateTime.now())
            .remarks(request.getRemarks())
            .build();
    }

    /**
     * Get payout history for a vendor
     */
    public List<PayoutDto> getPayoutHistory(String vendorId, int limit) {
        log.info("Fetching payout history for vendor: {}", vendorId);
        
        // In production, query from a payouts collection
        // For now, return sample data
        return buildSamplePayoutHistory(vendorId, limit);
    }

    // ==================== HELPER METHODS ====================

    private LocalDateTime[] getDateRange(String period) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate;
        
        switch (period.toUpperCase()) {
            case "WEEK":
                startDate = now.minusWeeks(1);
                break;
            case "QUARTER":
                startDate = now.minusMonths(3);
                break;
            case "YEAR":
                startDate = now.minusYears(1);
                break;
            case "ALL_TIME":
                startDate = now.minusYears(10); // Effectively all time
                break;
            case "MONTH":
            default:
                startDate = now.minusMonths(1);
                break;
        }
        
        return new LocalDateTime[] { startDate, now };
    }

    private LocalDateTime[] getPreviousDateRange(String period, LocalDateTime currentStart) {
        long daysDiff = ChronoUnit.DAYS.between(currentStart, LocalDateTime.now());
        LocalDateTime prevEnd = currentStart;
        LocalDateTime prevStart = prevEnd.minusDays(daysDiff);
        return new LocalDateTime[] { prevStart, prevEnd };
    }

    private double calculatePercentageChange(double previous, double current) {
        if (previous == 0) {
            return current > 0 ? 100.0 : 0.0;
        }
        return ((current - previous) / previous) * 100;
    }

    private List<EarningsOverviewDto.EarningsBreakdownDto> buildEarningsBreakdown(List<Order> orders, String period) {
        Map<String, List<Order>> groupedOrders = orders.stream()
            .filter(o -> o.getCreatedAt() != null)
            .collect(Collectors.groupingBy(o -> o.getCreatedAt().format(dateFormatter)));
        
        return groupedOrders.entrySet().stream()
            .map(entry -> {
                List<Order> dayOrders = entry.getValue();
                double earnings = dayOrders.stream().mapToDouble(Order::getFinalAmount).sum();
                double avgOrder = dayOrders.isEmpty() ? 0 : earnings / dayOrders.size();
                
                return EarningsOverviewDto.EarningsBreakdownDto.builder()
                    .date(entry.getKey())
                    .earnings(earnings)
                    .orders(dayOrders.size())
                    .avgOrderValue(avgOrder)
                    .build();
            })
            .sorted(Comparator.comparing(EarningsOverviewDto.EarningsBreakdownDto::getDate))
            .collect(Collectors.toList());
    }

    private List<EarningsOverviewDto.EarningsByProductDto> getTopProductsByEarnings(List<Order> orders, double totalEarnings) {
        Map<String, Double> productEarnings = new HashMap<>();
        Map<String, Integer> productUnits = new HashMap<>();
        
        for (Order order : orders) {
            if (order.getOrderItems() != null) {
                order.getOrderItems().forEach(item -> {
                    String productId = item.getProductId();
                    productEarnings.merge(productId, item.getUnitPrice() * item.getQuantity(), Double::sum);
                    productUnits.merge(productId, item.getQuantity(), Integer::sum);
                });
            }
        }
        
        // Get product details
        Set<String> productIds = productEarnings.keySet();
        Map<String, Products> productsMap = productRepository.findAllById(productIds).stream()
            .collect(Collectors.toMap(Products::getProductId, p -> p));
        
        return productEarnings.entrySet().stream()
            .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
            .limit(5)
            .map(entry -> {
                String productId = entry.getKey();
                Products product = productsMap.get(productId);
                double earnings = entry.getValue();
                
                return EarningsOverviewDto.EarningsByProductDto.builder()
                    .productId(productId)
                    .productName(product != null ? product.getProductName() : "Unknown Product")
                    .productImage(product != null ? product.getProductImageURL() : null)
                    .unitsSold(productUnits.getOrDefault(productId, 0))
                    .totalEarnings(earnings)
                    .percentage(totalEarnings > 0 ? (earnings / totalEarnings) * 100 : 0)
                    .build();
            })
            .collect(Collectors.toList());
    }

    private Page<Order> getOrdersByType(String vendorId, String type, PageRequest pageRequest) {
        switch (type.toUpperCase()) {
            case "SALE":
                return orderRepository.findByVendorIdAndOrderStatus(vendorId, "DELIVERED", pageRequest);
            case "REFUND":
                return orderRepository.findByVendorIdAndOrderStatus(vendorId, "RETURNED", pageRequest);
            default:
                return orderRepository.findByVendorId(vendorId, pageRequest);
        }
    }

    private TransactionDto mapOrderToTransaction(Order order) {
        String type = "SALE";
        if ("RETURNED".equals(order.getOrderStatus()) || "REFUNDED".equals(order.getPaymentStatus())) {
            type = "REFUND";
        } else if ("CANCELLED".equals(order.getOrderStatus())) {
            type = "CANCELLED";
        }
        
        double platformFee = order.getFinalAmount() * (PLATFORM_FEE_RATE / 100);
        String productName = order.getOrderItems() != null && !order.getOrderItems().isEmpty() 
            ? order.getOrderItems().get(0).getProductName()
            : "Multiple items";
        int quantity = order.getOrderItems() != null 
            ? order.getOrderItems().stream().mapToInt(i -> i.getQuantity()).sum()
            : 0;
        
        return TransactionDto.builder()
            .transactionId(order.getPaymentTransactionId() != null ? order.getPaymentTransactionId() : order.getOrderId())
            .orderId(order.getOrderId())
            .vendorId(order.getVendorId())
            .type(type)
            .amount(order.getFinalAmount())
            .netAmount(order.getFinalAmount() - platformFee)
            .platformFee(platformFee)
            .status("SALE".equals(type) ? "COMPLETED" : type.equals("REFUND") ? "REVERSED" : "PENDING")
            .description(buildTransactionDescription(order, type))
            .productName(productName)
            .quantity(quantity)
            .createdAt(order.getCreatedAt())
            .build();
    }

    private String buildTransactionDescription(Order order, String type) {
        if ("SALE".equals(type)) {
            return "Payment received for order #" + order.getOrderId();
        } else if ("REFUND".equals(type)) {
            return "Refund processed for order #" + order.getOrderId();
        }
        return "Transaction for order #" + order.getOrderId();
    }

    private List<PayoutDto> buildSamplePayoutHistory(String vendorId, int limit) {
        // Sample payout data - in production, query from payouts collection
        List<PayoutDto> payouts = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        
        payouts.add(PayoutDto.builder()
            .payoutId("PAY-" + UUID.randomUUID().toString().substring(0, 8))
            .vendorId(vendorId)
            .amount(15000.00)
            .status("COMPLETED")
            .paymentMethod("BANK_TRANSFER")
            .bankName("State Bank of India")
            .accountNumberMasked("****4523")
            .requestedAt(now.minusDays(7))
            .processedAt(now.minusDays(6))
            .completedAt(now.minusDays(5))
            .transactionId("TXN123456789")
            .build());
        
        payouts.add(PayoutDto.builder()
            .payoutId("PAY-" + UUID.randomUUID().toString().substring(0, 8))
            .vendorId(vendorId)
            .amount(8500.00)
            .status("COMPLETED")
            .paymentMethod("UPI")
            .upiId("vendor@upi")
            .requestedAt(now.minusDays(14))
            .processedAt(now.minusDays(13))
            .completedAt(now.minusDays(13))
            .transactionId("TXN987654321")
            .build());
        
        return payouts.stream().limit(limit).collect(Collectors.toList());
    }
}
