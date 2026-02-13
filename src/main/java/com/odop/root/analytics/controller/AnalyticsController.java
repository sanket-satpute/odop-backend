package com.odop.root.analytics.controller;

import com.odop.root.analytics.dto.*;
import com.odop.root.analytics.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/odop/analytics")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final VendorAnalyticsService vendorAnalyticsService;

    /**
     * Health check
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Analytics Dashboard API");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    // ==================== ADMIN DASHBOARD ====================

    /**
     * Get dashboard summary - overview of all metrics
     */
    @GetMapping("/dashboard/summary")
    public ResponseEntity<DashboardSummaryDto> getDashboardSummary() {
        log.info("Fetching dashboard summary");
        DashboardSummaryDto summary = analyticsService.getDashboardSummary();
        return ResponseEntity.ok(summary);
    }

    /**
     * Get sales analytics with filters
     */
    @GetMapping("/sales")
    public ResponseEntity<SalesAnalyticsDto> getSalesAnalytics(
            @RequestParam(required = false) String period,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String orderStatus,
            @RequestParam(required = false) String vendorId) {
        
        AnalyticsFilterRequest filter = AnalyticsFilterRequest.builder()
            .period(period != null ? period : "MONTH")
            .state(state)
            .orderStatus(orderStatus)
            .vendorId(vendorId)
            .build();
        
        log.info("Fetching sales analytics with filter: {}", filter);
        SalesAnalyticsDto analytics = analyticsService.getSalesAnalytics(filter);
        return ResponseEntity.ok(analytics);
    }

    /**
     * Get geographic analytics - state/district wise data
     */
    @GetMapping("/geographic")
    public ResponseEntity<GeographicAnalyticsDto> getGeographicAnalytics(
            @RequestParam(required = false) String period) {
        
        AnalyticsFilterRequest filter = AnalyticsFilterRequest.builder()
            .period(period != null ? period : "MONTH")
            .build();
        
        log.info("Fetching geographic analytics");
        GeographicAnalyticsDto analytics = analyticsService.getGeographicAnalytics(filter);
        return ResponseEntity.ok(analytics);
    }

    /**
     * Get top vendors leaderboard
     */
    @GetMapping("/vendors/leaderboard")
    public ResponseEntity<List<SalesAnalyticsDto.TopVendorDto>> getVendorLeaderboard(
            @RequestParam(defaultValue = "10") int limit) {
        
        log.info("Fetching vendor leaderboard, limit: {}", limit);
        List<SalesAnalyticsDto.TopVendorDto> leaderboard = vendorAnalyticsService.getVendorLeaderboard(limit);
        return ResponseEntity.ok(leaderboard);
    }

    // ==================== VENDOR DASHBOARD ====================

    /**
     * Get analytics for a specific vendor
     */
    @GetMapping("/vendor/{vendorId}/summary")
    public ResponseEntity<VendorAnalyticsDto> getVendorSummary(
            @PathVariable String vendorId,
            @RequestParam(required = false) String period) {
        
        AnalyticsFilterRequest filter = AnalyticsFilterRequest.builder()
            .period(period != null ? period : "MONTH")
            .vendorId(vendorId)
            .build();
        
        log.info("Fetching vendor analytics for: {}", vendorId);
        VendorAnalyticsDto analytics = vendorAnalyticsService.getVendorAnalytics(vendorId, filter);
        return ResponseEntity.ok(analytics);
    }

    /**
     * Get vendor's sales analytics
     */
    @GetMapping("/vendor/{vendorId}/sales")
    public ResponseEntity<SalesAnalyticsDto> getVendorSales(
            @PathVariable String vendorId,
            @RequestParam(required = false) String period) {
        
        AnalyticsFilterRequest filter = AnalyticsFilterRequest.builder()
            .period(period != null ? period : "MONTH")
            .vendorId(vendorId)
            .build();
        
        log.info("Fetching vendor sales for: {}", vendorId);
        SalesAnalyticsDto analytics = analyticsService.getSalesAnalytics(filter);
        return ResponseEntity.ok(analytics);
    }

    // ==================== ODOP SPECIFIC ====================

    /**
     * Get ODOP district-wise analytics
     */
    @GetMapping("/odop/districts")
    public ResponseEntity<GeographicAnalyticsDto> getOdopDistrictAnalytics(
            @RequestParam(required = false) String state) {
        
        AnalyticsFilterRequest filter = AnalyticsFilterRequest.builder()
            .state(state)
            .build();
        
        log.info("Fetching ODOP district analytics for state: {}", state);
        GeographicAnalyticsDto analytics = analyticsService.getGeographicAnalytics(filter);
        return ResponseEntity.ok(analytics);
    }

    /**
     * Get GI-tagged products analytics
     */
    @GetMapping("/odop/gi-products")
    public ResponseEntity<SalesAnalyticsDto> getGiProductsAnalytics(
            @RequestParam(required = false) String period) {
        
        AnalyticsFilterRequest filter = AnalyticsFilterRequest.builder()
            .period(period != null ? period : "MONTH")
            .giTagOnly(true)
            .build();
        
        log.info("Fetching GI products analytics");
        SalesAnalyticsDto analytics = analyticsService.getSalesAnalytics(filter);
        return ResponseEntity.ok(analytics);
    }
}
