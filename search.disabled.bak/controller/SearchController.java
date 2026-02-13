package com.odop.root.search.controller;

import com.odop.root.search.dto.*;
import com.odop.root.search.service.IndexSyncService;
import com.odop.root.search.service.SearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for Advanced Search operations
 * 
 * Endpoints:
 * - GET  /odop/search/health           - Health check
 * - GET  /odop/search                  - Search products (query params)
 * - POST /odop/search                  - Search products (request body)
 * - GET  /odop/search/vendors          - Search vendors
 * - GET  /odop/search/autocomplete     - Autocomplete suggestions
 * - GET  /odop/search/trending         - Trending searches
 * - GET  /odop/search/sync/status      - Index sync status
 * - POST /odop/search/sync/products    - Manually sync products
 * - POST /odop/search/sync/vendors     - Manually sync vendors
 * - POST /odop/search/sync/all         - Sync all data
 * 
 * Note: This controller is only active when search.enabled=true
 */
@RestController
@RequestMapping("/odop/search")
@CrossOrigin(origins = "*")
@ConditionalOnProperty(name = "search.enabled", havingValue = "true", matchIfMissing = false)
public class SearchController {

    private static final Logger logger = LoggerFactory.getLogger(SearchController.class);

    @Autowired
    private SearchService searchService;

    @Autowired
    private IndexSyncService indexSyncService;

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Advanced Search Service (Elasticsearch)");
        response.put("timestamp", System.currentTimeMillis());

        IndexSyncService.SyncStatus syncStatus = indexSyncService.getSyncStatus();
        response.put("elasticsearchEnabled", syncStatus.isElasticsearchEnabled());
        response.put("elasticsearchConnected", syncStatus.isElasticsearchConnected());

        return ResponseEntity.ok(response);
    }

    /**
     * Search products with query parameters
     * 
     * Example: /odop/search?q=handloom&state=Maharashtra&minPrice=100&maxPrice=5000
     */
    @GetMapping
    public ResponseEntity<SearchResponse<SearchResultItem>> searchProducts(
            @RequestParam(value = "q", required = false) String query,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "sortBy", defaultValue = "relevance") String sortBy,
            @RequestParam(value = "category", required = false) List<String> categories,
            @RequestParam(value = "minPrice", required = false) Double minPrice,
            @RequestParam(value = "maxPrice", required = false) Double maxPrice,
            @RequestParam(value = "minRating", required = false) Integer minRating,
            @RequestParam(value = "state", required = false) String state,
            @RequestParam(value = "district", required = false) String district,
            @RequestParam(value = "giTagged", required = false) Boolean giTagCertified,
            @RequestParam(value = "inStock", required = false) Boolean inStock,
            @RequestParam(value = "promotions", required = false) Boolean promotionEnabled,
            @RequestParam(value = "craftType", required = false) String craftType,
            @RequestParam(value = "vendorId", required = false) String vendorId,
            @RequestParam(value = "tags", required = false) List<String> tags,
            @RequestParam(value = "fuzzy", defaultValue = "true") Boolean fuzzySearch) {

        SearchRequest request = new SearchRequest();
        request.setQuery(query);
        request.setPage(page);
        request.setSize(size);
        request.setSortBy(sortBy);
        request.setCategories(categories);
        request.setMinPrice(minPrice);
        request.setMaxPrice(maxPrice);
        request.setMinRating(minRating);
        request.setState(state);
        request.setDistrict(district);
        request.setGiTagCertified(giTagCertified);
        request.setInStock(inStock);
        request.setPromotionEnabled(promotionEnabled);
        request.setCraftType(craftType);
        request.setVendorId(vendorId);
        request.setTags(tags);
        request.setFuzzySearch(fuzzySearch);

        return ResponseEntity.ok(searchService.searchProducts(request));
    }

    /**
     * Search products with request body (for complex queries)
     */
    @PostMapping
    public ResponseEntity<SearchResponse<SearchResultItem>> searchProductsPost(
            @RequestBody SearchRequest request) {

        logger.info("🔍 Search request: {}", request.getQuery());
        return ResponseEntity.ok(searchService.searchProducts(request));
    }

    /**
     * Search vendors
     */
    @GetMapping("/vendors")
    public ResponseEntity<SearchResponse<SearchResultItem>> searchVendors(
            @RequestParam(value = "q", required = false) String query,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "state", required = false) String state,
            @RequestParam(value = "district", required = false) String district,
            @RequestParam(value = "fuzzy", defaultValue = "true") Boolean fuzzySearch) {

        SearchRequest request = new SearchRequest();
        request.setQuery(query);
        request.setSearchType("vendors");
        request.setPage(page);
        request.setSize(size);
        request.setState(state);
        request.setDistrict(district);
        request.setFuzzySearch(fuzzySearch);

        return ResponseEntity.ok(searchService.searchVendors(request));
    }

    /**
     * Autocomplete suggestions as user types
     */
    @GetMapping("/autocomplete")
    public ResponseEntity<AutocompleteResponse> autocomplete(
            @RequestParam("q") String query,
            @RequestParam(value = "limit", defaultValue = "10") int limit) {

        return ResponseEntity.ok(searchService.autocomplete(query, limit));
    }

    /**
     * Get trending/popular searches
     */
    @GetMapping("/trending")
    public ResponseEntity<Map<String, Object>> getTrendingSearches(
            @RequestParam(value = "limit", defaultValue = "10") int limit) {

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("trending", searchService.getTrendingSearches(limit));
        return ResponseEntity.ok(response);
    }

    /**
     * Get index sync status
     */
    @GetMapping("/sync/status")
    public ResponseEntity<IndexSyncService.SyncStatus> getSyncStatus() {
        return ResponseEntity.ok(indexSyncService.getSyncStatus());
    }

    /**
     * Manually sync products to Elasticsearch
     */
    @PostMapping("/sync/products")
    public ResponseEntity<Map<String, Object>> syncProducts() {
        int count = indexSyncService.syncAllProducts();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Products synced to Elasticsearch");
        response.put("count", count);

        return ResponseEntity.ok(response);
    }

    /**
     * Manually sync vendors to Elasticsearch
     */
    @PostMapping("/sync/vendors")
    public ResponseEntity<Map<String, Object>> syncVendors() {
        int count = indexSyncService.syncAllVendors();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Vendors synced to Elasticsearch");
        response.put("count", count);

        return ResponseEntity.ok(response);
    }

    /**
     * Sync all data to Elasticsearch
     */
    @PostMapping("/sync/all")
    public ResponseEntity<Map<String, Object>> syncAll() {
        int products = indexSyncService.syncAllProducts();
        int vendors = indexSyncService.syncAllVendors();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "All data synced to Elasticsearch");
        response.put("products", products);
        response.put("vendors", vendors);

        return ResponseEntity.ok(response);
    }

    /**
     * Filter options for search UI (available categories, states, etc.)
     */
    @GetMapping("/filters")
    public ResponseEntity<Map<String, Object>> getFilterOptions() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);

        // These would ideally be aggregated from Elasticsearch
        Map<String, Object> filters = new HashMap<>();
        
        filters.put("sortOptions", List.of(
            Map.of("value", "relevance", "label", "Relevance"),
            Map.of("value", "price_asc", "label", "Price: Low to High"),
            Map.of("value", "price_desc", "label", "Price: High to Low"),
            Map.of("value", "rating", "label", "Customer Rating"),
            Map.of("value", "newest", "label", "Newest First"),
            Map.of("value", "popularity", "label", "Popularity")
        ));

        filters.put("craftTypes", List.of(
            "Handloom", "Handicraft", "Pottery", "Leather", "Bamboo",
            "Wooden", "Metal", "Textile", "Jewelry", "Painting"
        ));

        filters.put("priceRanges", List.of(
            Map.of("min", 0, "max", 500, "label", "Under ₹500"),
            Map.of("min", 500, "max", 1000, "label", "₹500 - ₹1000"),
            Map.of("min", 1000, "max", 2500, "label", "₹1000 - ₹2500"),
            Map.of("min", 2500, "max", 5000, "label", "₹2500 - ₹5000"),
            Map.of("min", 5000, "max", 10000, "label", "₹5000 - ₹10000"),
            Map.of("min", 10000, "max", null, "label", "Above ₹10000")
        ));

        filters.put("ratings", List.of(4, 3, 2, 1));

        response.put("filters", filters);
        return ResponseEntity.ok(response);
    }
}
