package com.odop.root.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Embedded document for Cart - represents individual items in a cart.
 * This allows for better structure than parallel arrays (productIds[], quantities[]).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {

    private String productId;           // reference to Products
    private String vendorId;            // reference to Vendor (for multi-vendor cart)
    private String productName;         // denormalized for quick display
    private String productImageURL;     // denormalized for quick display
    private int quantity;
    private double unitPrice;
    private double discount;
    @Builder.Default
    private boolean selected = true;    // for selective checkout

    /**
     * Helper method to calculate item total.
     */
    public double getItemTotal() {
        return (unitPrice * quantity) - discount;
    }
}
