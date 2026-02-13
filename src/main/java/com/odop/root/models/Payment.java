package com.odop.root.models;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Payment entity for storing payment transaction details.
 * Integrated with Razorpay Payment Gateway.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "payments")
public class Payment {

    @Id
    private String paymentId;               // Internal payment ID
    
    // Razorpay specific fields
    private String razorpayOrderId;         // Razorpay order_id (order_xxx)
    private String razorpayPaymentId;       // Razorpay payment_id (pay_xxx) - set after payment
    private String razorpaySignature;       // Signature for verification
    
    // Order reference
    private String orderId;                 // Reference to Order collection
    private String customerId;              // Reference to Customer
    private String vendorId;                // Reference to Vendor (for vendor payouts later)
    
    // Amount details (in paise for Razorpay, stored as rupees)
    private double amount;                  // Total amount in rupees
    @Builder.Default
    private String currency = "INR";        // INR, USD, etc.
    
    // Payment details
    @Builder.Default
    private PaymentStatus status = PaymentStatus.CREATED; // CREATED, SUCCESS, FAILED, REFUNDED
    private String paymentMethod;           // card, upi, netbanking, wallet
    private String paymentMethodDetails;    // Last 4 digits of card, UPI ID, bank name, etc.
    
    // Customer info (for receipts)
    private String customerEmail;
    private String customerPhone;
    private String customerName;
    
    // Refund details
    private String refundId;                // Razorpay refund_id if refunded
    private double refundAmount;
    private String refundReason;
    private LocalDateTime refundedAt;
    
    // Metadata
    private String description;             // Payment description
    private String receiptNumber;           // Unique receipt for this payment
    private String notes;                   // Additional notes
    
    // Error handling
    private String errorCode;               // Razorpay error code if failed
    private String errorDescription;        // Error description
    private String errorSource;             // Error source
    private String errorStep;               // Step where error occurred
    private String errorReason;             // Reason for failure
    
    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime completedAt;      // When payment was successful
    
    // For webhook tracking
    @Builder.Default
    private boolean webhookReceived = false;
    private LocalDateTime webhookReceivedAt;

    /**
     * Custom setter for status that automatically updates updatedAt timestamp.
     */
    public void setStatus(PaymentStatus status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Convenience constructor with timestamp initialization.
     */
    public Payment(String orderId, String customerId, double amount) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.amount = amount;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = PaymentStatus.CREATED;
        this.currency = "INR";
        this.webhookReceived = false;
    }
}
