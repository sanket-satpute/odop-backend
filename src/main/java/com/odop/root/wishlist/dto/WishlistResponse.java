package com.odop.root.wishlist.dto;

import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WishlistResponse {
    private String customerId;
    private int totalItems;
    private List<WishlistItemDto> items;
    private double totalValue;
}
