package com.odop.root.analytics.service;

import com.odop.root.analytics.dto.*;
import com.odop.root.models.*;
import com.odop.root.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

    private final MongoTemplate mongoTemplate;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final VendorRepository vendorRepository;
    private final CartRepository cartRepository;
    private final PaymentRepository paymentRepository;
    private final ProductCategoryRepository categoryRepository;

    /**
     * Get dashboard summary with all key metrics
     */
    public DashboardSummaryDto getDashboardSummary() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayStart = now.toLocalDate().atStartOfDay();
        LocalDateTime weekStart = now.minusDays(7);
        LocalDateTime monthStart = now.minusDays(30);
        LocalDateTime lastMonthStart = now.minusDays(60);

        // Revenue calculations
        double totalRevenue = calculateTotalRevenue(null, null);
        double todayRevenue = calculateTotalRevenue(todayStart, now);
        double weekRevenue = calculateTotalRevenue(weekStart, now);
        double monthRevenue = calculateTotalRevenue(monthStart, now);
        double lastMonthRevenue = calculateTotalRevenue(lastMonthStart, monthStart);
        double revenueGrowth = lastMonthRevenue > 0 ? 
            ((monthRevenue - lastMonthRevenue) / lastMonthRevenue) * 100 : 0;

        // Order counts
        List<Order> allOrders = orderRepository.findAll();
        long totalOrders = allOrders.size();
        long todayOrders = allOrders.stream()
            .filter(o -> o.getCreatedAt() != null && o.getCreatedAt().isAfter(todayStart))
            .count();
        long pendingOrders = allOrders.stream()
            .filter(o -> "PENDING".equals(o.getOrderStatus()) || "PROCESSING".equals(o.getOrderStatus()))
            .count();
        long deliveredOrders = allOrders.stream()
            .filter(o -> "DELIVERED".equals(o.getOrderStatus()))
            .count();
        long cancelledOrders = allOrders.stream()
            .filter(o -> "CANCELLED".equals(o.getOrderStatus()))
            .count();

        // Customer counts
        List<Customer> allCustomers = customerRepository.findAll();
        long totalCustomers = allCustomers.size();
        long newCustomersToday = allCustomers.stream()
            .filter(c -> c.getCreatedAt() != null && c.getCreatedAt().isAfter(todayStart))
            .count();
        long newCustomersMonth = allCustomers.stream()
            .filter(c -> c.getCreatedAt() != null && c.getCreatedAt().isAfter(monthStart))
            .count();

        // Vendor counts
        List<Vendor> allVendors = vendorRepository.findAll();
        long totalVendors = allVendors.size();
        long verifiedVendors = allVendors.stream()
            .filter(v -> Boolean.TRUE.equals(v.getVerified()))
            .count();
        long pendingVendors = allVendors.stream()
            .filter(v -> "pending".equalsIgnoreCase(v.getStatus()))
            .count();

        // Product counts
        List<Products> allProducts = productRepository.findAll();
        long totalProducts = allProducts.size();
        long outOfStock = allProducts.stream()
            .filter(p -> "Out of Stock".equals(p.getStockStatus()) || p.getProductQuantity() <= 0)
            .count();
        long giTagged = allProducts.stream()
            .filter(p -> Boolean.TRUE.equals(p.getGiTagCertified()))
            .count();

        // Cart analytics
        List<Cart> allCarts = cartRepository.findAll();
        long abandonedCarts = allCarts.stream()
            .filter(c -> "ABANDONED".equals(c.getStatus()))
            .count();
        long convertedCarts = allCarts.stream()
            .filter(c -> "CONVERTED".equals(c.getStatus()))
            .count();
        double conversionRate = allCarts.size() > 0 ? 
            (convertedCarts * 100.0 / allCarts.size()) : 0;

        // Average order value
        double avgOrderValue = totalOrders > 0 ? totalRevenue / totalOrders : 0;

        // ODOP specific
        Set<String> states = allProducts.stream()
            .map(Products::getOriginState)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        Set<String> districts = allProducts.stream()
            .map(Products::getOriginDistrict)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

        return DashboardSummaryDto.builder()
            .totalRevenue(totalRevenue)
            .todayRevenue(todayRevenue)
            .weekRevenue(weekRevenue)
            .monthRevenue(monthRevenue)
            .revenueGrowthPercent(Math.round(revenueGrowth * 100.0) / 100.0)
            .totalOrders(totalOrders)
            .todayOrders(todayOrders)
            .pendingOrders(pendingOrders)
            .deliveredOrders(deliveredOrders)
            .cancelledOrders(cancelledOrders)
            .totalCustomers(totalCustomers)
            .newCustomersToday(newCustomersToday)
            .newCustomersThisMonth(newCustomersMonth)
            .totalVendors(totalVendors)
            .activeVendors(verifiedVendors)
            .verifiedVendors(verifiedVendors)
            .pendingVerificationVendors(pendingVendors)
            .totalProducts(totalProducts)
            .activeProducts(totalProducts - outOfStock)
            .outOfStockProducts(outOfStock)
            .giTaggedProducts(giTagged)
            .averageOrderValue(Math.round(avgOrderValue * 100.0) / 100.0)
            .conversionRate(Math.round(conversionRate * 100.0) / 100.0)
            .abandonedCarts(abandonedCarts)
            .totalStates((long) states.size())
            .totalDistricts((long) districts.size())
            .generatedAt(LocalDateTime.now())
            .build();
    }

    /**
     * Calculate total revenue within date range
     */
    private double calculateTotalRevenue(LocalDateTime start, LocalDateTime end) {
        Query query = new Query();
        query.addCriteria(Criteria.where("orderStatus").in("DELIVERED", "SHIPPED", "CONFIRMED"));
        
        if (start != null && end != null) {
            query.addCriteria(Criteria.where("createdAt").gte(start).lte(end));
        }
        
        List<Order> orders = mongoTemplate.find(query, Order.class);
        return orders.stream()
            .mapToDouble(Order::getFinalAmount)
            .sum();
    }

    /**
     * Get sales analytics with filters
     */
    public SalesAnalyticsDto getSalesAnalytics(AnalyticsFilterRequest filter) {
        LocalDateTime[] dateRange = getDateRange(filter);
        LocalDateTime start = dateRange[0];
        LocalDateTime end = dateRange[1];

        Query query = new Query();
        query.addCriteria(Criteria.where("createdAt").gte(start).lte(end));
        
        if (filter.getOrderStatus() != null) {
            query.addCriteria(Criteria.where("orderStatus").is(filter.getOrderStatus()));
        }
        if (filter.getVendorId() != null) {
            query.addCriteria(Criteria.where("vendorId").is(filter.getVendorId()));
        }
        if (filter.getState() != null) {
            query.addCriteria(Criteria.where("shippingState").is(filter.getState()));
        }

        List<Order> orders = mongoTemplate.find(query, Order.class);
        
        double totalRevenue = orders.stream().mapToDouble(Order::getFinalAmount).sum();
        long totalOrders = orders.size();
        double avgOrderValue = totalOrders > 0 ? totalRevenue / totalOrders : 0;

        // Revenue by payment method
        Map<String, Double> revenueByPayment = orders.stream()
            .filter(o -> o.getPaymentMethod() != null)
            .collect(Collectors.groupingBy(
                Order::getPaymentMethod,
                Collectors.summingDouble(Order::getFinalAmount)
            ));

        // Orders by status
        Map<String, Long> ordersByStatus = orders.stream()
            .filter(o -> o.getOrderStatus() != null)
            .collect(Collectors.groupingBy(Order::getOrderStatus, Collectors.counting()));

        // Revenue by state
        Map<String, Double> revenueByState = orders.stream()
            .filter(o -> o.getShippingState() != null)
            .collect(Collectors.groupingBy(
                Order::getShippingState,
                Collectors.summingDouble(Order::getFinalAmount)
            ));

        // Top products
        List<SalesAnalyticsDto.TopSellingProductDto> topProducts = getTopSellingProducts(orders, 10);

        return SalesAnalyticsDto.builder()
            .totalRevenue(totalRevenue)
            .totalOrders(totalOrders)
            .averageOrderValue(Math.round(avgOrderValue * 100.0) / 100.0)
            .revenueByPaymentMethod(revenueByPayment)
            .revenueByState(revenueByState)
            .ordersByStatus(ordersByStatus)
            .topProducts(topProducts)
            .period(filter.getPeriod())
            .startDate(start.toLocalDate().toString())
            .endDate(end.toLocalDate().toString())
            .build();
    }

    /**
     * Get top selling products from orders
     */
    private List<SalesAnalyticsDto.TopSellingProductDto> getTopSellingProducts(List<Order> orders, int limit) {
        Map<String, Long> productSales = new HashMap<>();
        Map<String, Double> productRevenue = new HashMap<>();

        for (Order order : orders) {
            if (order.getOrderItems() != null) {
                for (OrderItem item : order.getOrderItems()) {
                    String productId = item.getProductId();
                    productSales.merge(productId, (long) item.getQuantity(), Long::sum);
                    productRevenue.merge(productId, item.getTotalPrice(), Double::sum);
                }
            }
        }

        return productSales.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(limit)
            .map(entry -> {
                String productId = entry.getKey();
                Products product = productRepository.findByProductId(productId);
                return SalesAnalyticsDto.TopSellingProductDto.builder()
                    .productId(productId)
                    .productName(product != null ? product.getProductName() : "Unknown")
                    .unitsSold(entry.getValue())
                    .revenue(productRevenue.getOrDefault(productId, 0.0))
                    .imageUrl(product != null ? product.getProductImageURL() : null)
                    .build();
            })
            .collect(Collectors.toList());
    }

    /**
     * Get date range from filter
     */
    private LocalDateTime[] getDateRange(AnalyticsFilterRequest filter) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start, end = now;

        if (filter.getStartDate() != null && filter.getEndDate() != null) {
            start = filter.getStartDate().atStartOfDay();
            end = filter.getEndDate().atTime(LocalTime.MAX);
        } else {
            String period = filter.getPeriod() != null ? filter.getPeriod() : "MONTH";
            switch (period.toUpperCase()) {
                case "TODAY":
                    start = now.toLocalDate().atStartOfDay();
                    break;
                case "WEEK":
                    start = now.minusDays(7);
                    break;
                case "QUARTER":
                    start = now.minusDays(90);
                    break;
                case "YEAR":
                    start = now.minusDays(365);
                    break;
                case "MONTH":
                default:
                    start = now.minusDays(30);
                    break;
            }
        }
        return new LocalDateTime[]{start, end};
    }

    /**
     * Get geographic analytics
     */
    public GeographicAnalyticsDto getGeographicAnalytics(AnalyticsFilterRequest filter) {
        List<Products> products = productRepository.findAll();
        List<Vendor> vendors = vendorRepository.findAll();
        List<Order> orders = orderRepository.findAll();

        // State-wise analytics
        Map<String, GeographicAnalyticsDto.StateAnalytics> stateMap = new HashMap<>();

        // Products by state
        for (Products product : products) {
            String state = product.getOriginState();
            if (state != null) {
                stateMap.computeIfAbsent(state, k -> GeographicAnalyticsDto.StateAnalytics.builder()
                    .state(k).revenue(0).orders(0).products(0).vendors(0).giTagProducts(0).build());
                GeographicAnalyticsDto.StateAnalytics sa = stateMap.get(state);
                sa.setProducts(sa.getProducts() + 1);
                if (Boolean.TRUE.equals(product.getGiTagCertified())) {
                    sa.setGiTagProducts(sa.getGiTagProducts() + 1);
                }
            }
        }

        // Vendors by state
        for (Vendor vendor : vendors) {
            String state = vendor.getLocationState();
            if (state != null && stateMap.containsKey(state)) {
                GeographicAnalyticsDto.StateAnalytics sa = stateMap.get(state);
                sa.setVendors(sa.getVendors() + 1);
            }
        }

        // Orders/Revenue by state
        for (Order order : orders) {
            String state = order.getShippingState();
            if (state != null) {
                stateMap.computeIfAbsent(state, k -> GeographicAnalyticsDto.StateAnalytics.builder()
                    .state(k).revenue(0).orders(0).products(0).vendors(0).giTagProducts(0).build());
                GeographicAnalyticsDto.StateAnalytics sa = stateMap.get(state);
                sa.setOrders(sa.getOrders() + 1);
                sa.setRevenue(sa.getRevenue() + order.getFinalAmount());
            }
        }

        List<GeographicAnalyticsDto.StateAnalytics> stateList = new ArrayList<>(stateMap.values());
        List<GeographicAnalyticsDto.StateAnalytics> topStates = stateList.stream()
            .sorted(Comparator.comparingDouble(GeographicAnalyticsDto.StateAnalytics::getRevenue).reversed())
            .limit(10)
            .collect(Collectors.toList());

        return GeographicAnalyticsDto.builder()
            .stateWiseData(stateList)
            .topRevenueStates(topStates)
            .build();
    }
}
