package com.odop.root.variant.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * Request DTO for creating product variants
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateVariantRequest {
    
    @NotBlank(message = "Product ID is required")
    private String productId;
    
    private String sku;  // Optional - will be auto-generated if not provided
    
    @NotNull(message = "Attributes are required")
    private Map<String, String> attributes;
    
    @Min(value = 0, message = "Price must be positive")
    private double price;
    
    private double mrp;
    private double costPrice;
    
    @Min(value = 0, message = "Stock quantity must be non-negative")
    private int stockQuantity;
    
    private int lowStockThreshold;
    private boolean trackInventory;
    
    private List<String> imageUrls;
    private String thumbnailUrl;
    
    private double weight;
    private String weightUnit;
    
    private String barcode;
    private String barcodeType;
    
    private boolean active;
    private boolean isDefault;
    private int displayOrder;
}
