package com.odop.root.coupon.repository;

import com.odop.root.coupon.model.CouponUsage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CouponUsageRepository extends MongoRepository<CouponUsage, String> {
    List<CouponUsage> findByCouponCode(String couponCode);
    List<CouponUsage> findByCustomerId(String customerId);
    List<CouponUsage> findByCouponCodeAndCustomerId(String couponCode, String customerId);
    long countByCouponCode(String couponCode);
    long countByCouponCodeAndCustomerId(String couponCode, String customerId);
}
