package com.odop.root.analytics.dto;

import lombok.*;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VendorAnalyticsDto {
    
    // For specific vendor
    private String vendorId;
    private String shoppeeName;
    
    // Revenue
    private double totalRevenue;
    private double todayRevenue;
    private double monthRevenue;
    private double revenueGrowthPercent;
    
    // Orders
    private long totalOrders;
    private long pendingOrders;
    private long completedOrders;
    private long cancelledOrders;
    private double fulfillmentRate;
    
    // Products
    private long totalProducts;
    private long activeProducts;
    private long outOfStockProducts;
    private List<ProductPerformance> topProducts;
    
    // Customers
    private long uniqueCustomers;
    private long repeatCustomers;
    private double repeatCustomerRate;
    
    // Ratings
    private double averageRating;
    private long totalReviews;
    private Map<Integer, Long> ratingDistribution;
    
    // Time series
    private List<TimeSeriesDataDto> dailyRevenue;
    private List<TimeSeriesDataDto> dailyOrders;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProductPerformance {
        private String productId;
        private String productName;
        private long unitsSold;
        private double revenue;
        private int rating;
        private String imageUrl;
    }
}
