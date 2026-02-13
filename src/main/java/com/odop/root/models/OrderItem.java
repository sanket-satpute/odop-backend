package com.odop.root.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Embedded document for Order - represents individual items in an order.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    private String productId;           // reference to Products
    private String productName;         // denormalized for order history
    private String productImageURL;     // denormalized for order history
    private int quantity;
    private double unitPrice;
    private double discount;
    private double totalPrice;          // (unitPrice * quantity) - discount

    /**
     * Convenience constructor that auto-calculates total price.
     */
    public OrderItem(String productId, String productName, String productImageURL, 
                     int quantity, double unitPrice, double discount) {
        this.productId = productId;
        this.productName = productName;
        this.productImageURL = productImageURL;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.discount = discount;
        this.totalPrice = (unitPrice * quantity) - discount;
    }
}
