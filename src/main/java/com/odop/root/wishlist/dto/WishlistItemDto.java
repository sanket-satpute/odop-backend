package com.odop.root.wishlist.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WishlistItemDto {
    private String productId;
    private String productName;
    private String productImageURL;
    private double price;
    private int discount;
    private double discountedPrice;
    private String stockStatus;
    private boolean inStock;
    private String vendorId;
    private String vendorName;
    private String originDistrict;
    private String originState;
    private String category;
    private Boolean giTagCertified;
    private int rating;
    private String addedAt;
}
