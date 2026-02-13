package com.odop.root.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderDto {
    private String orderId;
    private String customerId;
    private String vendorId;
    private List<OrderItemDto> orderItems;
    private double totalAmount;
    private double discountAmount;
    private double deliveryCharges;
    private double finalAmount;
    
    // Shipping Address
    private String shippingAddress;
    private String shippingDistrict;
    private String shippingState;
    private String shippingPinCode;
    private long shippingContactNumber;
    
    // Order Status
    private String orderStatus;
    private String paymentStatus;
    private String paymentMethod;
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
