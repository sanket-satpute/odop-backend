package com.odop.root.earnings.controller;

import com.odop.root.earnings.dto.*;
import com.odop.root.earnings.service.EarningsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for vendor earnings management.
 * Provides endpoints for earnings overview, transactions, and payouts.
 */
@RestController
@RequestMapping("/odop/earnings")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class EarningsController {

    private final EarningsService earningsService;

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Vendor Earnings API");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    /**
     * Get earnings overview for a vendor
     * @param vendorId - Vendor ID
     * @param period - Time period (WEEK, MONTH, QUARTER, YEAR, ALL_TIME)
     */
    @GetMapping("/vendor/{vendorId}/overview")
    public ResponseEntity<EarningsOverviewDto> getEarningsOverview(
            @PathVariable String vendorId,
            @RequestParam(defaultValue = "MONTH") String period) {
        
        log.info("Fetching earnings overview for vendor: {}, period: {}", vendorId, period);
        EarningsOverviewDto overview = earningsService.getEarningsOverview(vendorId, period);
        return ResponseEntity.ok(overview);
    }

    /**
     * Get transaction history for a vendor
     * @param vendorId - Vendor ID
     * @param type - Transaction type filter (SALE, REFUND, PAYOUT)
     * @param page - Page number (0-based)
     * @param size - Page size
     */
    @GetMapping("/vendor/{vendorId}/transactions")
    public ResponseEntity<TransactionPageDto> getTransactions(
            @PathVariable String vendorId,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("Fetching transactions for vendor: {}, type: {}, page: {}", vendorId, type, page);
        TransactionPageDto transactions = earningsService.getTransactions(vendorId, type, page, size);
        return ResponseEntity.ok(transactions);
    }

    /**
     * Request a payout withdrawal
     * @param request - Payout request details
     */
    @PostMapping("/vendor/{vendorId}/payout")
    public ResponseEntity<PayoutDto> requestPayout(
            @PathVariable String vendorId,
            @RequestBody PayoutRequestDto request) {
        
        // Ensure vendorId matches
        request.setVendorId(vendorId);
        
        log.info("Processing payout request for vendor: {}, amount: {}", vendorId, request.getAmount());
        PayoutDto payout = earningsService.requestPayout(request);
        return ResponseEntity.ok(payout);
    }

    /**
     * Get payout history for a vendor
     * @param vendorId - Vendor ID
     * @param limit - Maximum number of records to return
     */
    @GetMapping("/vendor/{vendorId}/payouts")
    public ResponseEntity<List<PayoutDto>> getPayoutHistory(
            @PathVariable String vendorId,
            @RequestParam(defaultValue = "10") int limit) {
        
        log.info("Fetching payout history for vendor: {}", vendorId);
        List<PayoutDto> payouts = earningsService.getPayoutHistory(vendorId, limit);
        return ResponseEntity.ok(payouts);
    }

    /**
     * Get earnings summary by period for charting
     */
    @GetMapping("/vendor/{vendorId}/chart")
    public ResponseEntity<List<EarningsOverviewDto.EarningsBreakdownDto>> getEarningsChart(
            @PathVariable String vendorId,
            @RequestParam(defaultValue = "MONTH") String period) {
        
        log.info("Fetching earnings chart data for vendor: {}, period: {}", vendorId, period);
        EarningsOverviewDto overview = earningsService.getEarningsOverview(vendorId, period);
        return ResponseEntity.ok(overview.getBreakdown());
    }

    /**
     * Get top products by earnings
     */
    @GetMapping("/vendor/{vendorId}/top-products")
    public ResponseEntity<List<EarningsOverviewDto.EarningsByProductDto>> getTopProducts(
            @PathVariable String vendorId,
            @RequestParam(defaultValue = "MONTH") String period) {
        
        log.info("Fetching top products for vendor: {}", vendorId);
        EarningsOverviewDto overview = earningsService.getEarningsOverview(vendorId, period);
        return ResponseEntity.ok(overview.getTopProducts());
    }
}
