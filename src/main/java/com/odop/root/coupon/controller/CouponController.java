package com.odop.root.coupon.controller;

import com.odop.root.coupon.dto.*;
import com.odop.root.coupon.model.Coupon;
import com.odop.root.coupon.service.CouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/odop/coupon")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class CouponController {

    private final CouponService couponService;

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> r = new HashMap<>();
        r.put("status", "UP");
        r.put("service", "Coupon API");
        return ResponseEntity.ok(r);
    }

    // ========== ADMIN ENDPOINTS ==========

    @PostMapping
    public ResponseEntity<Coupon> createCoupon(@RequestBody CouponDto dto, Authentication auth) {
        log.info("Creating coupon: {}", dto.getCode());
        Coupon coupon = couponService.createCoupon(dto, auth.getName());
        return ResponseEntity.ok(coupon);
    }

    @GetMapping
    public ResponseEntity<List<Coupon>> getAllCoupons() {
        return ResponseEntity.ok(couponService.getAllCoupons());
    }

    @GetMapping("/{code}")
    public ResponseEntity<Coupon> getCoupon(@PathVariable String code) {
        Coupon coupon = couponService.getCouponByCode(code);
        if (coupon == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(coupon);
    }

    @PutMapping("/{code}")
    public ResponseEntity<Coupon> updateCoupon(@PathVariable String code, @RequestBody CouponDto dto) {
        Coupon coupon = couponService.updateCoupon(code, dto);
        return ResponseEntity.ok(coupon);
    }

    @DeleteMapping("/{code}")
    public ResponseEntity<Map<String, String>> deleteCoupon(@PathVariable String code) {
        couponService.deleteCoupon(code);
        Map<String, String> r = new HashMap<>();
        r.put("message", "Coupon deleted");
        return ResponseEntity.ok(r);
    }

    // ========== CUSTOMER ENDPOINTS ==========

    @PostMapping("/validate")
    public ResponseEntity<CouponValidationResponse> validateCoupon(
            @RequestBody ApplyCouponRequest req, Authentication auth) {
        log.info("Validating coupon: {}", req.getCouponCode());
        CouponValidationResponse response = couponService.validateCoupon(
            req.getCouponCode(), auth.getName(), req.getCartTotal(), req.getShippingState());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/available")
    public ResponseEntity<List<Coupon>> getAvailableCoupons() {
        return ResponseEntity.ok(couponService.getActiveCoupons());
    }
}
