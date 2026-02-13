package com.odop.root.invoice.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Invoice model for storing generated invoices.
 */
@Document(collection = "invoices")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice {

    @Id
    private String invoiceId;
    
    // Invoice Number (human-readable)
    private String invoiceNumber;           // e.g., "ODOP-INV-2026-00001"
    
    // Order Reference
    private String orderId;
    
    // Customer Details
    private String customerId;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String customerAddress;
    private String customerCity;
    private String customerState;
    private String customerPinCode;
    private String customerGstin;           // GST Number if business customer
    
    // Vendor Details
    private String vendorId;
    private String vendorName;
    private String vendorShopName;
    private String vendorAddress;
    private String vendorCity;
    private String vendorState;
    private String vendorPinCode;
    private String vendorGstin;
    private String vendorPan;
    
    // Company Details (ODOP Platform)
    private String companyName;
    private String companyAddress;
    private String companyGstin;
    private String companyPan;
    
    // Invoice Items
    private List<InvoiceItem> items;
    
    // Amounts
    private Double subtotal;
    private Double discountAmount;
    private String discountCode;
    private Double taxableAmount;
    
    // GST Breakdown
    private Double cgstRate;                // Central GST Rate
    private Double cgstAmount;              // Central GST Amount
    private Double sgstRate;                // State GST Rate
    private Double sgstAmount;              // State GST Amount
    private Double igstRate;                // Integrated GST Rate (inter-state)
    private Double igstAmount;              // Integrated GST Amount
    private Double totalGst;
    
    private Double shippingCharges;
    private Double grandTotal;
    
    // Payment Info
    private String paymentMethod;
    private String paymentStatus;
    private String transactionId;
    private LocalDateTime paymentDate;
    
    // Invoice Metadata
    private LocalDateTime invoiceDate;
    private LocalDateTime dueDate;
    private String status;                  // DRAFT, GENERATED, SENT, PAID, CANCELLED
    private String pdfUrl;                  // Cloud URL if stored
    
    // Supply Type (for GST)
    private String supplyType;              // INTRA_STATE, INTER_STATE
    private String placeOfSupply;
    
    // Notes
    private String customerNotes;
    private String internalNotes;
    private String termsAndConditions;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * Invoice line item
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InvoiceItem {
        private String productId;
        private String productName;
        private String description;
        private String hsnCode;             // HSN/SAC Code for GST
        private Integer quantity;
        private Double unitPrice;
        private Double discount;
        private Double taxableValue;
        private Double cgstRate;
        private Double cgstAmount;
        private Double sgstRate;
        private Double sgstAmount;
        private Double igstRate;
        private Double igstAmount;
        private Double totalAmount;
    }
}
