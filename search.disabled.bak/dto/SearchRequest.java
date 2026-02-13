package com.odop.root.search.dto;

import java.util.List;

/**
 * DTO for search request parameters
 * Supports full-text search with filters
 */
public class SearchRequest {

    // Main search query
    private String query;

    // Search type: products, vendors, all
    private String searchType = "products";

    // Pagination
    private int page = 0;
    private int size = 20;

    // Sorting
    private String sortBy = "relevance"; // relevance, price_asc, price_desc, rating, newest, popularity
    private String sortOrder = "desc";

    // Filters
    private List<String> categories;
    private List<String> subCategories;
    private Double minPrice;
    private Double maxPrice;
    private Integer minRating;
    private String state;
    private String district;
    private String pinCode;
    private Boolean giTagCertified;
    private Boolean inStock;
    private Boolean promotionEnabled;
    private List<String> tags;
    private String craftType;
    private String vendorId;

    // Search options
    private Boolean fuzzySearch = true;
    private Integer fuzzyMaxEdits = 2;
    private Boolean highlightResults = true;
    private Boolean includeAggregations = true;

    // Constructors
    public SearchRequest() {}

    public SearchRequest(String query) {
        this.query = query;
    }

    // Builder pattern for convenience
    public static SearchRequest query(String query) {
        return new SearchRequest(query);
    }

    public SearchRequest withCategory(String category) {
        this.categories = List.of(category);
        return this;
    }

    public SearchRequest withPriceRange(Double min, Double max) {
        this.minPrice = min;
        this.maxPrice = max;
        return this;
    }

    public SearchRequest withLocation(String state, String district) {
        this.state = state;
        this.district = district;
        return this;
    }

    public SearchRequest withPagination(int page, int size) {
        this.page = page;
        this.size = size;
        return this;
    }

    // Getters and Setters
    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }

    public String getSearchType() { return searchType; }
    public void setSearchType(String searchType) { this.searchType = searchType; }

    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }

    public int getSize() { return size; }
    public void setSize(int size) { this.size = Math.min(size, 100); } // Cap at 100

    public String getSortBy() { return sortBy; }
    public void setSortBy(String sortBy) { this.sortBy = sortBy; }

    public String getSortOrder() { return sortOrder; }
    public void setSortOrder(String sortOrder) { this.sortOrder = sortOrder; }

    public List<String> getCategories() { return categories; }
    public void setCategories(List<String> categories) { this.categories = categories; }

    public List<String> getSubCategories() { return subCategories; }
    public void setSubCategories(List<String> subCategories) { this.subCategories = subCategories; }

    public Double getMinPrice() { return minPrice; }
    public void setMinPrice(Double minPrice) { this.minPrice = minPrice; }

    public Double getMaxPrice() { return maxPrice; }
    public void setMaxPrice(Double maxPrice) { this.maxPrice = maxPrice; }

    public Integer getMinRating() { return minRating; }
    public void setMinRating(Integer minRating) { this.minRating = minRating; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }

    public String getPinCode() { return pinCode; }
    public void setPinCode(String pinCode) { this.pinCode = pinCode; }

    public Boolean getGiTagCertified() { return giTagCertified; }
    public void setGiTagCertified(Boolean giTagCertified) { this.giTagCertified = giTagCertified; }

    public Boolean getInStock() { return inStock; }
    public void setInStock(Boolean inStock) { this.inStock = inStock; }

    public Boolean getPromotionEnabled() { return promotionEnabled; }
    public void setPromotionEnabled(Boolean promotionEnabled) { this.promotionEnabled = promotionEnabled; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    public String getCraftType() { return craftType; }
    public void setCraftType(String craftType) { this.craftType = craftType; }

    public String getVendorId() { return vendorId; }
    public void setVendorId(String vendorId) { this.vendorId = vendorId; }

    public Boolean getFuzzySearch() { return fuzzySearch; }
    public void setFuzzySearch(Boolean fuzzySearch) { this.fuzzySearch = fuzzySearch; }

    public Integer getFuzzyMaxEdits() { return fuzzyMaxEdits; }
    public void setFuzzyMaxEdits(Integer fuzzyMaxEdits) { this.fuzzyMaxEdits = fuzzyMaxEdits; }

    public Boolean getHighlightResults() { return highlightResults; }
    public void setHighlightResults(Boolean highlightResults) { this.highlightResults = highlightResults; }

    public Boolean getIncludeAggregations() { return includeAggregations; }
    public void setIncludeAggregations(Boolean includeAggregations) { this.includeAggregations = includeAggregations; }
}
