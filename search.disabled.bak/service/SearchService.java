package com.odop.root.search.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.TotalHits;
import com.odop.root.models.Products;
import com.odop.root.repository.ProductRepository;
import com.odop.root.search.config.ElasticsearchConfig;
import com.odop.root.search.document.ProductDocument;
import com.odop.root.search.document.VendorDocument;
import com.odop.root.search.dto.*;
import com.odop.root.search.repository.ProductSearchRepository;
import com.odop.root.search.repository.VendorSearchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Advanced Search Service using Elasticsearch
 * 
 * Features:
 * - Full-text search across multiple fields
 * - Fuzzy matching for typo tolerance
 * - Filters (category, price, location, rating, etc.)
 * - Sorting (relevance, price, rating, popularity)
 * - Aggregations for faceted search
 * - Autocomplete suggestions
 * - MongoDB fallback when ES is unavailable
 * 
 * Note: This service is only active when search.enabled=true
 */
@Service
@ConditionalOnProperty(name = "search.enabled", havingValue = "true", matchIfMissing = false)
public class SearchService {

    private static final Logger logger = LoggerFactory.getLogger(SearchService.class);

    @Autowired
    private ElasticsearchClient elasticsearchClient;

    @Autowired
    private ElasticsearchConfig elasticsearchConfig;

    @Autowired
    private ProductSearchRepository productSearchRepository;

    @Autowired
    private VendorSearchRepository vendorSearchRepository;

    @Autowired
    private ProductRepository productRepository; // MongoDB fallback

    @Value("${search.index.products:odop_products}")
    private String productsIndex;

    @Value("${search.index.vendors:odop_vendors}")
    private String vendorsIndex;

    @Value("${search.page-size:20}")
    private int defaultPageSize;

    /**
     * Main search method - searches products with filters and aggregations
     */
    public com.odop.root.search.dto.SearchResponse<SearchResultItem> searchProducts(
            com.odop.root.search.dto.SearchRequest request) {
        
        long startTime = System.currentTimeMillis();

        // Fall back to MongoDB if ES is disabled or unavailable
        if (!elasticsearchConfig.isSearchEnabled()) {
            return searchProductsWithMongoDB(request);
        }

        try {
            // Build the Elasticsearch query
            BoolQuery.Builder boolQuery = new BoolQuery.Builder();

            // Text search across multiple fields
            if (request.getQuery() != null && !request.getQuery().trim().isEmpty()) {
                String query = request.getQuery().trim();

                if (Boolean.TRUE.equals(request.getFuzzySearch())) {
                    // Fuzzy multi-match for typo tolerance
                    boolQuery.must(Query.of(q -> q
                        .multiMatch(m -> m
                            .query(query)
                            .fields("productName^3", "productDescription", "tags^2", 
                                   "localName^2", "craftType^1.5", "originStory", "categoryName^2")
                            .fuzziness("AUTO")
                            .prefixLength(2)
                        )
                    ));
                } else {
                    // Exact multi-match
                    boolQuery.must(Query.of(q -> q
                        .multiMatch(m -> m
                            .query(query)
                            .fields("productName^3", "productDescription", "tags^2", 
                                   "localName^2", "craftType^1.5", "originStory", "categoryName^2")
                        )
                    ));
                }
            }

            // Apply filters
            applyFilters(boolQuery, request);

            // Build search request
            SearchRequest.Builder searchRequest = new SearchRequest.Builder()
                .index(productsIndex)
                .query(Query.of(q -> q.bool(boolQuery.build())))
                .from(request.getPage() * request.getSize())
                .size(request.getSize());

            // Apply sorting
            applySorting(searchRequest, request);

            // Execute search
            SearchResponse<ProductDocument> response = elasticsearchClient.search(
                searchRequest.build(), ProductDocument.class);

            // Process results
            List<SearchResultItem> results = new ArrayList<>();
            for (Hit<ProductDocument> hit : response.hits().hits()) {
                ProductDocument doc = hit.source();
                if (doc != null) {
                    results.add(mapToSearchResultItem(doc, hit.score()));
                }
            }

            // Build response
            TotalHits totalHits = response.hits().total();
            long total = totalHits != null ? totalHits.value() : 0;

            com.odop.root.search.dto.SearchResponse<SearchResultItem> searchResponse = 
                com.odop.root.search.dto.SearchResponse.success(results, total, request.getPage(), request.getSize());
            
            searchResponse.setQuery(request.getQuery());
            searchResponse.setSearchType("products");
            searchResponse.setTook(System.currentTimeMillis() - startTime);

            logger.info("🔍 Search completed: '{}' - {} results in {}ms", 
                       request.getQuery(), total, searchResponse.getTook());

            return searchResponse;

        } catch (IOException e) {
            logger.error("Elasticsearch search failed, falling back to MongoDB: {}", e.getMessage());
            return searchProductsWithMongoDB(request);
        }
    }

    /**
     * Search vendors
     */
    public com.odop.root.search.dto.SearchResponse<SearchResultItem> searchVendors(
            com.odop.root.search.dto.SearchRequest request) {
        
        long startTime = System.currentTimeMillis();

        if (!elasticsearchConfig.isSearchEnabled()) {
            return com.odop.root.search.dto.SearchResponse.error("Elasticsearch is disabled");
        }

        try {
            Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
            Page<VendorDocument> results;

            if (request.getQuery() != null && !request.getQuery().trim().isEmpty()) {
                if (Boolean.TRUE.equals(request.getFuzzySearch())) {
                    results = vendorSearchRepository.fuzzySearch(request.getQuery(), pageable);
                } else {
                    results = vendorSearchRepository.searchByMultipleFields(request.getQuery(), pageable);
                }
            } else if (request.getState() != null) {
                if (request.getDistrict() != null) {
                    results = vendorSearchRepository.findByLocationDistrictAndLocationState(
                        request.getDistrict(), request.getState(), pageable);
                } else {
                    results = vendorSearchRepository.findByLocationState(request.getState(), pageable);
                }
            } else {
                results = vendorSearchRepository.findByVerifiedTrue(pageable);
            }

            List<SearchResultItem> items = results.getContent().stream()
                .map(this::mapVendorToSearchResultItem)
                .collect(Collectors.toList());

            com.odop.root.search.dto.SearchResponse<SearchResultItem> response = 
                com.odop.root.search.dto.SearchResponse.success(
                    items, results.getTotalElements(), request.getPage(), request.getSize());
            
            response.setQuery(request.getQuery());
            response.setSearchType("vendors");
            response.setTook(System.currentTimeMillis() - startTime);

            return response;

        } catch (Exception e) {
            logger.error("Vendor search failed: {}", e.getMessage());
            return com.odop.root.search.dto.SearchResponse.error("Search failed: " + e.getMessage());
        }
    }

    /**
     * Autocomplete suggestions
     */
    public AutocompleteResponse autocomplete(String query, int limit) {
        long startTime = System.currentTimeMillis();

        if (query == null || query.trim().length() < 2) {
            return AutocompleteResponse.empty(query);
        }

        List<AutocompleteResponse.Suggestion> suggestions = new ArrayList<>();

        if (!elasticsearchConfig.isSearchEnabled()) {
            // MongoDB fallback for autocomplete
            return getMongoDbAutocompleteSuggestions(query, limit);
        }

        try {
            // Search products for suggestions
            List<ProductDocument> products = productSearchRepository.findByProductNameContaining(query);

            for (ProductDocument doc : products.stream().limit(limit).toList()) {
                AutocompleteResponse.Suggestion suggestion = new AutocompleteResponse.Suggestion();
                suggestion.setText(doc.getProductName());
                suggestion.setType("product");
                suggestion.setId(doc.getId());
                suggestion.setImageUrl(doc.getProductImageURL());
                suggestion.setAdditionalInfo("₹" + doc.getPrice());
                suggestions.add(suggestion);
            }

            // Search vendors for suggestions
            List<VendorDocument> vendors = vendorSearchRepository.findByShoppeeNameContaining(query);

            for (VendorDocument doc : vendors.stream().limit(limit).toList()) {
                AutocompleteResponse.Suggestion suggestion = new AutocompleteResponse.Suggestion();
                suggestion.setText(doc.getShoppeeName());
                suggestion.setType("vendor");
                suggestion.setId(doc.getId());
                suggestion.setImageUrl(doc.getProfilePictureUrl());
                suggestion.setAdditionalInfo(doc.getLocationState());
                suggestions.add(suggestion);
            }

            AutocompleteResponse response = AutocompleteResponse.success(query, suggestions);
            response.setTook(System.currentTimeMillis() - startTime);

            return response;

        } catch (Exception e) {
            logger.warn("Autocomplete failed, using MongoDB fallback: {}", e.getMessage());
            return getMongoDbAutocompleteSuggestions(query, limit);
        }
    }

    /**
     * Get trending/popular searches
     */
    public List<String> getTrendingSearches(int limit) {
        // This would ideally be based on search analytics
        // For now, return popular categories/products
        return Arrays.asList(
            "Handloom Saree",
            "Brass Handicraft",
            "Wooden Toys",
            "Leather Goods",
            "Pottery",
            "Bamboo Craft",
            "Silk Products",
            "Tribal Art"
        );
    }

    // ========== Private Helper Methods ==========

    private void applyFilters(BoolQuery.Builder boolQuery, com.odop.root.search.dto.SearchRequest request) {
        // Category filter
        if (request.getCategories() != null && !request.getCategories().isEmpty()) {
            boolQuery.filter(Query.of(q -> q
                .terms(t -> t
                    .field("categoryId")
                    .terms(v -> v.value(request.getCategories().stream()
                        .map(c -> co.elastic.clients.elasticsearch._types.FieldValue.of(c))
                        .collect(Collectors.toList())))
                )
            ));
        }

        // Price range filter
        if (request.getMinPrice() != null || request.getMaxPrice() != null) {
            boolQuery.filter(Query.of(q -> q
                .range(r -> {
                    r.field("price");
                    if (request.getMinPrice() != null) r.gte(co.elastic.clients.json.JsonData.of(request.getMinPrice()));
                    if (request.getMaxPrice() != null) r.lte(co.elastic.clients.json.JsonData.of(request.getMaxPrice()));
                    return r;
                })
            ));
        }

        // Rating filter
        if (request.getMinRating() != null) {
            boolQuery.filter(Query.of(q -> q
                .range(r -> r
                    .field("rating")
                    .gte(co.elastic.clients.json.JsonData.of(request.getMinRating()))
                )
            ));
        }

        // State filter
        if (request.getState() != null && !request.getState().isEmpty()) {
            boolQuery.filter(Query.of(q -> q
                .term(t -> t.field("originState").value(request.getState()))
            ));
        }

        // District filter
        if (request.getDistrict() != null && !request.getDistrict().isEmpty()) {
            boolQuery.filter(Query.of(q -> q
                .term(t -> t.field("originDistrict").value(request.getDistrict()))
            ));
        }

        // GI Tag filter
        if (Boolean.TRUE.equals(request.getGiTagCertified())) {
            boolQuery.filter(Query.of(q -> q
                .term(t -> t.field("giTagCertified").value(true))
            ));
        }

        // In Stock filter
        if (Boolean.TRUE.equals(request.getInStock())) {
            boolQuery.filter(Query.of(q -> q
                .term(t -> t.field("stockStatus").value("In Stock"))
            ));
        }

        // Promotion filter
        if (Boolean.TRUE.equals(request.getPromotionEnabled())) {
            boolQuery.filter(Query.of(q -> q
                .term(t -> t.field("promotionEnabled").value(true))
            ));
        }

        // Vendor filter
        if (request.getVendorId() != null && !request.getVendorId().isEmpty()) {
            boolQuery.filter(Query.of(q -> q
                .term(t -> t.field("vendorId").value(request.getVendorId()))
            ));
        }

        // Craft type filter
        if (request.getCraftType() != null && !request.getCraftType().isEmpty()) {
            boolQuery.filter(Query.of(q -> q
                .term(t -> t.field("craftType").value(request.getCraftType()))
            ));
        }

        // Tags filter
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            for (String tag : request.getTags()) {
                boolQuery.filter(Query.of(q -> q
                    .term(t -> t.field("tags").value(tag))
                ));
            }
        }
    }

    private void applySorting(SearchRequest.Builder searchRequest, com.odop.root.search.dto.SearchRequest request) {
        String sortBy = request.getSortBy();
        SortOrder order = "asc".equalsIgnoreCase(request.getSortOrder()) ? SortOrder.Asc : SortOrder.Desc;

        switch (sortBy != null ? sortBy.toLowerCase() : "relevance") {
            case "price_asc":
                searchRequest.sort(s -> s.field(f -> f.field("price").order(SortOrder.Asc)));
                break;
            case "price_desc":
                searchRequest.sort(s -> s.field(f -> f.field("price").order(SortOrder.Desc)));
                break;
            case "rating":
                searchRequest.sort(s -> s.field(f -> f.field("rating").order(SortOrder.Desc)));
                break;
            case "newest":
                searchRequest.sort(s -> s.field(f -> f.field("createdAt").order(SortOrder.Desc)));
                break;
            case "popularity":
                searchRequest.sort(s -> s.field(f -> f.field("popularityScore").order(SortOrder.Desc)));
                break;
            case "relevance":
            default:
                // Default ES relevance scoring
                searchRequest.sort(s -> s.score(sc -> sc.order(SortOrder.Desc)));
                break;
        }
    }

    private SearchResultItem mapToSearchResultItem(ProductDocument doc, Double score) {
        SearchResultItem item = new SearchResultItem();
        item.setId(doc.getId());
        item.setType("product");
        item.setScore(score);
        item.setProductName(doc.getProductName());
        item.setProductDescription(doc.getProductDescription());
        item.setProductImageURL(doc.getProductImageURL());
        item.setPrice(doc.getPrice());
        item.setDiscountedPrice(doc.getDiscountedPrice());
        item.setDiscount(doc.getDiscount());
        item.setRating(doc.getRating());
        item.setCategoryName(doc.getCategoryName());
        item.setVendorName(doc.getVendorName());
        item.setShopName(doc.getShopName());
        item.setStockStatus(doc.getStockStatus());
        item.setTags(doc.getTags());
        item.setOriginDistrict(doc.getOriginDistrict());
        item.setOriginState(doc.getOriginState());
        item.setGiTagCertified(doc.getGiTagCertified());
        item.setCraftType(doc.getCraftType());
        item.setLocalName(doc.getLocalName());
        return item;
    }

    private SearchResultItem mapVendorToSearchResultItem(VendorDocument doc) {
        SearchResultItem item = new SearchResultItem();
        item.setId(doc.getId());
        item.setType("vendor");
        item.setShoppeeName(doc.getShoppeeName());
        item.setBusinessDescription(doc.getBusinessDescription());
        item.setLocationDistrict(doc.getLocationDistrict());
        item.setLocationState(doc.getLocationState());
        item.setProductCount(doc.getProductCount());
        item.setVerified(doc.getVerified());
        item.setProfilePictureUrl(doc.getProfilePictureUrl());
        return item;
    }

    /**
     * MongoDB fallback search when Elasticsearch is unavailable
     */
    private com.odop.root.search.dto.SearchResponse<SearchResultItem> searchProductsWithMongoDB(
            com.odop.root.search.dto.SearchRequest request) {
        
        long startTime = System.currentTimeMillis();
        List<Products> products;

        // Simple MongoDB search (limited functionality)
        if (request.getQuery() != null && !request.getQuery().isEmpty()) {
            products = productRepository.findByProductName(request.getQuery());
        } else if (request.getState() != null) {
            if (request.getDistrict() != null) {
                products = productRepository.findByOriginDistrictAndOriginState(
                    request.getDistrict(), request.getState());
            } else {
                products = productRepository.findByOriginState(request.getState());
            }
        } else if (request.getCategories() != null && !request.getCategories().isEmpty()) {
            products = productRepository.findByCategoryId(request.getCategories().get(0));
        } else {
            products = productRepository.findAll();
        }

        // Convert to search result items
        List<SearchResultItem> results = products.stream()
            .skip((long) request.getPage() * request.getSize())
            .limit(request.getSize())
            .map(this::mapMongoProductToSearchResultItem)
            .collect(Collectors.toList());

        com.odop.root.search.dto.SearchResponse<SearchResultItem> response = 
            com.odop.root.search.dto.SearchResponse.success(
                results, products.size(), request.getPage(), request.getSize());
        
        response.setQuery(request.getQuery());
        response.setSearchType("products (MongoDB fallback)");
        response.setTook(System.currentTimeMillis() - startTime);
        response.setMessage("Using MongoDB fallback - Elasticsearch unavailable");

        return response;
    }

    private SearchResultItem mapMongoProductToSearchResultItem(Products product) {
        SearchResultItem item = new SearchResultItem();
        item.setId(product.getProductId());
        item.setType("product");
        item.setProductName(product.getProductName());
        item.setProductDescription(product.getProductDescription());
        item.setProductImageURL(product.getProductImageURL());
        item.setPrice(product.getPrice());
        item.setDiscount(product.getDiscount());
        item.setRating(product.getRating());
        item.setStockStatus(product.getStockStatus());
        item.setTags(product.getTags());
        item.setOriginDistrict(product.getOriginDistrict());
        item.setOriginState(product.getOriginState());
        item.setGiTagCertified(product.getGiTagCertified());
        item.setCraftType(product.getCraftType());
        item.setLocalName(product.getLocalName());
        
        // Calculate discounted price
        if (product.getDiscount() > 0) {
            item.setDiscountedPrice(product.getPrice() * (1 - product.getDiscount() / 100.0));
        } else {
            item.setDiscountedPrice(product.getPrice());
        }

        return item;
    }

    private AutocompleteResponse getMongoDbAutocompleteSuggestions(String query, int limit) {
        List<AutocompleteResponse.Suggestion> suggestions = new ArrayList<>();

        List<Products> products = productRepository.findByProductName(query);
        for (Products p : products.stream().limit(limit).collect(Collectors.toList())) {
            AutocompleteResponse.Suggestion suggestion = new AutocompleteResponse.Suggestion();
            suggestion.setText(p.getProductName());
            suggestion.setType("product");
            suggestion.setId(p.getProductId());
            suggestion.setImageUrl(p.getProductImageURL());
            suggestion.setAdditionalInfo("₹" + p.getPrice());
            suggestions.add(suggestion);
        }

        return AutocompleteResponse.success(query, suggestions);
    }
}
