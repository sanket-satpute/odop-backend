package com.odop.root.search.service;

import com.odop.root.models.ProductCategory;
import com.odop.root.models.Products;
import com.odop.root.models.Vendor;
import com.odop.root.repository.ProductCategoryRepository;
import com.odop.root.repository.VendorRepository;
import com.odop.root.search.dto.*;
import com.odop.root.search.dto.AutocompleteResponse.*;
import com.odop.root.search.dto.SearchResponse.FacetItem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.core.query.TextQuery;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Service for MongoDB full-text search functionality.
 * Supports product search, vendor search, autocomplete, and faceted search.
 */
@Service
@SuppressWarnings({"unchecked", "null"})
public class SearchService {

    private static final Logger logger = LoggerFactory.getLogger(SearchService.class);

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private ProductCategoryRepository categoryRepository;

    @Autowired
    private VendorRepository vendorRepository;

    // Cache for popular searches (simple in-memory cache)
    private final Map<String, Long> searchCountCache = new ConcurrentHashMap<>();
    private static final List<String> POPULAR_SEARCHES = Arrays.asList(
        "handloom saree", "brass handicraft", "leather goods", "pottery",
        "silk", "embroidery", "wooden toys", "bamboo craft", "terracotta",
        "pashmina", "phulkari", "bidri", "blue pottery", "dhokra art"
    );

    /**
     * Main search method - searches products with filters and pagination.
     */
    public SearchResponse search(SearchRequest request) {
        long startTime = System.currentTimeMillis();
        
        try {
            String query = request.getQuery();
            if (query == null || query.trim().isEmpty()) {
                return getAllProducts(request);
            }
            
            query = query.trim();
            logger.info("Searching for: '{}' with filters", query);
            
            // Track search for popularity
            trackSearch(query);
            
            // Build the search query
            Query searchQuery = buildSearchQuery(query, request);
            
            // Get total count
            long totalResults = mongoTemplate.count(searchQuery, Products.class);
            
            // Apply pagination and sorting
            applyPaginationAndSort(searchQuery, request);
            
            // Execute search
            List<Products> products = mongoTemplate.find(searchQuery, Products.class);
            
            // Convert to search results
            List<SearchResultItem> results = products.stream()
                .map(this::convertToSearchResult)
                .collect(Collectors.toList());
            
            // Build response
            SearchResponse response = SearchResponse.success(
                results, totalResults, request.getPage(), request.getSize()
            );
            response.setQuery(query);
            response.setSearchTimeMs(System.currentTimeMillis() - startTime);
            
            // Add facets
            if (totalResults > 0) {
                response.setFacets(buildFacets(query, request));
            }
            
            // Add suggestions if few results
            if (totalResults < 5) {
                response.setSuggestions(getRelatedSuggestions(query));
            }
            
            logger.info("Search completed: {} results in {}ms", totalResults, response.getSearchTimeMs());
            return response;
            
        } catch (Exception e) {
            logger.error("Search error: {}", e.getMessage(), e);
            return SearchResponse.error("Search failed: " + e.getMessage());
        }
    }

    /**
     * Search vendors.
     */
    public SearchResponse searchVendors(SearchRequest request) {
        long startTime = System.currentTimeMillis();
        
        try {
            String query = request.getQuery();
            Query searchQuery = new Query();
            
            if (query != null && !query.trim().isEmpty()) {
                // Use text search or regex for vendor search
                TextCriteria textCriteria = TextCriteria.forDefaultLanguage()
                    .matchingAny(query.split("\\s+"));
                searchQuery = TextQuery.queryText(textCriteria)
                    .sortByScore();
            }
            
            // Add location filter
            if (request.getDistrict() != null && !request.getDistrict().isEmpty()) {
                searchQuery.addCriteria(Criteria.where("locationDistrict")
                    .regex(Pattern.quote(request.getDistrict()), "i"));
            }
            if (request.getState() != null && !request.getState().isEmpty()) {
                searchQuery.addCriteria(Criteria.where("locationState")
                    .regex(Pattern.quote(request.getState()), "i"));
            }
            
            // Only verified vendors
            searchQuery.addCriteria(Criteria.where("status").is("verified"));
            
            long totalResults = mongoTemplate.count(searchQuery, Vendor.class);
            
            // Pagination
            searchQuery.with(PageRequest.of(request.getPage(), request.getSize()));
            
            List<Vendor> vendors = mongoTemplate.find(searchQuery, Vendor.class);
            
            List<SearchResultItem> results = vendors.stream()
                .map(this::convertVendorToSearchResult)
                .collect(Collectors.toList());
            
            SearchResponse response = SearchResponse.success(
                results, totalResults, request.getPage(), request.getSize()
            );
            response.setQuery(query);
            response.setSearchTimeMs(System.currentTimeMillis() - startTime);
            
            return response;
            
        } catch (Exception e) {
            logger.error("Vendor search error: {}", e.getMessage());
            return SearchResponse.error("Vendor search failed: " + e.getMessage());
        }
    }

    /**
     * Autocomplete suggestions for search input.
     */
    public AutocompleteResponse autocomplete(String query, int limit) {
        AutocompleteResponse response = new AutocompleteResponse(query);
        
        if (query == null || query.trim().length() < 2) {
            response.setPopularSearches(POPULAR_SEARCHES.subList(0, Math.min(5, POPULAR_SEARCHES.size())));
            return response;
        }
        
        // Use a new final variable for the processed query
        final String searchQuery = query.trim().toLowerCase();
        List<AutocompleteSuggestion> suggestions = new ArrayList<>();
        
        try {
            // 1. Search product names
            String regex = "^" + Pattern.quote(searchQuery);
            Query productQuery = new Query(Criteria.where("productName").regex(regex, "i"))
                .limit(limit);
            List<Products> products = mongoTemplate.find(productQuery, Products.class);
            
            for (Products p : products) {
                AutocompleteSuggestion suggestion = new AutocompleteSuggestion(
                    p.getProductName(), "PRODUCT_NAME", 1
                );
                suggestion.setHighlight(highlightMatch(p.getProductName(), searchQuery));
                suggestions.add(suggestion);
            }
            
            // 2. Search categories
            Query categoryQuery = new Query(Criteria.where("categoryName").regex(regex, "i"))
                .limit(3);
            List<ProductCategory> categories = mongoTemplate.find(categoryQuery, ProductCategory.class);
            
            for (ProductCategory c : categories) {
                AutocompleteSuggestion suggestion = new AutocompleteSuggestion(
                    c.getCategoryName(), "CATEGORY", 1
                );
                suggestion.setHighlight(highlightMatch(c.getCategoryName(), searchQuery));
                suggestions.add(suggestion);
            }
            
            // 3. Search districts (from products)
            List<String> districts = mongoTemplate.findDistinct(
                new Query(Criteria.where("originDistrict").regex(regex, "i")),
                "originDistrict", "products", String.class
            );
            for (String district : districts.stream().limit(3).collect(Collectors.toList())) {
                if (district != null) {
                    AutocompleteSuggestion suggestion = new AutocompleteSuggestion(
                        district, "DISTRICT", 1
                    );
                    suggestion.setHighlight(highlightMatch(district, searchQuery));
                    suggestions.add(suggestion);
                }
            }
            
            response.setSuggestions(suggestions.stream().limit(limit).collect(Collectors.toList()));
            
            // 4. Get matching products for preview
            if (products.size() > 0) {
                List<ProductSuggestion> productSuggestions = products.stream()
                    .limit(4)
                    .map(this::convertToProductSuggestion)
                    .collect(Collectors.toList());
                response.setProducts(productSuggestions);
            }
            
            // 5. Get matching vendors
            Query vendorQuery = new Query(
                new Criteria().orOperator(
                    Criteria.where("shoppeeName").regex(regex, "i"),
                    Criteria.where("locationDistrict").regex(regex, "i")
                )
            ).limit(3);
            List<Vendor> vendors = mongoTemplate.find(vendorQuery, Vendor.class);
            
            if (vendors.size() > 0) {
                List<VendorSuggestion> vendorSuggestions = vendors.stream()
                    .map(this::convertToVendorSuggestion)
                    .collect(Collectors.toList());
                response.setVendors(vendorSuggestions);
            }
            
            // Add popular searches
            response.setPopularSearches(POPULAR_SEARCHES.stream()
                .filter(s -> s.toLowerCase().contains(searchQuery))
                .limit(3)
                .collect(Collectors.toList()));
            
        } catch (Exception e) {
            logger.error("Autocomplete error: {}", e.getMessage());
        }
        
        return response;
    }

    /**
     * Get products by location (ODOP feature).
     */
    public SearchResponse searchByLocation(String district, String state, SearchRequest request) {
        request.setDistrict(district);
        request.setState(state);
        return search(request);
    }

    /**
     * Get GI-tagged products.
     */
    public SearchResponse searchGiTagged(SearchRequest request) {
        request.setGiTaggedOnly(true);
        return search(request);
    }

    // ================ PRIVATE HELPER METHODS ================

    private Query buildSearchQuery(String query, SearchRequest request) {
        Query searchQuery;
        
        // Try text search first
        try {
            TextCriteria textCriteria = TextCriteria.forDefaultLanguage()
                .matchingAny(query.split("\\s+"));
            searchQuery = TextQuery.queryText(textCriteria).sortByScore();
        } catch (Exception e) {
            // Fallback to regex search if text index not available
            logger.warn("Text search failed, using regex: {}", e.getMessage());
            searchQuery = new Query();
            Pattern pattern = Pattern.compile(Pattern.quote(query), Pattern.CASE_INSENSITIVE);
            searchQuery.addCriteria(new Criteria().orOperator(
                Criteria.where("productName").regex(pattern),
                Criteria.where("productDescription").regex(pattern),
                Criteria.where("tags").regex(pattern),
                Criteria.where("originDistrict").regex(pattern),
                Criteria.where("localName").regex(pattern)
            ));
        }
        
        // Apply filters
        addFilters(searchQuery, request);
        
        return searchQuery;
    }

    private void addFilters(Query query, SearchRequest request) {
        // Category filter
        if (request.getCategoryId() != null && !request.getCategoryId().isEmpty()) {
            query.addCriteria(Criteria.where("categoryId").is(request.getCategoryId()));
        }
        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            query.addCriteria(Criteria.where("categoryId").in(request.getCategoryIds()));
        }
        
        // Location filter
        if (request.getDistrict() != null && !request.getDistrict().isEmpty()) {
            query.addCriteria(Criteria.where("originDistrict")
                .regex(Pattern.quote(request.getDistrict()), "i"));
        }
        if (request.getState() != null && !request.getState().isEmpty()) {
            query.addCriteria(Criteria.where("originState")
                .regex(Pattern.quote(request.getState()), "i"));
        }
        
        // Price range
        if (request.getMinPrice() != null || request.getMaxPrice() != null) {
            Criteria priceCriteria = Criteria.where("price");
            if (request.getMinPrice() != null) {
                priceCriteria = priceCriteria.gte(request.getMinPrice());
            }
            if (request.getMaxPrice() != null) {
                priceCriteria = priceCriteria.lte(request.getMaxPrice());
            }
            query.addCriteria(priceCriteria);
        }
        
        // Rating filter
        if (request.getMinRating() != null) {
            query.addCriteria(Criteria.where("rating").gte(request.getMinRating()));
        }
        
        // GI Tag filter
        if (Boolean.TRUE.equals(request.getGiTaggedOnly())) {
            query.addCriteria(Criteria.where("giTagCertified").is(true));
        }
        
        // Stock filter
        if (Boolean.TRUE.equals(request.getInStockOnly())) {
            query.addCriteria(Criteria.where("stockStatus").is("In Stock"));
        }
        
        // Vendor filter
        if (request.getVendorId() != null && !request.getVendorId().isEmpty()) {
            query.addCriteria(Criteria.where("vendorId").is(request.getVendorId()));
        }
    }

    private void applyPaginationAndSort(Query query, SearchRequest request) {
        // Sorting
        Sort sort;
        switch (request.getSortBy()) {
            case "price_asc":
                sort = Sort.by(Sort.Direction.ASC, "price");
                break;
            case "price_desc":
                sort = Sort.by(Sort.Direction.DESC, "price");
                break;
            case "rating":
                sort = Sort.by(Sort.Direction.DESC, "rating");
                break;
            case "newest":
                sort = Sort.by(Sort.Direction.DESC, "createdAt");
                break;
            case "popular":
                sort = Sort.by(Sort.Direction.DESC, "totalSold");
                break;
            default:
                // relevance - text score already applied
                sort = Sort.by(Sort.Direction.DESC, "popularityScore");
        }
        
        query.with(PageRequest.of(request.getPage(), request.getSize(), sort));
    }

    private SearchResponse getAllProducts(SearchRequest request) {
        Query query = new Query();
        addFilters(query, request);
        
        long total = mongoTemplate.count(query, Products.class);
        applyPaginationAndSort(query, request);
        
        List<Products> products = mongoTemplate.find(query, Products.class);
        List<SearchResultItem> results = products.stream()
            .map(this::convertToSearchResult)
            .collect(Collectors.toList());
        
        return SearchResponse.success(results, total, request.getPage(), request.getSize());
    }

    private SearchResultItem convertToSearchResult(Products product) {
        // Get vendor name
        String vendorName = null;
        if (product.getVendorId() != null) {
            Vendor vendor = vendorRepository.findById(product.getVendorId()).orElse(null);
            if (vendor != null) {
                vendorName = vendor.getShoppeeName();
            }
        }
        
        // Get category name
        String categoryName = null;
        if (product.getCategoryId() != null) {
            ProductCategory category = categoryRepository.findById(product.getCategoryId()).orElse(null);
            if (category != null) {
                categoryName = category.getCategoryName();
            }
        }
        
        return SearchResultItem.builder()
            .id(product.getProductId())
            .type("PRODUCT")
            .name(product.getProductName())
            .description(truncateDescription(product.getProductDescription()))
            .imageUrl(product.getProductImageURL())
            .price((double) product.getPrice())
            .rating((double) product.getRating())
            .district(product.getOriginDistrict())
            .state(product.getOriginState())
            .categoryName(categoryName)
            .giTagCertified(product.getGiTagCertified())
            .vendorName(vendorName)
            .vendorId(product.getVendorId())
            .totalSold(product.getTotalSold())
            .stockStatus(product.getStockStatus())
            .build();
    }

    private SearchResultItem convertVendorToSearchResult(Vendor vendor) {
        return SearchResultItem.builder()
            .id(vendor.getVendorId())
            .type("VENDOR")
            .name(vendor.getShoppeeName())
            .description(truncateDescription(vendor.getBusinessDescription()))
            .imageUrl(vendor.getProfilePictureUrl())
            .rating(vendor.getRatings())
            .district(vendor.getLocationDistrict())
            .state(vendor.getLocationState())
            .shoppeeName(vendor.getShoppeeName())
            .isVerified(vendor.getIsVerified())
            .productCount(vendor.getProductCount())
            .build();
    }

    private ProductSuggestion convertToProductSuggestion(Products product) {
        ProductSuggestion suggestion = new ProductSuggestion();
        suggestion.setProductId(product.getProductId());
        suggestion.setProductName(product.getProductName());
        suggestion.setImageUrl(product.getProductImageURL());
        suggestion.setPrice((double) product.getPrice());
        suggestion.setRating((double) product.getRating());
        suggestion.setDistrict(product.getOriginDistrict());
        return suggestion;
    }

    private VendorSuggestion convertToVendorSuggestion(Vendor vendor) {
        VendorSuggestion suggestion = new VendorSuggestion();
        suggestion.setVendorId(vendor.getVendorId());
        suggestion.setShoppeeName(vendor.getShoppeeName());
        suggestion.setShopkeeperName(vendor.getShopkeeperName());
        suggestion.setDistrict(vendor.getLocationDistrict());
        suggestion.setState(vendor.getLocationState());
        suggestion.setRating(vendor.getRatings());
        suggestion.setIsVerified(vendor.getIsVerified());
        return suggestion;
    }

    private Map<String, List<FacetItem>> buildFacets(String query, SearchRequest request) {
        Map<String, List<FacetItem>> facets = new HashMap<>();
        
        try {
            // Category facets
            Aggregation categoryAgg = Aggregation.newAggregation(
                Aggregation.group("categoryId").count().as("count"),
                Aggregation.sort(Sort.Direction.DESC, "count"),
                Aggregation.limit(10)
            );
            
            List<Map<String, Object>> categoryResults = mongoTemplate.aggregate(
                categoryAgg, "products", Map.class
            ).getMappedResults().stream()
                .map(m -> (Map<String, Object>) m)
                .collect(Collectors.toList());
            
            List<FacetItem> categoryFacets = new ArrayList<>();
            for (Map<String, Object> result : categoryResults) {
                String categoryId = (String) result.get("_id");
                if (categoryId != null) {
                    ProductCategory cat = categoryRepository.findById(categoryId).orElse(null);
                    String label = cat != null ? cat.getCategoryName() : categoryId;
                    categoryFacets.add(new FacetItem(categoryId, label, ((Number) result.get("count")).longValue()));
                }
            }
            facets.put("categories", categoryFacets);
            
            // State facets
            Aggregation stateAgg = Aggregation.newAggregation(
                Aggregation.group("originState").count().as("count"),
                Aggregation.sort(Sort.Direction.DESC, "count"),
                Aggregation.limit(10)
            );
            
            List<Map<String, Object>> stateResults = mongoTemplate.aggregate(
                stateAgg, "products", Map.class
            ).getMappedResults().stream()
                .map(m -> (Map<String, Object>) m)
                .collect(Collectors.toList());
            
            List<FacetItem> stateFacets = stateResults.stream()
                .filter(r -> r.get("_id") != null)
                .map(r -> new FacetItem(
                    (String) r.get("_id"),
                    (String) r.get("_id"),
                    ((Number) r.get("count")).longValue()
                ))
                .collect(Collectors.toList());
            facets.put("states", stateFacets);
            
            // Price range facets
            facets.put("priceRanges", Arrays.asList(
                new FacetItem("0-500", "Under ₹500", countByPriceRange(0, 500)),
                new FacetItem("500-1000", "₹500 - ₹1000", countByPriceRange(500, 1000)),
                new FacetItem("1000-5000", "₹1000 - ₹5000", countByPriceRange(1000, 5000)),
                new FacetItem("5000+", "Above ₹5000", countByPriceRange(5000, Double.MAX_VALUE))
            ));
            
        } catch (Exception e) {
            logger.warn("Error building facets: {}", e.getMessage());
        }
        
        return facets;
    }

    private long countByPriceRange(double min, double max) {
        Query query = new Query(Criteria.where("price").gte(min).lt(max));
        return mongoTemplate.count(query, Products.class);
    }

    private List<String> getRelatedSuggestions(String query) {
        // Simple related search suggestions
        List<String> suggestions = new ArrayList<>();
        
        // Add popular searches that might be related
        for (String popular : POPULAR_SEARCHES) {
            if (popular.contains(query.toLowerCase()) || query.toLowerCase().contains(popular.split(" ")[0])) {
                suggestions.add(popular);
            }
        }
        
        return suggestions.stream().limit(5).collect(Collectors.toList());
    }

    private String highlightMatch(String text, String query) {
        if (text == null || query == null) return text;
        String pattern = "(?i)(" + Pattern.quote(query) + ")";
        return text.replaceAll(pattern, "<mark>$1</mark>");
    }

    private String truncateDescription(String description) {
        if (description == null) return null;
        if (description.length() <= 150) return description;
        return description.substring(0, 147) + "...";
    }

    private void trackSearch(String query) {
        searchCountCache.merge(query.toLowerCase(), 1L, Long::sum);
    }

    /**
     * Get popular searches (for analytics).
     */
    public List<String> getPopularSearches(int limit) {
        return searchCountCache.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(limit)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }
}
