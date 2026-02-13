package com.odop.root.search.dto;

/**
 * Individual search result item.
 */
public class SearchResultItem {
    
    private String id;
    private String type;                 // PRODUCT, VENDOR, CATEGORY
    private String name;
    private String description;
    private String imageUrl;
    private Double price;
    private Double rating;
    private String district;
    private String state;
    private String categoryName;
    private Boolean giTagCertified;
    private String vendorName;
    private String vendorId;
    private Integer totalSold;
    private String stockStatus;
    private Double score;                // Search relevance score
    
    // For vendor results
    private String shoppeeName;
    private Boolean isVerified;
    private Integer productCount;
    
    // Constructors
    public SearchResultItem() {}
    
    // Builder pattern for easy construction
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private final SearchResultItem item = new SearchResultItem();
        
        public Builder id(String id) { item.id = id; return this; }
        public Builder type(String type) { item.type = type; return this; }
        public Builder name(String name) { item.name = name; return this; }
        public Builder description(String description) { item.description = description; return this; }
        public Builder imageUrl(String imageUrl) { item.imageUrl = imageUrl; return this; }
        public Builder price(Double price) { item.price = price; return this; }
        public Builder rating(Double rating) { item.rating = rating; return this; }
        public Builder district(String district) { item.district = district; return this; }
        public Builder state(String state) { item.state = state; return this; }
        public Builder categoryName(String categoryName) { item.categoryName = categoryName; return this; }
        public Builder giTagCertified(Boolean giTagCertified) { item.giTagCertified = giTagCertified; return this; }
        public Builder vendorName(String vendorName) { item.vendorName = vendorName; return this; }
        public Builder vendorId(String vendorId) { item.vendorId = vendorId; return this; }
        public Builder totalSold(Integer totalSold) { item.totalSold = totalSold; return this; }
        public Builder stockStatus(String stockStatus) { item.stockStatus = stockStatus; return this; }
        public Builder score(Double score) { item.score = score; return this; }
        public Builder shoppeeName(String shoppeeName) { item.shoppeeName = shoppeeName; return this; }
        public Builder isVerified(Boolean isVerified) { item.isVerified = isVerified; return this; }
        public Builder productCount(Integer productCount) { item.productCount = productCount; return this; }
        
        public SearchResultItem build() { return item; }
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    
    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }
    
    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }
    
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    
    public Boolean getGiTagCertified() { return giTagCertified; }
    public void setGiTagCertified(Boolean giTagCertified) { this.giTagCertified = giTagCertified; }
    
    public String getVendorName() { return vendorName; }
    public void setVendorName(String vendorName) { this.vendorName = vendorName; }
    
    public String getVendorId() { return vendorId; }
    public void setVendorId(String vendorId) { this.vendorId = vendorId; }
    
    public Integer getTotalSold() { return totalSold; }
    public void setTotalSold(Integer totalSold) { this.totalSold = totalSold; }
    
    public String getStockStatus() { return stockStatus; }
    public void setStockStatus(String stockStatus) { this.stockStatus = stockStatus; }
    
    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }
    
    public String getShoppeeName() { return shoppeeName; }
    public void setShoppeeName(String shoppeeName) { this.shoppeeName = shoppeeName; }
    
    public Boolean getIsVerified() { return isVerified; }
    public void setIsVerified(Boolean isVerified) { this.isVerified = isVerified; }
    
    public Integer getProductCount() { return productCount; }
    public void setProductCount(Integer productCount) { this.productCount = productCount; }
}
