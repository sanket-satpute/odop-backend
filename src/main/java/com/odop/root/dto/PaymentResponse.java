package com.odop.root.dto;

import java.time.LocalDateTime;

import com.odop.root.models.PaymentStatus;

import lombok.Data;

/**
 * Response DTO for payment operations.
 * Contains all necessary info for frontend to process payment.
 */
@Data
public class PaymentResponse {
    
    // Our payment record
    private String paymentId;            // Internal payment ID
    private String orderId;              // Reference to Order
    
    // Razorpay order details (needed for checkout)
    private String razorpayOrderId;      // order_xxx - use this for Razorpay checkout
    private String razorpayPaymentId;    // pay_xxx - available after payment
    
    // Amount details
    private double amount;               // Amount in rupees
    private int amountInPaise;           // Amount in paise (for Razorpay)
    private String currency;
    
    // Status
    private PaymentStatus status;
    private String statusMessage;
    
    // Razorpay key (needed for frontend checkout initialization)
    private String razorpayKeyId;        // Public key for frontend
    
    // Customer info (for pre-filling checkout)
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    
    // Receipt
    private String receiptNumber;
    private String description;
    
    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    
    // Refund info (if applicable)
    private String refundId;
    private double refundAmount;
    private String refundReason;
    
    // Error info (if payment failed)
    private String errorCode;
    private String errorDescription;
    
    // Verification result
    private boolean verified;
    private String verificationMessage;
    
    // Helper method
    public static PaymentResponse success(String message) {
        PaymentResponse response = new PaymentResponse();
        response.setStatus(PaymentStatus.SUCCESS);
        response.setStatusMessage(message);
        response.setVerified(true);
        return response;
    }
    
    public static PaymentResponse error(String message) {
        PaymentResponse response = new PaymentResponse();
        response.setStatus(PaymentStatus.FAILED);
        response.setStatusMessage(message);
        response.setVerified(false);
        return response;
    }
}
