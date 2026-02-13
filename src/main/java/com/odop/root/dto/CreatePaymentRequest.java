package com.odop.root.dto;

import lombok.Data;

/**
 * Request DTO for creating a new payment order.
 * Used when customer initiates checkout.
 */
@Data
public class CreatePaymentRequest {
    
    private String orderId;              // Reference to Order (optional - can create order first or after)
    private double amount;               // Amount in rupees (will be converted to paise for Razorpay)
    private String currency;             // Default: INR
    
    // Customer details
    private String customerId;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    
    // Vendor details (for multi-vendor tracking)
    private String vendorId;
    
    // Optional metadata
    private String description;          // Payment description
    private String notes;                // Additional notes
    
    // Default constructor
    public CreatePaymentRequest() {
        this.currency = "INR";
    }
}
