package com.odop.root.search.dto;

import java.util.List;
import java.util.Map;

/**
 * DTO for individual search result item
 * Includes highlighting and score information
 */
public class SearchResultItem {

    private String id;
    private String type; // product, vendor
    private Double score;
    private Map<String, List<String>> highlights;

    // Product fields
    private String productName;
    private String productDescription;
    private String productImageURL;
    private Double price;
    private Double discountedPrice;
    private Integer discount;
    private Integer rating;
    private String categoryName;
    private String vendorName;
    private String shopName;
    private String stockStatus;
    private List<String> tags;

    // ODOP fields
    private String originDistrict;
    private String originState;
    private Boolean giTagCertified;
    private String craftType;
    private String localName;

    // Vendor fields (when type = vendor)
    private String shoppeeName;
    private String businessDescription;
    private String locationDistrict;
    private String locationState;
    private Integer productCount;
    private Boolean verified;
    private String profilePictureUrl;

    // Constructors
    public SearchResultItem() {}

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }

    public Map<String, List<String>> getHighlights() { return highlights; }
    public void setHighlights(Map<String, List<String>> highlights) { this.highlights = highlights; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getProductDescription() { return productDescription; }
    public void setProductDescription(String productDescription) { this.productDescription = productDescription; }

    public String getProductImageURL() { return productImageURL; }
    public void setProductImageURL(String productImageURL) { this.productImageURL = productImageURL; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public Double getDiscountedPrice() { return discountedPrice; }
    public void setDiscountedPrice(Double discountedPrice) { this.discountedPrice = discountedPrice; }

    public Integer getDiscount() { return discount; }
    public void setDiscount(Integer discount) { this.discount = discount; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getVendorName() { return vendorName; }
    public void setVendorName(String vendorName) { this.vendorName = vendorName; }

    public String getShopName() { return shopName; }
    public void setShopName(String shopName) { this.shopName = shopName; }

    public String getStockStatus() { return stockStatus; }
    public void setStockStatus(String stockStatus) { this.stockStatus = stockStatus; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    public String getOriginDistrict() { return originDistrict; }
    public void setOriginDistrict(String originDistrict) { this.originDistrict = originDistrict; }

    public String getOriginState() { return originState; }
    public void setOriginState(String originState) { this.originState = originState; }

    public Boolean getGiTagCertified() { return giTagCertified; }
    public void setGiTagCertified(Boolean giTagCertified) { this.giTagCertified = giTagCertified; }

    public String getCraftType() { return craftType; }
    public void setCraftType(String craftType) { this.craftType = craftType; }

    public String getLocalName() { return localName; }
    public void setLocalName(String localName) { this.localName = localName; }

    public String getShoppeeName() { return shoppeeName; }
    public void setShoppeeName(String shoppeeName) { this.shoppeeName = shoppeeName; }

    public String getBusinessDescription() { return businessDescription; }
    public void setBusinessDescription(String businessDescription) { this.businessDescription = businessDescription; }

    public String getLocationDistrict() { return locationDistrict; }
    public void setLocationDistrict(String locationDistrict) { this.locationDistrict = locationDistrict; }

    public String getLocationState() { return locationState; }
    public void setLocationState(String locationState) { this.locationState = locationState; }

    public Integer getProductCount() { return productCount; }
    public void setProductCount(Integer productCount) { this.productCount = productCount; }

    public Boolean getVerified() { return verified; }
    public void setVerified(Boolean verified) { this.verified = verified; }

    public String getProfilePictureUrl() { return profilePictureUrl; }
    public void setProfilePictureUrl(String profilePictureUrl) { this.profilePictureUrl = profilePictureUrl; }
}
