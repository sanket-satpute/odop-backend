package com.odop.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;

@Document(collection = "wallets")
public class Wallet {

    @Id
    private String walletId;

    @Indexed(unique = true)
    private String customerId;

    private double balance;
    private String currency = "INR";
    
    private boolean isActive = true;
    private boolean isLocked = false;
    private String lockReason;

    private List<WalletTransaction> transactions = new ArrayList<>();

    private Date createdAt;
    private Date updatedAt;

    // Nested class for wallet transactions
    public static class WalletTransaction {
        private String transactionId;
        private String type; // credit, debit, refund, cashback, bonus
        private double amount;
        private double balanceAfter;
        private String description;
        private String referenceId; // order ID, payment ID, etc.
        private String referenceType; // order, refund, promotion, etc.
        private String status; // pending, completed, failed, reversed
        private Date timestamp;

        public WalletTransaction() {
            this.timestamp = new Date();
            this.status = "completed";
        }

        // Getters and Setters
        public String getTransactionId() {
            return transactionId;
        }

        public void setTransactionId(String transactionId) {
            this.transactionId = transactionId;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public double getAmount() {
            return amount;
        }

        public void setAmount(double amount) {
            this.amount = amount;
        }

        public double getBalanceAfter() {
            return balanceAfter;
        }

        public void setBalanceAfter(double balanceAfter) {
            this.balanceAfter = balanceAfter;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getReferenceId() {
            return referenceId;
        }

        public void setReferenceId(String referenceId) {
            this.referenceId = referenceId;
        }

        public String getReferenceType() {
            return referenceType;
        }

        public void setReferenceType(String referenceType) {
            this.referenceType = referenceType;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public Date getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(Date timestamp) {
            this.timestamp = timestamp;
        }
    }

    // Constructor
    public Wallet() {
        this.balance = 0.0;
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    public Wallet(String customerId) {
        this();
        this.customerId = customerId;
    }

    // Pre-save hook
    public void preSave() {
        this.updatedAt = new Date();
        if (this.createdAt == null) {
            this.createdAt = new Date();
        }
    }

    // Getters and Setters
    public String getWalletId() {
        return walletId;
    }

    public void setWalletId(String walletId) {
        this.walletId = walletId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean isLocked) {
        this.isLocked = isLocked;
    }

    public String getLockReason() {
        return lockReason;
    }

    public void setLockReason(String lockReason) {
        this.lockReason = lockReason;
    }

    public List<WalletTransaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<WalletTransaction> transactions) {
        this.transactions = transactions;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Helper methods
    public WalletTransaction addCredit(double amount, String description, String referenceId, String referenceType) {
        if (this.isLocked) {
            throw new IllegalStateException("Wallet is locked: " + this.lockReason);
        }
        
        WalletTransaction transaction = new WalletTransaction();
        transaction.setTransactionId("TXN-" + System.currentTimeMillis());
        transaction.setType("credit");
        transaction.setAmount(amount);
        transaction.setDescription(description);
        transaction.setReferenceId(referenceId);
        transaction.setReferenceType(referenceType);
        
        this.balance += amount;
        transaction.setBalanceAfter(this.balance);
        
        if (this.transactions == null) {
            this.transactions = new ArrayList<>();
        }
        this.transactions.add(0, transaction); // Add to front for recent first
        this.updatedAt = new Date();
        
        return transaction;
    }

    public WalletTransaction addDebit(double amount, String description, String referenceId, String referenceType) {
        if (this.isLocked) {
            throw new IllegalStateException("Wallet is locked: " + this.lockReason);
        }
        if (this.balance < amount) {
            throw new IllegalStateException("Insufficient balance");
        }
        
        WalletTransaction transaction = new WalletTransaction();
        transaction.setTransactionId("TXN-" + System.currentTimeMillis());
        transaction.setType("debit");
        transaction.setAmount(amount);
        transaction.setDescription(description);
        transaction.setReferenceId(referenceId);
        transaction.setReferenceType(referenceType);
        
        this.balance -= amount;
        transaction.setBalanceAfter(this.balance);
        
        if (this.transactions == null) {
            this.transactions = new ArrayList<>();
        }
        this.transactions.add(0, transaction);
        this.updatedAt = new Date();
        
        return transaction;
    }

    public List<WalletTransaction> getRecentTransactions(int limit) {
        if (this.transactions == null || this.transactions.isEmpty()) {
            return new ArrayList<>();
        }
        return this.transactions.subList(0, Math.min(limit, this.transactions.size()));
    }
}
