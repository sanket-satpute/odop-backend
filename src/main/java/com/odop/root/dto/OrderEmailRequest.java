package com.odop.root.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for order confirmation email
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderEmailRequest {
    
    private String customerEmail;
    private String customerName;
    private String orderId;
    private String orderDate;
    private List<OrderItemDetail> items;
    private BigDecimal subtotal;
    private BigDecimal shippingCost;
    private BigDecimal tax;
    private BigDecimal totalAmount;
    private String paymentMethod;
    private String paymentStatus;
    private ShippingAddress shippingAddress;
    private String estimatedDelivery;
    private String trackingNumber;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderItemDetail {
        private String productName;
        private String productImage;
        private int quantity;
        private BigDecimal unitPrice;
        private BigDecimal totalPrice;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ShippingAddress {
        private String fullName;
        private String addressLine1;
        private String addressLine2;
        private String city;
        private String state;
        private String postalCode;
        private String country;
        private String phone;
    }
}
