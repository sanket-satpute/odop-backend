package com.odop.root.search.repository;

import com.odop.root.search.document.VendorDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Elasticsearch repository for Vendor search operations
 */
@Repository
public interface VendorSearchRepository extends ElasticsearchRepository<VendorDocument, String> {

    /**
     * Find vendors by shop name
     */
    List<VendorDocument> findByShoppeeName(String shoppeeName);

    /**
     * Find vendors containing text in shop name
     */
    List<VendorDocument> findByShoppeeNameContaining(String text);

    /**
     * Find vendors by state
     */
    Page<VendorDocument> findByLocationState(String state, Pageable pageable);

    /**
     * Find vendors by district and state
     */
    Page<VendorDocument> findByLocationDistrictAndLocationState(String district, String state, Pageable pageable);

    /**
     * Find verified vendors
     */
    Page<VendorDocument> findByVerifiedTrue(Pageable pageable);

    /**
     * Find vendors by status
     */
    Page<VendorDocument> findByStatus(String status, Pageable pageable);

    /**
     * Custom multi-field search query
     */
    @Query("{\"bool\": {\"should\": [" +
           "{\"match\": {\"shoppeeName\": {\"query\": \"?0\", \"boost\": 3}}}," +
           "{\"match\": {\"shopkeeperName\": {\"query\": \"?0\", \"boost\": 2}}}," +
           "{\"match\": {\"businessDescription\": {\"query\": \"?0\", \"boost\": 1}}}," +
           "{\"match\": {\"productCategories\": {\"query\": \"?0\", \"boost\": 2}}}" +
           "]}}")
    Page<VendorDocument> searchByMultipleFields(String query, Pageable pageable);

    /**
     * Fuzzy search for typo tolerance
     */
    @Query("{\"bool\": {\"should\": [" +
           "{\"fuzzy\": {\"shoppeeName\": {\"value\": \"?0\", \"fuzziness\": \"AUTO\", \"boost\": 3}}}," +
           "{\"fuzzy\": {\"businessDescription\": {\"value\": \"?0\", \"fuzziness\": \"AUTO\"}}}" +
           "]}}")
    Page<VendorDocument> fuzzySearch(String query, Pageable pageable);

    /**
     * Find vendors by product category
     */
    Page<VendorDocument> findByProductCategoriesContaining(String category, Pageable pageable);

    /**
     * Count by state
     */
    long countByLocationState(String state);

    /**
     * Count verified vendors
     */
    long countByVerifiedTrue();
}
