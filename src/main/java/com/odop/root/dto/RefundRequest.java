package com.odop.root.dto;

import lombok.Data;

/**
 * Request DTO for processing refunds.
 */
@Data
public class RefundRequest {
    
    private String paymentId;            // Our internal payment ID (or use razorpayPaymentId)
    private String razorpayPaymentId;    // Razorpay payment_id (pay_xxx)
    
    private double refundAmount;         // Amount to refund (in rupees). If 0, full refund.
    private String reason;               // Reason for refund
    
    // Speed of refund
    private String speed;                // "normal" or "optimum" (instant refund - may have charges)
    
    // Notes
    private String notes;
}
