package com.odop.root.variant.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Product Variant - represents different variations of a product (size, color, etc.)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "product_variants")
@CompoundIndex(name = "product_variant_idx", def = "{'productId': 1, 'sku': 1}", unique = true)
public class ProductVariant {
    
    @Id
    private String id;
    
    @Indexed
    private String productId;       // Reference to parent product
    
    @Indexed(unique = true)
    private String sku;             // Stock Keeping Unit - unique identifier
    
    // Variant attributes (size, color, material, etc.)
    private Map<String, String> attributes;  // e.g., {"size": "L", "color": "Red"}
    
    // Pricing
    private double price;           // Variant-specific price (can differ from base)
    private double mrp;             // Maximum Retail Price
    private double costPrice;       // Cost to vendor
    private double priceOffset;     // Offset from base price (+/-)
    
    // Inventory
    private int stockQuantity;
    private int reservedQuantity;   // Items in carts/pending orders
    private int lowStockThreshold;
    private boolean trackInventory;
    
    // Status
    private boolean active;
    private boolean isDefault;      // Default variant to show
    
    // Images specific to this variant
    private List<String> imageUrls;
    private String thumbnailUrl;
    
    // Weight and dimensions (can vary by variant)
    private double weight;          // in grams
    private String weightUnit;
    private double length;
    private double width;
    private double height;
    private String dimensionUnit;
    
    // Barcode
    private String barcode;
    private String barcodeType;     // UPC, EAN, etc.
    
    // Metadata
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Sort order
    private int displayOrder;
    
    /**
     * Get available stock (total - reserved)
     */
    public int getAvailableStock() {
        return stockQuantity - reservedQuantity;
    }
    
    /**
     * Check if in stock
     */
    public boolean isInStock() {
        return getAvailableStock() > 0;
    }
    
    /**
     * Check if low stock
     */
    public boolean isLowStock() {
        return trackInventory && getAvailableStock() <= lowStockThreshold;
    }
    
    /**
     * Get discount percentage
     */
    public int getDiscountPercentage() {
        if (mrp > 0 && price < mrp) {
            return (int) ((mrp - price) / mrp * 100);
        }
        return 0;
    }
    
    /**
     * Generate display name from attributes
     */
    public String getDisplayName() {
        if (attributes == null || attributes.isEmpty()) {
            return "Default";
        }
        return String.join(" / ", attributes.values());
    }
}
