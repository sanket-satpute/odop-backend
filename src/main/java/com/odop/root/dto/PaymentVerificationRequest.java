package com.odop.root.dto;

import lombok.Data;

/**
 * Request DTO for verifying payment after completion.
 * Razorpay sends these values to the frontend which must be verified on backend.
 */
@Data
public class PaymentVerificationRequest {
    
    private String razorpayOrderId;      // order_xxx - from createOrder response
    private String razorpayPaymentId;    // pay_xxx - received after successful payment
    private String razorpaySignature;    // Signature to verify authenticity
    
    // Optional: Order ID from our system to update order status
    private String orderId;
}
