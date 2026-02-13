package com.odop.root.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.odop.root.models.Cart;
import com.odop.root.services.CartService;

@RestController
@RequestMapping("odop/cart")
@CrossOrigin
public class CartController {

    @Autowired
    private CartService cartService;

    private static final Logger logger = LogManager.getLogger(CartController.class);

    @PostMapping("/save_cart")
    public Cart saveCart(@RequestBody Cart cart) {
        cart.setCreatedAt(LocalDateTime.now());
        return this.cartService.saveCart(cart);
    }

    @GetMapping("/get_all_carts")
    public List<Cart> getAllCarts() {
        return this.cartService.getAllCarts();
    }

    @GetMapping("/get_cart_id/{id}")
    public Cart getCartById(@PathVariable("id") String uid) {
        return this.cartService.getCartById(uid);
    }

    @GetMapping("/approve_cart_id/{id}")
    public Cart approveCartById(@PathVariable("id") String uid) {
        Cart cart = this.cartService.getCartById(uid);
        cart.setApproval(true);
        return saveCart(cart);
    }

    @GetMapping("/get_cart_vendor_id/{vendorId}")
    public List<Cart> getCartByVendorId(@PathVariable("vendorId") String vendorId) {
        return this.cartService.getCartByVendorId(vendorId);
    }

    @GetMapping("/get_cart_customer_id/{customerId}")
    public List<Cart> getCartByCustomerId(@PathVariable("customerId") String customerId) {
        return this.cartService.getCartByCustomerId(customerId);
    }

    @GetMapping("/get_cart_product_id/{productId}")
    public List<Cart> getCartByProductId(@PathVariable("productId") String productId) {
        return this.cartService.getByProductId(productId);
    }

    @GetMapping("/get_cart_approval/{approval}")
    public List<Cart> getCartByApproval(@PathVariable("approval") String approval) {
        return this.cartService.getCartByVendorId(approval);
    }

    @DeleteMapping("/delete_by_id/{id}")
    public ResponseEntity<Boolean> deleteCartById(@PathVariable("id") String id) {
        logger.debug("Cart id : {}", id);
        return ResponseEntity.ok(this.cartService.deleteById(id));
    }
}