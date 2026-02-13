package com.odop.root.earnings.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for payout withdrawal
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayoutRequestDto {
    
    private String vendorId;
    private double amount;
    private String paymentMethod; // BANK_TRANSFER, UPI
    
    // Payment destination
    private String bankAccountId; // if bank transfer
    private String upiId; // if UPI
    
    private String remarks;
}
