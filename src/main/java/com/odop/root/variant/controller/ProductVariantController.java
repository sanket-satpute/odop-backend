package com.odop.root.variant.controller;

import com.odop.root.variant.dto.*;
import com.odop.root.variant.model.VariantAttribute;
import com.odop.root.variant.service.ProductVariantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * REST Controller for product variants
 */
@RestController
@RequestMapping("/odop/variants")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ProductVariantController {
    
    private final ProductVariantService variantService;
    
    // ==================== VARIANT ENDPOINTS ====================
    
    /**
     * Create a new variant
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")
    public ResponseEntity<?> createVariant(@Valid @RequestBody CreateVariantRequest request) {
        try {
            ProductVariantDto variant = variantService.createVariant(request);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Variant created successfully",
                    "variant", variant
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Error creating variant", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Failed to create variant"
            ));
        }
    }
    
    /**
     * Update a variant
     */
    @PutMapping("/{variantId}")
    @PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")
    public ResponseEntity<?> updateVariant(
            @PathVariable String variantId,
            @Valid @RequestBody CreateVariantRequest request) {
        try {
            ProductVariantDto variant = variantService.updateVariant(variantId, request);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Variant updated successfully",
                    "variant", variant
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Error updating variant", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Failed to update variant"
            ));
        }
    }
    
    /**
     * Get variant by ID
     */
    @GetMapping("/{variantId}")
    public ResponseEntity<?> getVariant(@PathVariable String variantId) {
        ProductVariantDto variant = variantService.getVariant(variantId);
        if (variant == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(variant);
    }
    
    /**
     * Get variant by SKU
     */
    @GetMapping("/sku/{sku}")
    public ResponseEntity<?> getVariantBySku(@PathVariable String sku) {
        ProductVariantDto variant = variantService.getVariantBySku(sku);
        if (variant == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(variant);
    }
    
    /**
     * Get all variants for a product
     */
    @GetMapping("/product/{productId}")
    public ResponseEntity<?> getProductVariants(@PathVariable String productId) {
        List<ProductVariantDto> variants = variantService.getProductVariants(productId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "variants", variants,
                "count", variants.size()
        ));
    }
    
    /**
     * Get active variants for a product
     */
    @GetMapping("/product/{productId}/active")
    public ResponseEntity<?> getActiveProductVariants(@PathVariable String productId) {
        List<ProductVariantDto> variants = variantService.getActiveProductVariants(productId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "variants", variants,
                "count", variants.size()
        ));
    }
    
    /**
     * Get default variant for a product
     */
    @GetMapping("/product/{productId}/default")
    public ResponseEntity<?> getDefaultVariant(@PathVariable String productId) {
        ProductVariantDto variant = variantService.getDefaultVariant(productId);
        if (variant == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(variant);
    }
    
    /**
     * Delete variant
     */
    @DeleteMapping("/{variantId}")
    @PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")
    public ResponseEntity<?> deleteVariant(@PathVariable String variantId) {
        try {
            variantService.deleteVariant(variantId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Variant deleted successfully"
            ));
        } catch (Exception e) {
            log.error("Error deleting variant", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Failed to delete variant"
            ));
        }
    }
    
    // ==================== STOCK ENDPOINTS ====================
    
    /**
     * Update stock
     */
    @PostMapping("/stock/update")
    @PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")
    public ResponseEntity<?> updateStock(@Valid @RequestBody UpdateVariantStockRequest request) {
        try {
            ProductVariantDto variant = variantService.updateStock(request);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Stock updated successfully",
                    "variant", variant
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Check stock availability
     */
    @GetMapping("/{variantId}/stock/check")
    public ResponseEntity<?> checkStock(
            @PathVariable String variantId,
            @RequestParam int quantity) {
        boolean available = variantService.checkStockAvailability(variantId, quantity);
        return ResponseEntity.ok(Map.of(
                "available", available,
                "variantId", variantId,
                "requestedQuantity", quantity
        ));
    }
    
    /**
     * Get low stock variants
     */
    @GetMapping("/stock/low")
    @PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")
    public ResponseEntity<?> getLowStockVariants() {
        List<ProductVariantDto> variants = variantService.getLowStockVariants();
        return ResponseEntity.ok(Map.of(
                "success", true,
                "variants", variants,
                "count", variants.size()
        ));
    }
    
    // ==================== BULK OPERATIONS ====================
    
    /**
     * Create multiple variants
     */
    @PostMapping("/bulk/{productId}")
    @PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")
    public ResponseEntity<?> createBulkVariants(
            @PathVariable String productId,
            @RequestBody List<CreateVariantRequest> requests) {
        try {
            List<ProductVariantDto> variants = variantService.createBulkVariants(productId, requests);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Bulk variants created",
                    "variants", variants,
                    "count", variants.size()
            ));
        } catch (Exception e) {
            log.error("Error creating bulk variants", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Failed to create bulk variants"
            ));
        }
    }
    
    /**
     * Generate variants from attribute combinations
     */
    @PostMapping("/generate/{productId}")
    @PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")
    public ResponseEntity<?> generateVariants(
            @PathVariable String productId,
            @RequestBody GenerateVariantsRequest request) {
        try {
            List<ProductVariantDto> variants = variantService.generateVariantsFromAttributes(
                    productId,
                    request.getBasePrice(),
                    request.getBaseStock(),
                    request.getAttributeOptions()
            );
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Variants generated successfully",
                    "variants", variants,
                    "count", variants.size()
            ));
        } catch (Exception e) {
            log.error("Error generating variants", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Failed to generate variants"
            ));
        }
    }
    
    /**
     * Get available attribute values for a product
     */
    @GetMapping("/product/{productId}/available-attributes")
    public ResponseEntity<?> getAvailableAttributes(@PathVariable String productId) {
        Map<String, Set<String>> available = variantService.getAvailableAttributeValues(productId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "attributes", available
        ));
    }
    
    // ==================== ATTRIBUTE ENDPOINTS ====================
    
    /**
     * Get all variant attributes
     */
    @GetMapping("/attributes")
    public ResponseEntity<?> getAllAttributes() {
        List<VariantAttribute> attributes = variantService.getAllAttributes();
        return ResponseEntity.ok(Map.of(
                "success", true,
                "attributes", attributes
        ));
    }
    
    /**
     * Get attributes for category
     */
    @GetMapping("/attributes/category/{categoryId}")
    public ResponseEntity<?> getAttributesForCategory(@PathVariable String categoryId) {
        List<VariantAttribute> attributes = variantService.getAttributesForCategory(categoryId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "attributes", attributes
        ));
    }
    
    /**
     * Create/Update attribute
     */
    @PostMapping("/attributes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> saveAttribute(@RequestBody VariantAttribute attribute) {
        try {
            VariantAttribute saved = variantService.saveAttribute(attribute);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Attribute saved successfully",
                    "attribute", saved
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Initialize default attributes
     */
    @PostMapping("/attributes/init")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> initAttributes() {
        variantService.initializeDefaultAttributes();
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Default attributes initialized"
        ));
    }
    
    // ==================== DTO FOR GENERATE REQUEST ====================
    
    /**
     * Request DTO for generating variants
     */
    @lombok.Data
    public static class GenerateVariantsRequest {
        private double basePrice;
        private int baseStock;
        private Map<String, List<String>> attributeOptions;
    }
}
