package com.odop.root.analytics.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardSummaryDto {
    
    // Revenue Metrics
    private double totalRevenue;
    private double todayRevenue;
    private double weekRevenue;
    private double monthRevenue;
    private double revenueGrowthPercent;
    
    // Order Metrics
    private long totalOrders;
    private long todayOrders;
    private long pendingOrders;
    private long deliveredOrders;
    private long cancelledOrders;
    private double orderGrowthPercent;
    
    // Customer Metrics
    private long totalCustomers;
    private long newCustomersToday;
    private long newCustomersThisMonth;
    private double customerGrowthPercent;
    
    // Vendor Metrics
    private long totalVendors;
    private long activeVendors;
    private long verifiedVendors;
    private long pendingVerificationVendors;
    
    // Product Metrics
    private long totalProducts;
    private long activeProducts;
    private long outOfStockProducts;
    private long giTaggedProducts;
    
    // Performance Metrics
    private double averageOrderValue;
    private double conversionRate;
    private long abandonedCarts;
    
    // ODOP Specific
    private long totalDistricts;
    private long totalStates;
    
    private LocalDateTime generatedAt;
}
