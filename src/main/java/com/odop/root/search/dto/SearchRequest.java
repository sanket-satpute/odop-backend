package com.odop.root.search.dto;

import java.util.List;

/**
 * Request DTO for search operations.
 */
public class SearchRequest {
    
    private String query;                    // Search query text
    private String searchType;               // PRODUCTS, VENDORS, ALL
    
    // Filters
    private String categoryId;
    private List<String> categoryIds;
    private String district;
    private String state;
    private Double minPrice;
    private Double maxPrice;
    private Integer minRating;
    private Boolean giTaggedOnly;
    private Boolean inStockOnly;
    private String vendorId;
    
    // Pagination
    private int page = 0;
    private int size = 20;
    
    // Sorting
    private String sortBy = "relevance";     // relevance, price_asc, price_desc, rating, newest
    private String sortDirection = "desc";
    
    // Constructors
    public SearchRequest() {}
    
    public SearchRequest(String query) {
        this.query = query;
    }
    
    // Getters and Setters
    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }
    
    public String getSearchType() { return searchType; }
    public void setSearchType(String searchType) { this.searchType = searchType; }
    
    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }
    
    public List<String> getCategoryIds() { return categoryIds; }
    public void setCategoryIds(List<String> categoryIds) { this.categoryIds = categoryIds; }
    
    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }
    
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    
    public Double getMinPrice() { return minPrice; }
    public void setMinPrice(Double minPrice) { this.minPrice = minPrice; }
    
    public Double getMaxPrice() { return maxPrice; }
    public void setMaxPrice(Double maxPrice) { this.maxPrice = maxPrice; }
    
    public Integer getMinRating() { return minRating; }
    public void setMinRating(Integer minRating) { this.minRating = minRating; }
    
    public Boolean getGiTaggedOnly() { return giTaggedOnly; }
    public void setGiTaggedOnly(Boolean giTaggedOnly) { this.giTaggedOnly = giTaggedOnly; }
    
    public Boolean getInStockOnly() { return inStockOnly; }
    public void setInStockOnly(Boolean inStockOnly) { this.inStockOnly = inStockOnly; }
    
    public String getVendorId() { return vendorId; }
    public void setVendorId(String vendorId) { this.vendorId = vendorId; }
    
    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }
    
    public int getSize() { return size; }
    public void setSize(int size) { this.size = Math.min(size, 100); } // Max 100 results
    
    public String getSortBy() { return sortBy; }
    public void setSortBy(String sortBy) { this.sortBy = sortBy; }
    
    public String getSortDirection() { return sortDirection; }
    public void setSortDirection(String sortDirection) { this.sortDirection = sortDirection; }
}
