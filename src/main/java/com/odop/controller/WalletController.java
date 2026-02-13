package com.odop.controller;

import com.odop.model.Wallet;
import com.odop.model.Wallet.WalletTransaction;
import com.odop.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/odop/customer/{customerId}/wallet")
@CrossOrigin(origins = "*")
public class WalletController {

    @Autowired
    private WalletService walletService;

    /**
     * Get wallet details
     */
    @GetMapping
    public ResponseEntity<?> getWallet(@PathVariable String customerId) {
        Wallet wallet = walletService.getOrCreateWallet(customerId);
        return ResponseEntity.ok(wallet);
    }

    /**
     * Get wallet balance only
     */
    @GetMapping("/balance")
    public ResponseEntity<Map<String, Object>> getBalance(@PathVariable String customerId) {
        double balance = walletService.getBalance(customerId);
        return ResponseEntity.ok(Map.of(
            "balance", balance,
            "currency", "INR"
        ));
    }

    /**
     * Get wallet summary with statistics
     */
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getWalletSummary(@PathVariable String customerId) {
        Map<String, Object> summary = walletService.getWalletSummary(customerId);
        return ResponseEntity.ok(summary);
    }

    /**
     * Add money to wallet
     */
    @PostMapping("/add")
    public ResponseEntity<?> addMoney(
            @PathVariable String customerId,
            @RequestBody Map<String, Object> request) {
        try {
            double amount = Double.parseDouble(request.get("amount").toString());
            String description = (String) request.getOrDefault("description", "Wallet top-up");
            String referenceId = (String) request.getOrDefault("referenceId", "TOPUP-" + System.currentTimeMillis());
            
            Wallet wallet = walletService.addMoney(customerId, amount, description, referenceId, "topup");
            return ResponseEntity.ok(wallet);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Use wallet for payment
     */
    @PostMapping("/pay")
    public ResponseEntity<?> payUsingWallet(
            @PathVariable String customerId,
            @RequestBody Map<String, Object> request) {
        try {
            double amount = Double.parseDouble(request.get("amount").toString());
            String orderId = (String) request.get("orderId");
            
            if (orderId == null || orderId.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Order ID is required"));
            }
            
            Wallet wallet = walletService.payUsingWallet(customerId, amount, orderId);
            return ResponseEntity.ok(wallet);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Withdraw funds from wallet
     */
    @PostMapping("/withdraw")
    public ResponseEntity<?> withdrawFunds(
            @PathVariable String customerId,
            @RequestBody Map<String, Object> request) {
        try {
            double amount = Double.parseDouble(request.get("amount").toString());
            String method = (String) request.getOrDefault("method", "bank_transfer");

            Wallet wallet = walletService.withdrawFunds(customerId, amount, method);
            return ResponseEntity.ok(wallet);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Apply voucher/bonus to wallet
     */
    @PostMapping("/voucher/apply")
    public ResponseEntity<?> applyVoucher(
            @PathVariable String customerId,
            @RequestBody Map<String, Object> request) {
        try {
            String voucherCode = (String) request.get("voucherCode");
            if (voucherCode == null || voucherCode.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Voucher code is required"));
            }

            double bonusAmount = Double.parseDouble(request.get("bonusAmount").toString());
            Wallet wallet = walletService.addBonus(
                customerId,
                bonusAmount,
                voucherCode,
                "Voucher applied: " + voucherCode
            );
            return ResponseEntity.ok(wallet);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Check if sufficient balance exists
     */
    @GetMapping("/check-balance")
    public ResponseEntity<Map<String, Object>> checkBalance(
            @PathVariable String customerId,
            @RequestParam double amount) {
        boolean hasSufficientBalance = walletService.hasSufficientBalance(customerId, amount);
        double currentBalance = walletService.getBalance(customerId);
        
        return ResponseEntity.ok(Map.of(
            "hasSufficientBalance", hasSufficientBalance,
            "currentBalance", currentBalance,
            "requestedAmount", amount,
            "shortfall", hasSufficientBalance ? 0 : amount - currentBalance
        ));
    }

    /**
     * Get all transactions
     */
    @GetMapping("/transactions")
    public ResponseEntity<List<WalletTransaction>> getTransactions(@PathVariable String customerId) {
        List<WalletTransaction> transactions = walletService.getTransactionHistory(customerId);
        return ResponseEntity.ok(transactions);
    }

    /**
     * Get recent transactions
     */
    @GetMapping("/transactions/recent")
    public ResponseEntity<List<WalletTransaction>> getRecentTransactions(
            @PathVariable String customerId,
            @RequestParam(defaultValue = "10") int limit) {
        List<WalletTransaction> transactions = walletService.getRecentTransactions(customerId, limit);
        return ResponseEntity.ok(transactions);
    }

    /**
     * Get transactions by type
     */
    @GetMapping("/transactions/type/{type}")
    public ResponseEntity<List<WalletTransaction>> getTransactionsByType(
            @PathVariable String customerId,
            @PathVariable String type) {
        List<WalletTransaction> transactions = walletService.getTransactionsByType(customerId, type);
        return ResponseEntity.ok(transactions);
    }
}
