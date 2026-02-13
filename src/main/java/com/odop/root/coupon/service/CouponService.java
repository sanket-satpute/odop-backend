package com.odop.root.coupon.service;

import com.odop.root.coupon.dto.*;
import com.odop.root.coupon.model.*;
import com.odop.root.coupon.repository.*;
import com.odop.root.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CouponService {

    private final CouponRepository couponRepository;
    private final CouponUsageRepository usageRepository;
    private final OrderRepository orderRepository;

    public Coupon createCoupon(CouponDto dto, String adminId) {
        if (couponRepository.existsByCode(dto.getCode().toUpperCase())) {
            throw new RuntimeException("Coupon code already exists");
        }

        Coupon coupon = Coupon.builder()
            .code(dto.getCode().toUpperCase())
            .description(dto.getDescription())
            .discountType(dto.getDiscountType())
            .discountValue(dto.getDiscountValue())
            .maxDiscountAmount(dto.getMaxDiscountAmount())
            .validFrom(dto.getValidFrom())
            .validUntil(dto.getValidUntil())
            .isActive(dto.isActive())
            .totalUsageLimit(dto.getTotalUsageLimit())
            .usagePerCustomer(dto.getUsagePerCustomer())
            .currentUsageCount(0)
            .minOrderAmount(dto.getMinOrderAmount())
            .applicableCategories(dto.getApplicableCategories())
            .applicableStates(dto.getApplicableStates())
            .firstOrderOnly(dto.isFirstOrderOnly())
            .createdBy(adminId)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        return couponRepository.save(coupon);
    }

    public List<Coupon> getAllCoupons() {
        return couponRepository.findAll();
    }

    public List<Coupon> getActiveCoupons() {
        LocalDateTime now = LocalDateTime.now();
        return couponRepository.findByIsActiveTrueAndValidFromBeforeAndValidUntilAfter(now, now);
    }

    public Coupon getCouponByCode(String code) {
        return couponRepository.findByCodeIgnoreCase(code);
    }

    public CouponValidationResponse validateCoupon(String code, String customerId, 
            double cartTotal, String shippingState) {
        
        Coupon coupon = couponRepository.findByCodeIgnoreCase(code);
        
        if (coupon == null) {
            return invalid("Coupon not found");
        }
        if (!coupon.isActive()) {
            return invalid("Coupon is inactive");
        }
        
        LocalDateTime now = LocalDateTime.now();
        if (coupon.getValidFrom() != null && now.isBefore(coupon.getValidFrom())) {
            return invalid("Coupon is not yet valid");
        }
        if (coupon.getValidUntil() != null && now.isAfter(coupon.getValidUntil())) {
            return invalid("Coupon has expired");
        }
        
        if (coupon.getMinOrderAmount() > 0 && cartTotal < coupon.getMinOrderAmount()) {
            return invalid("Minimum order amount is ₹" + coupon.getMinOrderAmount());
        }
        
        if (coupon.getTotalUsageLimit() > 0 && 
            coupon.getCurrentUsageCount() >= coupon.getTotalUsageLimit()) {
            return invalid("Coupon usage limit reached");
        }
        
        if (coupon.getUsagePerCustomer() > 0) {
            long customerUsage = usageRepository.countByCouponCodeAndCustomerId(code, customerId);
            if (customerUsage >= coupon.getUsagePerCustomer()) {
                return invalid("You have already used this coupon");
            }
        }
        
        if (coupon.isFirstOrderOnly()) {
            long orderCount = orderRepository.findByCustomerId(customerId).size();
            if (orderCount > 0) {
                return invalid("Coupon valid for first order only");
            }
        }
        
        if (coupon.getApplicableStates() != null && !coupon.getApplicableStates().isEmpty()) {
            if (shippingState == null || !coupon.getApplicableStates().contains(shippingState)) {
                return invalid("Coupon not valid for your location");
            }
        }

        double discount = calculateDiscount(coupon, cartTotal);
        double finalAmount = cartTotal - discount;

        return CouponValidationResponse.builder()
            .valid(true)
            .couponCode(coupon.getCode())
            .message("Coupon applied successfully")
            .discountType(coupon.getDiscountType())
            .discountValue(coupon.getDiscountValue())
            .discountAmount(discount)
            .finalAmount(finalAmount)
            .build();
    }

    public void recordUsage(String couponCode, String customerId, String orderId, double discount) {
        CouponUsage usage = CouponUsage.builder()
            .couponCode(couponCode)
            .customerId(customerId)
            .orderId(orderId)
            .discountApplied(discount)
            .usedAt(LocalDateTime.now())
            .build();
        usageRepository.save(usage);

        Coupon coupon = couponRepository.findByCodeIgnoreCase(couponCode);
        if (coupon != null) {
            coupon.setCurrentUsageCount(coupon.getCurrentUsageCount() + 1);
            couponRepository.save(coupon);
        }
    }

    public Coupon updateCoupon(String code, CouponDto dto) {
        Coupon coupon = couponRepository.findByCodeIgnoreCase(code);
        if (coupon == null) {
            throw new RuntimeException("Coupon not found");
        }

        coupon.setDescription(dto.getDescription());
        coupon.setDiscountType(dto.getDiscountType());
        coupon.setDiscountValue(dto.getDiscountValue());
        coupon.setMaxDiscountAmount(dto.getMaxDiscountAmount());
        coupon.setValidFrom(dto.getValidFrom());
        coupon.setValidUntil(dto.getValidUntil());
        coupon.setActive(dto.isActive());
        coupon.setTotalUsageLimit(dto.getTotalUsageLimit());
        coupon.setUsagePerCustomer(dto.getUsagePerCustomer());
        coupon.setMinOrderAmount(dto.getMinOrderAmount());
        coupon.setUpdatedAt(LocalDateTime.now());

        return couponRepository.save(coupon);
    }

    public void deleteCoupon(String code) {
        Coupon coupon = couponRepository.findByCodeIgnoreCase(code);
        if (coupon != null) {
            couponRepository.delete(coupon);
        }
    }

    private double calculateDiscount(Coupon coupon, double cartTotal) {
        double discount = 0;
        
        switch (coupon.getDiscountType()) {
            case "PERCENTAGE":
                discount = cartTotal * (coupon.getDiscountValue() / 100);
                if (coupon.getMaxDiscountAmount() > 0) {
                    discount = Math.min(discount, coupon.getMaxDiscountAmount());
                }
                break;
            case "FIXED_AMOUNT":
                discount = coupon.getDiscountValue();
                break;
            case "FREE_SHIPPING":
                discount = 0; // Handled separately
                break;
        }
        
        return Math.round(discount * 100.0) / 100.0;
    }

    private CouponValidationResponse invalid(String message) {
        return CouponValidationResponse.builder()
            .valid(false)
            .message(message)
            .build();
    }
}
