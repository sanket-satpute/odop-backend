package com.odop.root.variant.repository;

import com.odop.root.variant.model.ProductVariant;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductVariantRepository extends MongoRepository<ProductVariant, String> {
    
    // Find by product
    List<ProductVariant> findByProductIdOrderByDisplayOrderAsc(String productId);
    
    List<ProductVariant> findByProductIdAndActiveTrue(String productId);
    
    // Find by SKU
    Optional<ProductVariant> findBySku(String sku);
    
    boolean existsBySku(String sku);
    
    // Find default variant
    Optional<ProductVariant> findByProductIdAndIsDefaultTrue(String productId);
    
    // Find by attributes
    @Query("{'productId': ?0, 'attributes': ?1}")
    Optional<ProductVariant> findByProductIdAndAttributes(String productId, java.util.Map<String, String> attributes);
    
    // Find in stock variants
    List<ProductVariant> findByProductIdAndStockQuantityGreaterThan(String productId, int minStock);
    
    // Find low stock variants
    @Query("{'stockQuantity': {'$lte': '$lowStockThreshold'}, 'trackInventory': true}")
    List<ProductVariant> findLowStockVariants();
    
    // Find low stock variants for a vendor's products
    @Query("{'productId': {'$in': ?0}, 'stockQuantity': {'$lte': '$lowStockThreshold'}, 'trackInventory': true}")
    List<ProductVariant> findLowStockVariantsForProducts(List<String> productIds);
    
    // Count variants for product
    long countByProductId(String productId);
    
    // Count active variants
    long countByProductIdAndActiveTrue(String productId);
    
    // Delete all variants for a product
    void deleteByProductId(String productId);
    
    // Find by barcode
    Optional<ProductVariant> findByBarcode(String barcode);
    
    // Find variants with specific attribute
    @Query("{'productId': ?0, 'attributes.?1': {'$exists': true}}")
    List<ProductVariant> findByProductIdAndAttributeExists(String productId, String attributeName);
    
    // Find variants with attribute value
    @Query("{'productId': ?0, 'attributes.?1': ?2}")
    List<ProductVariant> findByProductIdAndAttributeValue(String productId, String attributeName, String attributeValue);
}
