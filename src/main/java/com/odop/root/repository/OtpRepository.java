package com.odop.root.repository;

import com.odop.root.models.Otp;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for OTP entity
 */
@Repository
public interface OtpRepository extends MongoRepository<Otp, String> {

    // Find latest OTP for a phone number
    Optional<Otp> findTopByPhoneNumberAndPurposeAndVerifiedFalseAndExpiredFalseOrderByCreatedAtDesc(
            String phoneNumber, String purpose);

    // Find by phone and OTP code
    Optional<Otp> findByPhoneNumberAndOtpCodeAndVerifiedFalseAndExpiredFalse(
            String phoneNumber, String otpCode);

    // Find all OTPs for a phone number
    List<Otp> findByPhoneNumberOrderByCreatedAtDesc(String phoneNumber);

    // Find unverified OTPs for a phone number
    List<Otp> findByPhoneNumberAndVerifiedFalse(String phoneNumber);

    // Find OTPs created after a certain time (for rate limiting)
    List<Otp> findByPhoneNumberAndCreatedAtAfter(String phoneNumber, LocalDateTime afterTime);

    // Count recent OTPs for rate limiting
    long countByPhoneNumberAndCreatedAtAfter(String phoneNumber, LocalDateTime afterTime);

    // Find verified OTPs
    List<Otp> findByPhoneNumberAndVerifiedTrue(String phoneNumber);

    // Find by user
    List<Otp> findByUserIdOrderByCreatedAtDesc(String userId);

    // Delete expired OTPs (cleanup)
    void deleteByExpiresAtBefore(LocalDateTime dateTime);

    // Delete all OTPs for a phone number
    void deleteByPhoneNumber(String phoneNumber);
}
