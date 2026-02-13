package com.odop.root.search.repository;

import com.odop.root.search.document.ProductDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Elasticsearch repository for Product search operations
 */
@Repository
public interface ProductSearchRepository extends ElasticsearchRepository<ProductDocument, String> {

    /**
     * Find products by name (exact match)
     */
    List<ProductDocument> findByProductName(String productName);

    /**
     * Find products containing text in name
     */
    List<ProductDocument> findByProductNameContaining(String text);

    /**
     * Find products by category
     */
    Page<ProductDocument> findByCategoryId(String categoryId, Pageable pageable);

    /**
     * Find products by vendor
     */
    Page<ProductDocument> findByVendorId(String vendorId, Pageable pageable);

    /**
     * Find products by state
     */
    Page<ProductDocument> findByOriginState(String state, Pageable pageable);

    /**
     * Find products by district and state
     */
    Page<ProductDocument> findByOriginDistrictAndOriginState(String district, String state, Pageable pageable);

    /**
     * Find GI tagged products
     */
    Page<ProductDocument> findByGiTagCertifiedTrue(Pageable pageable);

    /**
     * Find products in stock
     */
    Page<ProductDocument> findByStockStatus(String stockStatus, Pageable pageable);

    /**
     * Find products with minimum rating
     */
    Page<ProductDocument> findByRatingGreaterThanEqual(Integer rating, Pageable pageable);

    /**
     * Find products in price range
     */
    Page<ProductDocument> findByPriceBetween(Double minPrice, Double maxPrice, Pageable pageable);

    /**
     * Find products with promotions
     */
    Page<ProductDocument> findByPromotionEnabledTrue(Pageable pageable);

    /**
     * Custom multi-field search query
     */
    @Query("{\"bool\": {\"should\": [" +
           "{\"match\": {\"productName\": {\"query\": \"?0\", \"boost\": 3}}}," +
           "{\"match\": {\"productDescription\": {\"query\": \"?0\", \"boost\": 1}}}," +
           "{\"match\": {\"tags\": {\"query\": \"?0\", \"boost\": 2}}}," +
           "{\"match\": {\"localName\": {\"query\": \"?0\", \"boost\": 2}}}," +
           "{\"match\": {\"craftType\": {\"query\": \"?0\", \"boost\": 1.5}}}" +
           "]}}")
    Page<ProductDocument> searchByMultipleFields(String query, Pageable pageable);

    /**
     * Fuzzy search for typo tolerance
     */
    @Query("{\"bool\": {\"should\": [" +
           "{\"fuzzy\": {\"productName\": {\"value\": \"?0\", \"fuzziness\": \"AUTO\", \"boost\": 3}}}," +
           "{\"fuzzy\": {\"productDescription\": {\"value\": \"?0\", \"fuzziness\": \"AUTO\"}}}," +
           "{\"fuzzy\": {\"tags\": {\"value\": \"?0\", \"fuzziness\": \"AUTO\", \"boost\": 2}}}" +
           "]}}")
    Page<ProductDocument> fuzzySearch(String query, Pageable pageable);

    /**
     * Find by tag
     */
    Page<ProductDocument> findByTagsContaining(String tag, Pageable pageable);

    /**
     * Find by craft type
     */
    Page<ProductDocument> findByCraftType(String craftType, Pageable pageable);

    /**
     * Count by category
     */
    long countByCategoryId(String categoryId);

    /**
     * Count by vendor
     */
    long countByVendorId(String vendorId);

    /**
     * Count by state
     */
    long countByOriginState(String state);
}
