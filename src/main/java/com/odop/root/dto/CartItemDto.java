package com.odop.root.dto;

import lombok.Data;

@Data
public class CartItemDto {
    private String productId;
    private String vendorId;
    private String productName;
    private String productImageURL;
    private int quantity;
    private double unitPrice;
    private double discount;
    private boolean selected;
    private double itemTotal;
}
