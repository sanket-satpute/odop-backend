package com.odop.root.coupon.repository;

import com.odop.root.coupon.model.Coupon;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CouponRepository extends MongoRepository<Coupon, String> {
    Coupon findByCode(String code);
    Coupon findByCodeIgnoreCase(String code);
    List<Coupon> findByIsActiveTrue();
    List<Coupon> findByIsActiveTrueAndValidFromBeforeAndValidUntilAfter(
        LocalDateTime now1, LocalDateTime now2);
    boolean existsByCode(String code);
}
