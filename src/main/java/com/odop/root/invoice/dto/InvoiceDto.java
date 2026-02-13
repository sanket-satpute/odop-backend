package com.odop.root.invoice.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for invoice response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceDto {
    
    private String invoiceId;
    private String invoiceNumber;
    private String orderId;
    
    // Customer
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String customerAddress;
    
    // Vendor
    private String vendorName;
    private String vendorShopName;
    
    // Items
    private List<InvoiceItemDto> items;
    
    // Amounts
    private Double subtotal;
    private Double discountAmount;
    private Double taxAmount;
    private Double shippingCharges;
    private Double grandTotal;
    
    // Status
    private String status;
    private String paymentStatus;
    
    // Dates
    private LocalDateTime invoiceDate;
    private LocalDateTime dueDate;
    
    // PDF
    private String pdfUrl;
    private boolean pdfAvailable;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InvoiceItemDto {
        private String productName;
        private String description;
        private Integer quantity;
        private Double unitPrice;
        private Double discount;
        private Double totalAmount;
    }
}
