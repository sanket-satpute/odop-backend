package com.odop.root.models;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Order entity for ODOP e-commerce platform.
 * Tracks order lifecycle from creation to delivery.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "orders")
public class Order {

    @Id
    private String orderId;
    private String customerId;          // reference to Customer
    private String vendorId;            // reference to Vendor
    private List<OrderItem> orderItems; // embedded order items
    private double totalAmount;
    private double discountAmount;
    private double deliveryCharges;
    private double finalAmount;         // totalAmount - discountAmount + deliveryCharges
    
    // Shipping Address
    private String shippingAddress;
    private String shippingDistrict;
    private String shippingState;
    private String shippingPinCode;
    private long shippingContactNumber;
    
    // Order Status
    private String orderStatus;         // PENDING, CONFIRMED, PROCESSING, SHIPPED, DELIVERED, CANCELLED, RETURNED
    private String paymentStatus;       // PENDING, PAID, FAILED, REFUNDED
    private String paymentMethod;       // COD, UPI, CARD, NET_BANKING
    private String paymentTransactionId;
    
    // Tracking
    private String trackingNumber;
    private String courierPartner;
    private LocalDateTime estimatedDeliveryDate;
    private LocalDateTime actualDeliveryDate;
    
    // Notes
    private String customerNotes;
    private String vendorNotes;
    private String cancellationReason;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
