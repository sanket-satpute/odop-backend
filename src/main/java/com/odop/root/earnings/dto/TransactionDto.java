package com.odop.root.earnings.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for individual transaction records
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDto {
    
    private String transactionId;
    private String orderId;
    private String vendorId;
    
    private String type; // SALE, REFUND, PAYOUT, PLATFORM_FEE, ADJUSTMENT
    private double amount;
    private double netAmount; // after fees
    private double platformFee;
    
    private String status; // COMPLETED, PENDING, FAILED, REVERSED
    private String description;
    
    // Order details (for SALE type)
    private String customerName;
    private String productName;
    private int quantity;
    
    private LocalDateTime createdAt;
}
