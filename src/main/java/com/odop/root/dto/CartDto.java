package com.odop.root.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CartDto {
    private String cartId;
    private String customerId;
    private List<CartItemDto> cartItems;
    private double totalAmount;
    private int totalItems;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
