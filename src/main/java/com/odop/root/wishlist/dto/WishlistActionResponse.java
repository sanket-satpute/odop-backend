package com.odop.root.wishlist.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WishlistActionResponse {
    private boolean success;
    private String message;
    private String productId;
    private String action;
    private int wishlistCount;
}
