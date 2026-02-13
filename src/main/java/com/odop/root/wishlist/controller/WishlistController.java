package com.odop.root.wishlist.controller;

import com.odop.root.wishlist.dto.*;
import com.odop.root.wishlist.service.WishlistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/odop/wishlist")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class WishlistController {

    private final WishlistService wishlistService;

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Wishlist API");
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<WishlistResponse> getWishlist(Authentication auth) {
        String customerId = auth.getName();
        log.info("Getting wishlist for customer: {}", customerId);
        WishlistResponse response = wishlistService.getWishlist(customerId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{productId}")
    public ResponseEntity<WishlistActionResponse> addToWishlist(
            @PathVariable String productId, Authentication auth) {
        String customerId = auth.getName();
        log.info("Adding product {} to wishlist for customer {}", productId, customerId);
        WishlistActionResponse response = wishlistService.addToWishlist(customerId, productId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<WishlistActionResponse> removeFromWishlist(
            @PathVariable String productId, Authentication auth) {
        String customerId = auth.getName();
        log.info("Removing product {} from wishlist for customer {}", productId, customerId);
        WishlistActionResponse response = wishlistService.removeFromWishlist(customerId, productId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/check/{productId}")
    public ResponseEntity<Map<String, Object>> checkInWishlist(
            @PathVariable String productId, Authentication auth) {
        String customerId = auth.getName();
        boolean inWishlist = wishlistService.isInWishlist(customerId, productId);
        Map<String, Object> response = new HashMap<>();
        response.put("productId", productId);
        response.put("inWishlist", inWishlist);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/clear")
    public ResponseEntity<WishlistActionResponse> clearWishlist(Authentication auth) {
        String customerId = auth.getName();
        log.info("Clearing wishlist for customer: {}", customerId);
        WishlistActionResponse response = wishlistService.clearWishlist(customerId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Object>> getWishlistCount(Authentication auth) {
        String customerId = auth.getName();
        int count = wishlistService.getWishlistCount(customerId);
        Map<String, Object> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }
}
