package com.odop.root.analytics.service;

import com.odop.root.analytics.dto.*;
import com.odop.root.models.*;
import com.odop.root.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class VendorAnalyticsService {

    private final MongoTemplate mongoTemplate;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final VendorRepository vendorRepository;
    private final ReviewRepository reviewRepository;

    /**
     * Get analytics for a specific vendor
     */
    public VendorAnalyticsDto getVendorAnalytics(String vendorId, AnalyticsFilterRequest filter) {
        Vendor vendor = vendorRepository.findByVendorId(vendorId);
        if (vendor == null) {
            throw new RuntimeException("Vendor not found: " + vendorId);
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayStart = now.toLocalDate().atStartOfDay();
        LocalDateTime monthStart = now.minusDays(30);

        // Get vendor's orders
        List<Order> vendorOrders = orderRepository.findByVendorId(vendorId);
        
        // Revenue calculations
        double totalRevenue = vendorOrders.stream()
            .filter(o -> !"CANCELLED".equals(o.getOrderStatus()))
            .mapToDouble(Order::getFinalAmount).sum();
        
        double todayRevenue = vendorOrders.stream()
            .filter(o -> o.getCreatedAt() != null && o.getCreatedAt().isAfter(todayStart))
            .filter(o -> !"CANCELLED".equals(o.getOrderStatus()))
            .mapToDouble(Order::getFinalAmount).sum();
        
        double monthRevenue = vendorOrders.stream()
            .filter(o -> o.getCreatedAt() != null && o.getCreatedAt().isAfter(monthStart))
            .filter(o -> !"CANCELLED".equals(o.getOrderStatus()))
            .mapToDouble(Order::getFinalAmount).sum();

        // Order counts
        long totalOrders = vendorOrders.size();
        long pendingOrders = vendorOrders.stream()
            .filter(o -> "PENDING".equals(o.getOrderStatus()) || "PROCESSING".equals(o.getOrderStatus()))
            .count();
        long completedOrders = vendorOrders.stream()
            .filter(o -> "DELIVERED".equals(o.getOrderStatus()))
            .count();
        long cancelledOrders = vendorOrders.stream()
            .filter(o -> "CANCELLED".equals(o.getOrderStatus()))
            .count();
        double fulfillmentRate = totalOrders > 0 ? 
            (completedOrders * 100.0 / (totalOrders - cancelledOrders)) : 0;

        // Product counts
        List<Products> vendorProducts = productRepository.findByVendorId(vendorId);
        long totalProducts = vendorProducts.size();
        long outOfStock = vendorProducts.stream()
            .filter(p -> p.getProductQuantity() <= 0 || "Out of Stock".equals(p.getStockStatus()))
            .count();

        // Customer analytics
        Set<String> uniqueCustomerIds = vendorOrders.stream()
            .map(Order::getCustomerId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        
        Map<String, Long> customerOrderCount = vendorOrders.stream()
            .filter(o -> o.getCustomerId() != null)
            .collect(Collectors.groupingBy(Order::getCustomerId, Collectors.counting()));
        
        long repeatCustomers = customerOrderCount.values().stream()
            .filter(count -> count > 1)
            .count();
        
        double repeatRate = uniqueCustomerIds.size() > 0 ?
            (repeatCustomers * 100.0 / uniqueCustomerIds.size()) : 0;

        // Top products by sales
        List<VendorAnalyticsDto.ProductPerformance> topProducts = getTopProductsForVendor(vendorOrders, vendorProducts, 5);

        // Rating distribution
        Map<Integer, Long> ratingDist = new HashMap<>();
        for (int i = 1; i <= 5; i++) ratingDist.put(i, 0L);
        
        List<Review> reviews = reviewRepository.findByVendorId(vendorId);
        for (Review review : reviews) {
            int r = review.getRating();
            if (r >= 1 && r <= 5) {
                ratingDist.merge(r, 1L, Long::sum);
            }
        }

        return VendorAnalyticsDto.builder()
            .vendorId(vendorId)
            .shoppeeName(vendor.getShoppeeName())
            .totalRevenue(totalRevenue)
            .todayRevenue(todayRevenue)
            .monthRevenue(monthRevenue)
            .totalOrders(totalOrders)
            .pendingOrders(pendingOrders)
            .completedOrders(completedOrders)
            .cancelledOrders(cancelledOrders)
            .fulfillmentRate(Math.round(fulfillmentRate * 100.0) / 100.0)
            .totalProducts(totalProducts)
            .activeProducts(totalProducts - outOfStock)
            .outOfStockProducts(outOfStock)
            .topProducts(topProducts)
            .uniqueCustomers((long) uniqueCustomerIds.size())
            .repeatCustomers(repeatCustomers)
            .repeatCustomerRate(Math.round(repeatRate * 100.0) / 100.0)
            .averageRating(vendor.getRatings() != null ? vendor.getRatings() : 0)
            .totalReviews((long) reviews.size())
            .ratingDistribution(ratingDist)
            .build();
    }

    private List<VendorAnalyticsDto.ProductPerformance> getTopProductsForVendor(
            List<Order> orders, List<Products> products, int limit) {
        
        Map<String, Long> productSales = new HashMap<>();
        Map<String, Double> productRevenue = new HashMap<>();

        for (Order order : orders) {
            if (order.getOrderItems() != null && !"CANCELLED".equals(order.getOrderStatus())) {
                for (OrderItem item : order.getOrderItems()) {
                    productSales.merge(item.getProductId(), (long) item.getQuantity(), Long::sum);
                    productRevenue.merge(item.getProductId(), item.getTotalPrice(), Double::sum);
                }
            }
        }

        Map<String, Products> productMap = products.stream()
            .collect(Collectors.toMap(Products::getProductId, p -> p, (a, b) -> a));

        return productSales.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(limit)
            .map(entry -> {
                Products product = productMap.get(entry.getKey());
                return VendorAnalyticsDto.ProductPerformance.builder()
                    .productId(entry.getKey())
                    .productName(product != null ? product.getProductName() : "Unknown")
                    .unitsSold(entry.getValue())
                    .revenue(productRevenue.getOrDefault(entry.getKey(), 0.0))
                    .rating(product != null ? product.getRating() : 0)
                    .imageUrl(product != null ? product.getProductImageURL() : null)
                    .build();
            })
            .collect(Collectors.toList());
    }

    /**
     * Get vendor leaderboard
     */
    public List<SalesAnalyticsDto.TopVendorDto> getVendorLeaderboard(int limit) {
        List<Vendor> vendors = vendorRepository.findAll();
        List<Order> allOrders = orderRepository.findAll();

        Map<String, Double> vendorRevenue = allOrders.stream()
            .filter(o -> o.getVendorId() != null && !"CANCELLED".equals(o.getOrderStatus()))
            .collect(Collectors.groupingBy(
                Order::getVendorId,
                Collectors.summingDouble(Order::getFinalAmount)
            ));

        Map<String, Long> vendorOrders = allOrders.stream()
            .filter(o -> o.getVendorId() != null)
            .collect(Collectors.groupingBy(Order::getVendorId, Collectors.counting()));

        Map<String, Vendor> vendorMap = vendors.stream()
            .collect(Collectors.toMap(Vendor::getVendorId, v -> v, (a, b) -> a));

        return vendorRevenue.entrySet().stream()
            .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
            .limit(limit)
            .map(entry -> {
                Vendor vendor = vendorMap.get(entry.getKey());
                return SalesAnalyticsDto.TopVendorDto.builder()
                    .vendorId(entry.getKey())
                    .shoppeeName(vendor != null ? vendor.getShoppeeName() : "Unknown")
                    .location(vendor != null ? 
                        (vendor.getLocationDistrict() + ", " + vendor.getLocationState()) : "")
                    .revenue(entry.getValue())
                    .orders(vendorOrders.getOrDefault(entry.getKey(), 0L))
                    .rating(vendor != null && vendor.getRatings() != null ? vendor.getRatings() : 0)
                    .build();
            })
            .collect(Collectors.toList());
    }
}
