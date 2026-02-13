package com.odop.root.bulkupload.dto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * DTO for bulk upload request
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkUploadRequest {
    
    // Type of upload - PRODUCTS, VARIANTS, PRICE_UPDATE, STOCK_UPDATE
    private String uploadType;
    
    // Column mapping from CSV to entity fields
    // e.g., {"Name": "productName", "Price": "price", "Stock": "stockQuantity"}
    private Map<String, String> columnMapping;
    
    // Options
    @Builder.Default
    private boolean updateExisting = false;  // Update if product exists
    
    @Builder.Default
    private boolean skipInvalid = true;      // Skip invalid rows instead of failing
    
    @Builder.Default
    private boolean generateSkus = true;     // Auto-generate SKUs if not provided
    
    @Builder.Default
    private String delimiter = ",";          // CSV delimiter
    
    @Builder.Default
    private boolean hasHeader = true;        // First row is header
    
    // Category ID for all products (optional)
    private String defaultCategoryId;
    
    // Default values for missing fields
    private Map<String, Object> defaultValues;
}
