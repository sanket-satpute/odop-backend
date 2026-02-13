package com.odop.service;

import com.odop.model.Wallet;
import com.odop.model.Wallet.WalletTransaction;
import com.odop.repository.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class WalletService {

    @Autowired
    private WalletRepository walletRepository;

    /**
     * Get or create wallet for a customer
     */
    public Wallet getOrCreateWallet(String customerId) {
        Optional<Wallet> existingWallet = walletRepository.findByCustomerId(customerId);
        if (existingWallet.isPresent()) {
            return existingWallet.get();
        }
        
        // Create new wallet
        Wallet wallet = new Wallet(customerId);
        return walletRepository.save(wallet);
    }

    /**
     * Get wallet by customer ID
     */
    public Optional<Wallet> getWallet(String customerId) {
        return walletRepository.findByCustomerId(customerId);
    }

    /**
     * Get wallet balance
     */
    public double getBalance(String customerId) {
        Optional<Wallet> wallet = walletRepository.findByCustomerId(customerId);
        return wallet.map(Wallet::getBalance).orElse(0.0);
    }

    /**
     * Add money to wallet (credit)
     */
    public Wallet addMoney(String customerId, double amount, String description, String referenceId, String referenceType) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        
        Wallet wallet = getOrCreateWallet(customerId);
        wallet.addCredit(amount, description, referenceId, referenceType);
        wallet.preSave();
        return walletRepository.save(wallet);
    }

    /**
     * Deduct money from wallet (debit)
     */
    public Wallet deductMoney(String customerId, double amount, String description, String referenceId, String referenceType) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        
        Optional<Wallet> walletOpt = walletRepository.findByCustomerId(customerId);
        if (walletOpt.isEmpty()) {
            throw new IllegalArgumentException("Wallet not found");
        }
        
        Wallet wallet = walletOpt.get();
        wallet.addDebit(amount, description, referenceId, referenceType);
        wallet.preSave();
        return walletRepository.save(wallet);
    }

    /**
     * Process refund to wallet
     */
    public Wallet processRefund(String customerId, double amount, String orderId, String description) {
        return addMoney(customerId, amount, description, orderId, "refund");
    }

    /**
     * Add cashback to wallet
     */
    public Wallet addCashback(String customerId, double amount, String orderId, String description) {
        return addMoney(customerId, amount, description, orderId, "cashback");
    }

    /**
     * Add promotional bonus
     */
    public Wallet addBonus(String customerId, double amount, String promoId, String description) {
        return addMoney(customerId, amount, description, promoId, "bonus");
    }

    /**
     * Pay using wallet
     */
    public Wallet payUsingWallet(String customerId, double amount, String orderId) {
        return deductMoney(customerId, amount, "Payment for order #" + orderId, orderId, "order");
    }

    /**
     * Withdraw funds from wallet
     */
    public Wallet withdrawFunds(String customerId, double amount, String method) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive");
        }
        String withdrawalMethod = (method == null || method.isBlank()) ? "bank_transfer" : method;
        String referenceId = "WD-" + System.currentTimeMillis();
        String description = "Withdrawal via " + withdrawalMethod;
        return deductMoney(customerId, amount, description, referenceId, "withdrawal");
    }

    /**
     * Check if customer has sufficient balance
     */
    public boolean hasSufficientBalance(String customerId, double amount) {
        return getBalance(customerId) >= amount;
    }

    /**
     * Get transaction history
     */
    public List<WalletTransaction> getTransactionHistory(String customerId) {
        Optional<Wallet> wallet = walletRepository.findByCustomerId(customerId);
        if (wallet.isPresent() && wallet.get().getTransactions() != null) {
            return wallet.get().getTransactions();
        }
        return List.of();
    }

    /**
     * Get recent transactions
     */
    public List<WalletTransaction> getRecentTransactions(String customerId, int limit) {
        Optional<Wallet> wallet = walletRepository.findByCustomerId(customerId);
        if (wallet.isPresent()) {
            return wallet.get().getRecentTransactions(limit);
        }
        return List.of();
    }

    /**
     * Get transactions by type
     */
    public List<WalletTransaction> getTransactionsByType(String customerId, String type) {
        Optional<Wallet> wallet = walletRepository.findByCustomerId(customerId);
        if (wallet.isPresent() && wallet.get().getTransactions() != null) {
            return wallet.get().getTransactions().stream()
                .filter(t -> type.equals(t.getType()))
                .collect(Collectors.toList());
        }
        return List.of();
    }

    /**
     * Get wallet summary/statistics
     */
    public Map<String, Object> getWalletSummary(String customerId) {
        Map<String, Object> summary = new HashMap<>();
        
        Optional<Wallet> walletOpt = walletRepository.findByCustomerId(customerId);
        if (walletOpt.isEmpty()) {
            summary.put("exists", false);
            summary.put("balance", 0.0);
            return summary;
        }
        
        Wallet wallet = walletOpt.get();
        List<WalletTransaction> transactions = wallet.getTransactions();
        
        summary.put("exists", true);
        summary.put("balance", wallet.getBalance());
        summary.put("currency", wallet.getCurrency());
        summary.put("isActive", wallet.isActive());
        summary.put("isLocked", wallet.isLocked());
        summary.put("totalTransactions", transactions != null ? transactions.size() : 0);
        
        // Calculate totals
        double totalCredits = 0;
        double totalDebits = 0;
        double totalRefunds = 0;
        double totalCashback = 0;
        
        if (transactions != null) {
            for (WalletTransaction t : transactions) {
                switch (t.getType()) {
                    case "credit":
                        totalCredits += t.getAmount();
                        break;
                    case "debit":
                        totalDebits += t.getAmount();
                        break;
                    case "refund":
                        totalRefunds += t.getAmount();
                        break;
                    case "cashback":
                        totalCashback += t.getAmount();
                        break;
                }
            }
        }
        
        summary.put("totalCredits", totalCredits);
        summary.put("totalDebits", totalDebits);
        summary.put("totalRefunds", totalRefunds);
        summary.put("totalCashback", totalCashback);
        
        return summary;
    }

    /**
     * Lock wallet
     */
    public Wallet lockWallet(String customerId, String reason) {
        Optional<Wallet> walletOpt = walletRepository.findByCustomerId(customerId);
        if (walletOpt.isEmpty()) {
            throw new IllegalArgumentException("Wallet not found");
        }
        
        Wallet wallet = walletOpt.get();
        wallet.setLocked(true);
        wallet.setLockReason(reason);
        wallet.preSave();
        return walletRepository.save(wallet);
    }

    /**
     * Unlock wallet
     */
    public Wallet unlockWallet(String customerId) {
        Optional<Wallet> walletOpt = walletRepository.findByCustomerId(customerId);
        if (walletOpt.isEmpty()) {
            throw new IllegalArgumentException("Wallet not found");
        }
        
        Wallet wallet = walletOpt.get();
        wallet.setLocked(false);
        wallet.setLockReason(null);
        wallet.preSave();
        return walletRepository.save(wallet);
    }

    /**
     * Deactivate wallet
     */
    public Wallet deactivateWallet(String customerId) {
        Optional<Wallet> walletOpt = walletRepository.findByCustomerId(customerId);
        if (walletOpt.isEmpty()) {
            throw new IllegalArgumentException("Wallet not found");
        }
        
        Wallet wallet = walletOpt.get();
        wallet.setActive(false);
        wallet.preSave();
        return walletRepository.save(wallet);
    }

    /**
     * Reactivate wallet
     */
    public Wallet reactivateWallet(String customerId) {
        Optional<Wallet> walletOpt = walletRepository.findByCustomerId(customerId);
        if (walletOpt.isEmpty()) {
            throw new IllegalArgumentException("Wallet not found");
        }
        
        Wallet wallet = walletOpt.get();
        wallet.setActive(true);
        wallet.preSave();
        return walletRepository.save(wallet);
    }
}
