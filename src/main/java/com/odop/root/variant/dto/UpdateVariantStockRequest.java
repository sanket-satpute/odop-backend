package com.odop.root.variant.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Request DTO for updating variant stock
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateVariantStockRequest {
    
    private String variantId;
    private int quantity;       // New quantity or adjustment
    private boolean isAbsolute; // true = set to quantity, false = adjust by quantity
    private String reason;      // Reason for stock update
}
