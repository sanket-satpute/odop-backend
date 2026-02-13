package com.odop.root.search.controller;

import com.odop.root.search.dto.AutocompleteResponse;
import com.odop.root.search.dto.SearchRequest;
import com.odop.root.search.dto.SearchResponse;
import com.odop.root.search.service.SearchService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

import java.util.List;

/**
 * REST Controller for search functionality.
 * Provides endpoints for product search, vendor search, and autocomplete.
 */
@RestController
@RequestMapping("/odop/search")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:63699"})
public class SearchController {

    private static final Logger logger = LoggerFactory.getLogger(SearchController.class);

    @Autowired
    private SearchService searchService;

    /**
     * Health check endpoint.
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Search API");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    /**
     * Main search endpoint for products.
     * 
     * Examples:
     * - GET /odop/search?q=handloom saree
     * - GET /odop/search?q=pottery&state=Rajasthan&minPrice=500&maxPrice=5000
     * - GET /odop/search?q=silk&giTaggedOnly=true&sortBy=price_asc
     */
    @GetMapping
    public ResponseEntity<SearchResponse> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) String district,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Integer minRating,
            @RequestParam(required = false) Boolean giTaggedOnly,
            @RequestParam(required = false) Boolean inStockOnly,
            @RequestParam(required = false) String vendorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "relevance") String sortBy) {
        
        logger.info("Search request: q='{}', district='{}', state='{}'", q, district, state);
        
        SearchRequest request = new SearchRequest(q);
        request.setCategoryId(categoryId);
        request.setDistrict(district);
        request.setState(state);
        request.setMinPrice(minPrice);
        request.setMaxPrice(maxPrice);
        request.setMinRating(minRating);
        request.setGiTaggedOnly(giTaggedOnly);
        request.setInStockOnly(inStockOnly);
        request.setVendorId(vendorId);
        request.setPage(page);
        request.setSize(size);
        request.setSortBy(sortBy);
        
        SearchResponse response = searchService.search(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Search endpoint with POST body for complex queries.
     */
    @PostMapping
    public ResponseEntity<SearchResponse> searchPost(@RequestBody SearchRequest request) {
        logger.info("Search POST request: q='{}'", request.getQuery());
        SearchResponse response = searchService.search(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Search vendors.
     * 
     * Examples:
     * - GET /odop/search/vendors?q=handicraft
     * - GET /odop/search/vendors?district=Jaipur&state=Rajasthan
     */
    @GetMapping("/vendors")
    public ResponseEntity<SearchResponse> searchVendors(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String district,
            @RequestParam(required = false) String state,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        logger.info("Vendor search: q='{}', district='{}', state='{}'", q, district, state);
        
        SearchRequest request = new SearchRequest(q);
        request.setDistrict(district);
        request.setState(state);
        request.setPage(page);
        request.setSize(size);
        request.setSearchType("VENDORS");
        
        SearchResponse response = searchService.searchVendors(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Autocomplete suggestions for search input.
     * 
     * Example: GET /odop/search/autocomplete?q=hand
     */
    @GetMapping("/autocomplete")
    public ResponseEntity<AutocompleteResponse> autocomplete(
            @RequestParam String q,
            @RequestParam(defaultValue = "10") int limit) {
        
        logger.debug("Autocomplete request: q='{}'", q);
        AutocompleteResponse response = searchService.autocomplete(q, limit);
        return ResponseEntity.ok(response);
    }

    /**
     * Search products by location (ODOP feature).
     * 
     * Example: GET /odop/search/location?district=Moradabad&state=Uttar Pradesh
     */
    @GetMapping("/location")
    public ResponseEntity<SearchResponse> searchByLocation(
            @RequestParam String district,
            @RequestParam String state,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        logger.info("Location search: district='{}', state='{}'", district, state);
        
        SearchRequest request = new SearchRequest();
        request.setPage(page);
        request.setSize(size);
        
        SearchResponse response = searchService.searchByLocation(district, state, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Search GI-tagged products.
     * 
     * Example: GET /odop/search/gi-tagged?state=Karnataka
     */
    @GetMapping("/gi-tagged")
    public ResponseEntity<SearchResponse> searchGiTagged(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String state,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        logger.info("GI-tagged search: q='{}', state='{}'", q, state);
        
        SearchRequest request = new SearchRequest(q);
        request.setState(state);
        request.setGiTaggedOnly(true);
        request.setPage(page);
        request.setSize(size);
        
        SearchResponse response = searchService.searchGiTagged(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Get popular searches for trending section.
     */
    @GetMapping("/popular")
    public ResponseEntity<List<String>> getPopularSearches(
            @RequestParam(defaultValue = "10") int limit) {
        
        List<String> popularSearches = searchService.getPopularSearches(limit);
        return ResponseEntity.ok(popularSearches);
    }

    /**
     * Quick search - simplified endpoint for header search bar.
     */
    @GetMapping("/quick")
    public ResponseEntity<SearchResponse> quickSearch(
            @RequestParam String q,
            @RequestParam(defaultValue = "10") int limit) {
        
        SearchRequest request = new SearchRequest(q);
        request.setSize(limit);
        request.setSortBy("relevance");
        
        SearchResponse response = searchService.search(request);
        return ResponseEntity.ok(response);
    }
}
