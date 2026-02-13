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
 * Cart entity for managing customer shopping carts.
 * Supports multi-vendor carts with CartItem embedded documents.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "carts")
public class Cart {

    @Id
    private String cartId;
    private String customerId;              // reference to Customer
    private List<CartItem> cartItems;       // embedded cart items (supports multi-vendor)
    @Builder.Default
    private double totalAmount = 0;
    @Builder.Default
    private int totalItems = 0;
    @Builder.Default
    private String status = "ACTIVE";       // ACTIVE, ABANDONED, CONVERTED
    
    // Legacy fields - kept for backward compatibility
    @Deprecated
    private String vendorId;                // use cartItems.vendorId instead
    @Deprecated
    private List<String> productIds;        // use cartItems instead
    @Deprecated
    private List<Integer> quantities;       // use cartItems instead
    @Deprecated
    private boolean approval;
    
    // Single item fields for simple cart operations (used by frontend)
    private String productId;               // single product ID for simple carts
    private int quantity;                   // quantity for single product cart
    private String time;                    // timestamp string
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Convenience constructor for new cart.
     */
    public Cart(String customerId) {
        this.customerId = customerId;
        this.status = "ACTIVE";
        this.totalAmount = 0;
        this.totalItems = 0;
    }

    /**
     * Helper method to recalculate totals from cart items.
     */
    public void recalculateTotals() {
        if (cartItems == null || cartItems.isEmpty()) {
            this.totalAmount = 0;
            this.totalItems = 0;
            return;
        }
        this.totalAmount = cartItems.stream()
                .filter(CartItem::isSelected)
                .mapToDouble(CartItem::getItemTotal)
                .sum();
        this.totalItems = cartItems.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }
}
