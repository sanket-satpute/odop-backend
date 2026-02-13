package com.odop.root.variant.service;

import com.odop.root.variant.dto.*;
import com.odop.root.variant.model.ProductVariant;
import com.odop.root.variant.model.VariantAttribute;
import com.odop.root.variant.repository.ProductVariantRepository;
import com.odop.root.variant.repository.VariantAttributeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing product variants
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductVariantService {
    
    private final ProductVariantRepository variantRepository;
    private final VariantAttributeRepository attributeRepository;
    
    // ==================== VARIANT CRUD ====================
    
    /**
     * Create a new variant for a product
     */
    public ProductVariantDto createVariant(CreateVariantRequest request) {
        log.info("Creating variant for product: {}", request.getProductId());
        
        // Generate SKU if not provided
        String sku = request.getSku();
        if (sku == null || sku.isEmpty()) {
            sku = generateSku(request.getProductId(), request.getAttributes());
        }
        
        // Check for duplicate SKU
        if (variantRepository.existsBySku(sku)) {
            throw new IllegalArgumentException("SKU already exists: " + sku);
        }
        
        // Check for duplicate attributes
        Optional<ProductVariant> existingWithAttributes = variantRepository
                .findByProductIdAndAttributes(request.getProductId(), request.getAttributes());
        
        if (existingWithAttributes.isPresent()) {
            throw new IllegalArgumentException("Variant with these attributes already exists");
        }
        
        ProductVariant variant = ProductVariant.builder()
                .productId(request.getProductId())
                .sku(sku)
                .attributes(request.getAttributes())
                .price(request.getPrice())
                .mrp(request.getMrp() > 0 ? request.getMrp() : request.getPrice())
                .costPrice(request.getCostPrice())
                .stockQuantity(request.getStockQuantity())
                .reservedQuantity(0)
                .lowStockThreshold(request.getLowStockThreshold() > 0 ? request.getLowStockThreshold() : 5)
                .trackInventory(request.isTrackInventory())
                .active(request.isActive())
                .isDefault(request.isDefault())
                .imageUrls(request.getImageUrls())
                .thumbnailUrl(request.getThumbnailUrl())
                .weight(request.getWeight())
                .weightUnit(request.getWeightUnit())
                .barcode(request.getBarcode())
                .barcodeType(request.getBarcodeType())
                .displayOrder(request.getDisplayOrder())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        // If this is marked as default, unset other defaults
        if (variant.isDefault()) {
            unsetDefaultVariant(request.getProductId());
        }
        
        // If this is the first variant, make it default
        if (variantRepository.countByProductId(request.getProductId()) == 0) {
            variant.setDefault(true);
        }
        
        variant = variantRepository.save(variant);
        log.info("Created variant with ID: {} SKU: {}", variant.getId(), variant.getSku());
        
        return ProductVariantDto.fromEntity(variant);
    }
    
    /**
     * Update a variant
     */
    public ProductVariantDto updateVariant(String variantId, CreateVariantRequest request) {
        log.info("Updating variant: {}", variantId);
        
        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new IllegalArgumentException("Variant not found: " + variantId));
        
        // Update fields
        if (request.getAttributes() != null) {
            variant.setAttributes(request.getAttributes());
        }
        if (request.getPrice() > 0) {
            variant.setPrice(request.getPrice());
        }
        if (request.getMrp() > 0) {
            variant.setMrp(request.getMrp());
        }
        variant.setCostPrice(request.getCostPrice());
        variant.setStockQuantity(request.getStockQuantity());
        variant.setLowStockThreshold(request.getLowStockThreshold());
        variant.setTrackInventory(request.isTrackInventory());
        variant.setActive(request.isActive());
        
        if (request.getImageUrls() != null) {
            variant.setImageUrls(request.getImageUrls());
        }
        if (request.getThumbnailUrl() != null) {
            variant.setThumbnailUrl(request.getThumbnailUrl());
        }
        
        variant.setWeight(request.getWeight());
        variant.setWeightUnit(request.getWeightUnit());
        variant.setBarcode(request.getBarcode());
        variant.setBarcodeType(request.getBarcodeType());
        variant.setDisplayOrder(request.getDisplayOrder());
        variant.setUpdatedAt(LocalDateTime.now());
        
        // Handle default flag
        if (request.isDefault() && !variant.isDefault()) {
            unsetDefaultVariant(variant.getProductId());
            variant.setDefault(true);
        }
        
        variant = variantRepository.save(variant);
        return ProductVariantDto.fromEntity(variant);
    }
    
    /**
     * Get variant by ID
     */
    public ProductVariantDto getVariant(String variantId) {
        return variantRepository.findById(variantId)
                .map(ProductVariantDto::fromEntity)
                .orElse(null);
    }
    
    /**
     * Get variant by SKU
     */
    public ProductVariantDto getVariantBySku(String sku) {
        return variantRepository.findBySku(sku)
                .map(ProductVariantDto::fromEntity)
                .orElse(null);
    }
    
    /**
     * Get all variants for a product
     */
    public List<ProductVariantDto> getProductVariants(String productId) {
        return variantRepository.findByProductIdOrderByDisplayOrderAsc(productId)
                .stream()
                .map(ProductVariantDto::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * Get active variants for a product
     */
    public List<ProductVariantDto> getActiveProductVariants(String productId) {
        return variantRepository.findByProductIdAndActiveTrue(productId)
                .stream()
                .map(ProductVariantDto::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * Get default variant for a product
     */
    public ProductVariantDto getDefaultVariant(String productId) {
        return variantRepository.findByProductIdAndIsDefaultTrue(productId)
                .map(ProductVariantDto::fromEntity)
                .orElseGet(() -> {
                    // Return first active variant if no default
                    List<ProductVariant> variants = variantRepository.findByProductIdAndActiveTrue(productId);
                    if (!variants.isEmpty()) {
                        return ProductVariantDto.fromEntity(variants.get(0));
                    }
                    return null;
                });
    }
    
    /**
     * Delete variant
     */
    public void deleteVariant(String variantId) {
        log.info("Deleting variant: {}", variantId);
        
        ProductVariant variant = variantRepository.findById(variantId).orElse(null);
        if (variant == null) {
            return;
        }
        
        boolean wasDefault = variant.isDefault();
        String productId = variant.getProductId();
        
        variantRepository.deleteById(variantId);
        
        // If deleted was default, set another as default
        if (wasDefault) {
            List<ProductVariant> remaining = variantRepository.findByProductIdAndActiveTrue(productId);
            if (!remaining.isEmpty()) {
                ProductVariant newDefault = remaining.get(0);
                newDefault.setDefault(true);
                variantRepository.save(newDefault);
            }
        }
    }
    
    /**
     * Delete all variants for a product
     */
    public void deleteProductVariants(String productId) {
        log.info("Deleting all variants for product: {}", productId);
        variantRepository.deleteByProductId(productId);
    }
    
    // ==================== STOCK MANAGEMENT ====================
    
    /**
     * Update variant stock
     */
    @Transactional
    public ProductVariantDto updateStock(UpdateVariantStockRequest request) {
        log.info("Updating stock for variant: {} quantity: {} absolute: {}", 
                request.getVariantId(), request.getQuantity(), request.isAbsolute());
        
        ProductVariant variant = variantRepository.findById(request.getVariantId())
                .orElseThrow(() -> new IllegalArgumentException("Variant not found"));
        
        if (request.isAbsolute()) {
            variant.setStockQuantity(Math.max(0, request.getQuantity()));
        } else {
            variant.setStockQuantity(Math.max(0, variant.getStockQuantity() + request.getQuantity()));
        }
        
        variant.setUpdatedAt(LocalDateTime.now());
        variant = variantRepository.save(variant);
        
        return ProductVariantDto.fromEntity(variant);
    }
    
    /**
     * Reserve stock (when item added to cart or order placed)
     */
    @Transactional
    public boolean reserveStock(String variantId, int quantity) {
        ProductVariant variant = variantRepository.findById(variantId).orElse(null);
        if (variant == null) {
            return false;
        }
        
        if (variant.getAvailableStock() < quantity) {
            return false;
        }
        
        variant.setReservedQuantity(variant.getReservedQuantity() + quantity);
        variant.setUpdatedAt(LocalDateTime.now());
        variantRepository.save(variant);
        
        return true;
    }
    
    /**
     * Release reserved stock (when cart item removed or order cancelled)
     */
    @Transactional
    public void releaseStock(String variantId, int quantity) {
        ProductVariant variant = variantRepository.findById(variantId).orElse(null);
        if (variant == null) {
            return;
        }
        
        variant.setReservedQuantity(Math.max(0, variant.getReservedQuantity() - quantity));
        variant.setUpdatedAt(LocalDateTime.now());
        variantRepository.save(variant);
    }
    
    /**
     * Confirm stock deduction (when order confirmed/paid)
     */
    @Transactional
    public void confirmStockDeduction(String variantId, int quantity) {
        ProductVariant variant = variantRepository.findById(variantId).orElse(null);
        if (variant == null) {
            return;
        }
        
        variant.setStockQuantity(Math.max(0, variant.getStockQuantity() - quantity));
        variant.setReservedQuantity(Math.max(0, variant.getReservedQuantity() - quantity));
        variant.setUpdatedAt(LocalDateTime.now());
        variantRepository.save(variant);
    }
    
    /**
     * Restore stock (when order returned)
     */
    @Transactional
    public void restoreStock(String variantId, int quantity) {
        ProductVariant variant = variantRepository.findById(variantId).orElse(null);
        if (variant == null) {
            return;
        }
        
        variant.setStockQuantity(variant.getStockQuantity() + quantity);
        variant.setUpdatedAt(LocalDateTime.now());
        variantRepository.save(variant);
    }
    
    /**
     * Get low stock variants
     */
    public List<ProductVariantDto> getLowStockVariants() {
        return variantRepository.findLowStockVariants()
                .stream()
                .map(ProductVariantDto::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * Check stock availability
     */
    public boolean checkStockAvailability(String variantId, int quantity) {
        ProductVariant variant = variantRepository.findById(variantId).orElse(null);
        return variant != null && variant.getAvailableStock() >= quantity;
    }
    
    // ==================== ATTRIBUTE MANAGEMENT ====================
    
    /**
     * Get all variant attributes
     */
    public List<VariantAttribute> getAllAttributes() {
        return attributeRepository.findAllByOrderByDisplayOrderAsc();
    }
    
    /**
     * Get attributes for a category
     */
    public List<VariantAttribute> getAttributesForCategory(String categoryId) {
        return attributeRepository.findByCategoryIdsContaining(categoryId);
    }
    
    /**
     * Create or update attribute
     */
    public VariantAttribute saveAttribute(VariantAttribute attribute) {
        if (attribute.getId() == null && attributeRepository.existsByCode(attribute.getCode())) {
            throw new IllegalArgumentException("Attribute with code already exists: " + attribute.getCode());
        }
        return attributeRepository.save(attribute);
    }
    
    /**
     * Initialize default attributes
     */
    public void initializeDefaultAttributes() {
        if (!attributeRepository.existsByCode("size")) {
            attributeRepository.save(VariantAttribute.createSizeAttribute());
        }
        if (!attributeRepository.existsByCode("color")) {
            attributeRepository.save(VariantAttribute.createColorAttribute());
        }
        if (!attributeRepository.existsByCode("material")) {
            attributeRepository.save(VariantAttribute.createMaterialAttribute());
        }
        if (!attributeRepository.existsByCode("weight")) {
            attributeRepository.save(VariantAttribute.createWeightAttribute());
        }
    }
    
    // ==================== BULK OPERATIONS ====================
    
    /**
     * Create multiple variants at once
     */
    public List<ProductVariantDto> createBulkVariants(String productId, List<CreateVariantRequest> requests) {
        List<ProductVariantDto> created = new ArrayList<>();
        
        for (CreateVariantRequest request : requests) {
            request.setProductId(productId);
            try {
                created.add(createVariant(request));
            } catch (Exception e) {
                log.error("Error creating variant: {}", e.getMessage());
            }
        }
        
        return created;
    }
    
    /**
     * Generate variants from attribute combinations
     */
    public List<ProductVariantDto> generateVariantsFromAttributes(
            String productId,
            double basePrice,
            int baseStock,
            Map<String, List<String>> attributeOptions) {
        
        List<Map<String, String>> combinations = generateAttributeCombinations(attributeOptions);
        List<ProductVariantDto> created = new ArrayList<>();
        
        for (int i = 0; i < combinations.size(); i++) {
            Map<String, String> attrs = combinations.get(i);
            
            CreateVariantRequest request = CreateVariantRequest.builder()
                    .productId(productId)
                    .attributes(attrs)
                    .price(basePrice)
                    .mrp(basePrice)
                    .stockQuantity(baseStock)
                    .lowStockThreshold(5)
                    .trackInventory(true)
                    .active(true)
                    .isDefault(i == 0)
                    .displayOrder(i)
                    .build();
            
            try {
                created.add(createVariant(request));
            } catch (Exception e) {
                log.error("Error creating variant for attributes {}: {}", attrs, e.getMessage());
            }
        }
        
        return created;
    }
    
    // ==================== HELPER METHODS ====================
    
    /**
     * Generate SKU from product ID and attributes
     */
    private String generateSku(String productId, Map<String, String> attributes) {
        StringBuilder sku = new StringBuilder();
        
        // Use first 8 chars of product ID
        sku.append(productId.length() > 8 ? productId.substring(0, 8).toUpperCase() : productId.toUpperCase());
        
        // Add attribute values
        if (attributes != null) {
            for (String value : attributes.values()) {
                sku.append("-");
                sku.append(value.replaceAll("[^a-zA-Z0-9]", "").toUpperCase().substring(0, Math.min(3, value.length())));
            }
        }
        
        // Add random suffix to ensure uniqueness
        sku.append("-").append(System.currentTimeMillis() % 10000);
        
        return sku.toString();
    }
    
    /**
     * Unset default flag on all variants for a product
     */
    private void unsetDefaultVariant(String productId) {
        variantRepository.findByProductIdAndIsDefaultTrue(productId).ifPresent(variant -> {
            variant.setDefault(false);
            variantRepository.save(variant);
        });
    }
    
    /**
     * Generate all combinations of attribute values
     */
    private List<Map<String, String>> generateAttributeCombinations(Map<String, List<String>> attributeOptions) {
        List<Map<String, String>> result = new ArrayList<>();
        List<String> attributeNames = new ArrayList<>(attributeOptions.keySet());
        
        generateCombinationsRecursive(attributeOptions, attributeNames, 0, new HashMap<>(), result);
        
        return result;
    }
    
    private void generateCombinationsRecursive(
            Map<String, List<String>> options,
            List<String> names,
            int index,
            Map<String, String> current,
            List<Map<String, String>> result) {
        
        if (index == names.size()) {
            result.add(new HashMap<>(current));
            return;
        }
        
        String attrName = names.get(index);
        for (String value : options.get(attrName)) {
            current.put(attrName, value);
            generateCombinationsRecursive(options, names, index + 1, current, result);
        }
    }
    
    /**
     * Get available attribute values for a product (based on in-stock variants)
     */
    public Map<String, Set<String>> getAvailableAttributeValues(String productId) {
        List<ProductVariant> variants = variantRepository.findByProductIdAndStockQuantityGreaterThan(productId, 0);
        Map<String, Set<String>> available = new HashMap<>();
        
        for (ProductVariant variant : variants) {
            if (variant.getAttributes() != null && variant.isActive()) {
                for (Map.Entry<String, String> entry : variant.getAttributes().entrySet()) {
                    available.computeIfAbsent(entry.getKey(), k -> new HashSet<>()).add(entry.getValue());
                }
            }
        }
        
        return available;
    }
}
