package com.odop.root.variant.dto;

import com.odop.root.variant.model.ProductVariant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * DTO for product variant responses
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariantDto {
    
    private String id;
    private String productId;
    private String sku;
    
    private Map<String, String> attributes;
    private String displayName;
    
    // Pricing
    private double price;
    private double mrp;
    private int discountPercentage;
    
    // Stock
    private int stockQuantity;
    private int availableStock;
    private boolean inStock;
    private boolean lowStock;
    
    // Images
    private List<String> imageUrls;
    private String thumbnailUrl;
    
    // Status
    private boolean active;
    private boolean isDefault;
    
    // Display
    private int displayOrder;
    
    /**
     * Convert from entity
     */
    public static ProductVariantDto fromEntity(ProductVariant variant) {
        return ProductVariantDto.builder()
                .id(variant.getId())
                .productId(variant.getProductId())
                .sku(variant.getSku())
                .attributes(variant.getAttributes())
                .displayName(variant.getDisplayName())
                .price(variant.getPrice())
                .mrp(variant.getMrp())
                .discountPercentage(variant.getDiscountPercentage())
                .stockQuantity(variant.getStockQuantity())
                .availableStock(variant.getAvailableStock())
                .inStock(variant.isInStock())
                .lowStock(variant.isLowStock())
                .imageUrls(variant.getImageUrls())
                .thumbnailUrl(variant.getThumbnailUrl())
                .active(variant.isActive())
                .isDefault(variant.isDefault())
                .displayOrder(variant.getDisplayOrder())
                .build();
    }
}
