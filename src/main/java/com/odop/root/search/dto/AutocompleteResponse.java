package com.odop.root.search.dto;

import java.util.List;

/**
 * Response DTO for autocomplete suggestions.
 */
public class AutocompleteResponse {
    
    private String query;
    private List<AutocompleteSuggestion> suggestions;
    private List<ProductSuggestion> products;
    private List<VendorSuggestion> vendors;
    private List<String> recentSearches;
    private List<String> popularSearches;
    
    // Constructors
    public AutocompleteResponse() {}
    
    public AutocompleteResponse(String query) {
        this.query = query;
    }
    
    // Nested class for text suggestions
    public static class AutocompleteSuggestion {
        private String text;
        private String type;        // PRODUCT_NAME, CATEGORY, DISTRICT, VENDOR
        private String highlight;   // HTML with <mark> tags
        private long matchCount;
        
        public AutocompleteSuggestion() {}
        
        public AutocompleteSuggestion(String text, String type, long matchCount) {
            this.text = text;
            this.type = type;
            this.matchCount = matchCount;
        }
        
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
        
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        
        public String getHighlight() { return highlight; }
        public void setHighlight(String highlight) { this.highlight = highlight; }
        
        public long getMatchCount() { return matchCount; }
        public void setMatchCount(long matchCount) { this.matchCount = matchCount; }
    }
    
    // Nested class for product suggestions
    public static class ProductSuggestion {
        private String productId;
        private String productName;
        private String imageUrl;
        private Double price;
        private Double rating;
        private String district;
        
        public ProductSuggestion() {}
        
        public String getProductId() { return productId; }
        public void setProductId(String productId) { this.productId = productId; }
        
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        
        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
        
        public Double getPrice() { return price; }
        public void setPrice(Double price) { this.price = price; }
        
        public Double getRating() { return rating; }
        public void setRating(Double rating) { this.rating = rating; }
        
        public String getDistrict() { return district; }
        public void setDistrict(String district) { this.district = district; }
    }
    
    // Nested class for vendor suggestions
    public static class VendorSuggestion {
        private String vendorId;
        private String shoppeeName;
        private String shopkeeperName;
        private String district;
        private String state;
        private Double rating;
        private Boolean isVerified;
        
        public VendorSuggestion() {}
        
        public String getVendorId() { return vendorId; }
        public void setVendorId(String vendorId) { this.vendorId = vendorId; }
        
        public String getShoppeeName() { return shoppeeName; }
        public void setShoppeeName(String shoppeeName) { this.shoppeeName = shoppeeName; }
        
        public String getShopkeeperName() { return shopkeeperName; }
        public void setShopkeeperName(String shopkeeperName) { this.shopkeeperName = shopkeeperName; }
        
        public String getDistrict() { return district; }
        public void setDistrict(String district) { this.district = district; }
        
        public String getState() { return state; }
        public void setState(String state) { this.state = state; }
        
        public Double getRating() { return rating; }
        public void setRating(Double rating) { this.rating = rating; }
        
        public Boolean getIsVerified() { return isVerified; }
        public void setIsVerified(Boolean isVerified) { this.isVerified = isVerified; }
    }
    
    // Getters and Setters
    public String getQuery() { return query; }
    public void setQuery(String query) { this.query = query; }
    
    public List<AutocompleteSuggestion> getSuggestions() { return suggestions; }
    public void setSuggestions(List<AutocompleteSuggestion> suggestions) { this.suggestions = suggestions; }
    
    public List<ProductSuggestion> getProducts() { return products; }
    public void setProducts(List<ProductSuggestion> products) { this.products = products; }
    
    public List<VendorSuggestion> getVendors() { return vendors; }
    public void setVendors(List<VendorSuggestion> vendors) { this.vendors = vendors; }
    
    public List<String> getRecentSearches() { return recentSearches; }
    public void setRecentSearches(List<String> recentSearches) { this.recentSearches = recentSearches; }
    
    public List<String> getPopularSearches() { return popularSearches; }
    public void setPopularSearches(List<String> popularSearches) { this.popularSearches = popularSearches; }
}
