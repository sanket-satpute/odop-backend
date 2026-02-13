package com.odop.root.earnings.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for payout/transaction record
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayoutDto {
    
    private String payoutId;
    private String vendorId;
    private double amount;
    private String status; // PENDING, PROCESSING, COMPLETED, FAILED
    private String paymentMethod; // BANK_TRANSFER, UPI
    
    // Bank details (masked)
    private String bankName;
    private String accountNumberMasked; // ****1234
    private String ifscCode;
    private String upiId;
    
    // Dates
    private LocalDateTime requestedAt;
    private LocalDateTime processedAt;
    private LocalDateTime completedAt;
    
    // Reference
    private String transactionId;
    private String remarks;
}
