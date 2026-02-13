package com.odop.root.wishlist.service;

import com.odop.root.models.*;
import com.odop.root.repository.*;
import com.odop.root.wishlist.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class WishlistService {

    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final VendorRepository vendorRepository;

    public WishlistResponse getWishlist(String customerId) {
        Customer customer = getCustomer(customerId);
        List<String> productIds = customer.getWishlistProductIds();
        
        if (productIds == null || productIds.isEmpty()) {
            return WishlistResponse.builder()
                .customerId(customerId)
                .totalItems(0)
                .items(new ArrayList<>())
                .totalValue(0)
                .build();
        }

        List<WishlistItemDto> items = new ArrayList<>();
        double totalValue = 0;

        for (String productId : productIds) {
            Products product = productRepository.findByProductId(productId);
            if (product != null) {
                WishlistItemDto item = mapToDto(product);
                items.add(item);
                totalValue += item.getDiscountedPrice();
            }
        }

        return WishlistResponse.builder()
            .customerId(customerId)
            .totalItems(items.size())
            .items(items)
            .totalValue(Math.round(totalValue * 100.0) / 100.0)
            .build();
    }

    public WishlistActionResponse addToWishlist(String customerId, String productId) {
        Customer customer = getCustomer(customerId);
        Products product = productRepository.findByProductId(productId);
        
        if (product == null) {
            return WishlistActionResponse.builder()
                .success(false)
                .message("Product not found")
                .productId(productId)
                .action("ADD")
                .build();
        }

        List<String> wishlist = customer.getWishlistProductIds();
        if (wishlist == null) {
            wishlist = new ArrayList<>();
        }

        if (wishlist.contains(productId)) {
            return WishlistActionResponse.builder()
                .success(false)
                .message("Product already in wishlist")
                .productId(productId)
                .action("ADD")
                .wishlistCount(wishlist.size())
                .build();
        }

        wishlist.add(productId);
        customer.setWishlistProductIds(wishlist);
        customer.setUpdatedAt(LocalDateTime.now());
        customerRepository.save(customer);

        log.info("Added product {} to wishlist for customer {}", productId, customerId);

        return WishlistActionResponse.builder()
            .success(true)
            .message("Product added to wishlist")
            .productId(productId)
            .action("ADD")
            .wishlistCount(wishlist.size())
            .build();
    }

    public WishlistActionResponse removeFromWishlist(String customerId, String productId) {
        Customer customer = getCustomer(customerId);
        List<String> wishlist = customer.getWishlistProductIds();

        if (wishlist == null || !wishlist.contains(productId)) {
            return WishlistActionResponse.builder()
                .success(false)
                .message("Product not in wishlist")
                .productId(productId)
                .action("REMOVE")
                .wishlistCount(wishlist != null ? wishlist.size() : 0)
                .build();
        }

        wishlist.remove(productId);
        customer.setWishlistProductIds(wishlist);
        customer.setUpdatedAt(LocalDateTime.now());
        customerRepository.save(customer);

        log.info("Removed product {} from wishlist for customer {}", productId, customerId);

        return WishlistActionResponse.builder()
            .success(true)
            .message("Product removed from wishlist")
            .productId(productId)
            .action("REMOVE")
            .wishlistCount(wishlist.size())
            .build();
    }

    public boolean isInWishlist(String customerId, String productId) {
        Customer customer = getCustomer(customerId);
        List<String> wishlist = customer.getWishlistProductIds();
        return wishlist != null && wishlist.contains(productId);
    }

    public WishlistActionResponse clearWishlist(String customerId) {
        Customer customer = getCustomer(customerId);
        customer.setWishlistProductIds(new ArrayList<>());
        customer.setUpdatedAt(LocalDateTime.now());
        customerRepository.save(customer);

        return WishlistActionResponse.builder()
            .success(true)
            .message("Wishlist cleared")
            .action("CLEAR")
            .wishlistCount(0)
            .build();
    }

    public int getWishlistCount(String customerId) {
        Customer customer = getCustomer(customerId);
        List<String> wishlist = customer.getWishlistProductIds();
        return wishlist != null ? wishlist.size() : 0;
    }

    /**
     * Get customer by ID or email address.
     * JWT token contains email as subject, so we need to support both lookup methods.
     */
    private Customer getCustomer(String customerIdOrEmail) {
        // First try to find by customerId
        Customer customer = customerRepository.findByCustomerId(customerIdOrEmail);
        
        // If not found, try to find by email address (JWT subject is email)
        if (customer == null) {
            customer = customerRepository.findByEmailAddress(customerIdOrEmail);
        }
        
        if (customer == null) {
            throw new RuntimeException("Customer not found: " + customerIdOrEmail);
        }
        return customer;
    }

    private WishlistItemDto mapToDto(Products product) {
        double discountedPrice = product.getPrice() * (1 - product.getDiscount() / 100.0);
        
        String vendorName = null;
        if (product.getVendorId() != null) {
            Vendor vendor = vendorRepository.findByVendorId(product.getVendorId());
            if (vendor != null) {
                if (vendor.getShoppeeName() != null && !vendor.getShoppeeName().isBlank()) {
                    vendorName = vendor.getShoppeeName();
                } else {
                    vendorName = vendor.getShopkeeperName();
                }
            }
        }

        boolean inStock = product.getProductQuantity() > 0 && 
            !"Out of Stock".equalsIgnoreCase(product.getStockStatus());

        LocalDateTime addedAt = product.getUpdatedAt() != null ? product.getUpdatedAt() : product.getCreatedAt();
        String category = product.getSubCategoryId() != null && !product.getSubCategoryId().isBlank()
            ? product.getSubCategoryId()
            : product.getCategoryId();

        return WishlistItemDto.builder()
            .productId(product.getProductId())
            .productName(product.getProductName())
            .productImageURL(product.getProductImageURL())
            .price(product.getPrice())
            .discount(product.getDiscount())
            .discountedPrice(Math.round(discountedPrice * 100.0) / 100.0)
            .stockStatus(product.getStockStatus())
            .inStock(inStock)
            .vendorId(product.getVendorId())
            .vendorName(vendorName)
            .originDistrict(product.getOriginDistrict())
            .originState(product.getOriginState())
            .category(category)
            .giTagCertified(product.getGiTagCertified())
            .rating(product.getRating())
            .addedAt(addedAt != null ? addedAt.format(DateTimeFormatter.ISO_DATE_TIME) : null)
            .build();
    }
}
