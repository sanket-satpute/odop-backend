package com.odop.root.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for payment confirmation email
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentEmailRequest {
    
    private String customerEmail;
    private String customerName;
    private String orderId;
    private String paymentId;
    private String razorpayPaymentId;
    private BigDecimal amount;
    private String currency;
    private String paymentMethod;
    private String paymentStatus;
    private String transactionDate;
    private String receiptUrl;
}
