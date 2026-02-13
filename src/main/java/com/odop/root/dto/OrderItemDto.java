package com.odop.root.dto;

import lombok.Data;

@Data
public class OrderItemDto {
    private String productId;
    private String productName;
    private String productImageURL;
    private int quantity;
    private double unitPrice;
    private double discount;
    private double totalPrice;
}
