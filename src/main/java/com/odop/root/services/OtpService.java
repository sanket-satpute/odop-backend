package com.odop.root.services;

import com.odop.root.config.TwilioConfig;
import com.odop.root.dto.OtpResponse;
import com.odop.root.dto.SendOtpRequest;
import com.odop.root.dto.VerifyOtpRequest;
import com.odop.root.models.Otp;
import com.odop.root.repository.OtpRepository;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Service for OTP generation, sending via SMS, and verification
 */
@Service
public class OtpService {

    private static final Logger logger = LoggerFactory.getLogger(OtpService.class);
    private static final SecureRandom random = new SecureRandom();

    @Autowired
    private OtpRepository otpRepository;

    @Autowired
    private TwilioConfig twilioConfig;

    @Value("${otp.expiry-minutes:5}")
    private int otpExpiryMinutes;

    @Value("${otp.length:6}")
    private int otpLength;

    @Value("${otp.max-attempts:3}")
    private int maxAttempts;

    @Value("${otp.resend-cooldown-seconds:60}")
    private int resendCooldownSeconds;

    /**
     * Send OTP to phone number
     */
    public OtpResponse sendOtp(SendOtpRequest request, String ipAddress) {
        String phoneNumber = normalizePhoneNumber(request.getPhoneNumber());
        
        // Check rate limiting
        long recentOtpCount = otpRepository.countByPhoneNumberAndCreatedAtAfter(
                phoneNumber, LocalDateTime.now().minusMinutes(10));
        
        if (recentOtpCount >= 5) {
            return buildErrorResponse(phoneNumber, request.getPurpose(),
                    "Too many OTP requests. Please try again after 10 minutes.");
        }

        // Check resend cooldown
        Optional<Otp> lastOtp = otpRepository
                .findTopByPhoneNumberAndPurposeAndVerifiedFalseAndExpiredFalseOrderByCreatedAtDesc(
                        phoneNumber, request.getPurpose());
        
        if (lastOtp.isPresent()) {
            long secondsSinceLastOtp = Duration.between(lastOtp.get().getCreatedAt(), LocalDateTime.now()).getSeconds();
            if (secondsSinceLastOtp < resendCooldownSeconds) {
                long waitTime = resendCooldownSeconds - secondsSinceLastOtp;
                return OtpResponse.builder()
                        .success(false)
                        .message("Please wait before requesting another OTP")
                        .phoneNumber(phoneNumber)
                        .purpose(request.getPurpose())
                        .resendAvailableInSeconds(waitTime)
                        .timestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
                        .build();
            }
            // Expire the old OTP
            lastOtp.get().markExpired();
            otpRepository.save(lastOtp.get());
        }

        // Generate OTP
        String otpCode = generateOtp();

        // Create OTP entity
        Otp otp = Otp.builder()
                .phoneNumber(phoneNumber)
                .otpCode(otpCode)
                .purpose(request.getPurpose())
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(otpExpiryMinutes))
                .verified(false)
                .attempts(0)
                .userId(request.getUserId())
                .userType(request.getUserType())
                .expired(false)
                .ipAddress(ipAddress)
                .build();

        otpRepository.save(otp);

        // Send SMS
        boolean smsSent = sendSms(phoneNumber, otpCode, request.getPurpose());

        if (smsSent) {
            logger.info("OTP sent successfully to: {}", maskPhoneNumber(phoneNumber));
            return OtpResponse.builder()
                    .success(true)
                    .message("OTP sent successfully")
                    .phoneNumber(maskPhoneNumber(phoneNumber))
                    .purpose(request.getPurpose())
                    .expiryMinutes(otpExpiryMinutes)
                    .remainingAttempts(maxAttempts)
                    .resendAvailableInSeconds(resendCooldownSeconds)
                    .timestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
                    .build();
        } else {
            // For demo/testing when Twilio is not configured
            logger.warn("SMS not sent (Twilio not configured). OTP for testing: {}", otpCode);
            return OtpResponse.builder()
                    .success(true)
                    .message("OTP generated (SMS service not configured - check logs for OTP)")
                    .phoneNumber(maskPhoneNumber(phoneNumber))
                    .purpose(request.getPurpose())
                    .expiryMinutes(otpExpiryMinutes)
                    .remainingAttempts(maxAttempts)
                    .resendAvailableInSeconds(resendCooldownSeconds)
                    .timestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
                    .build();
        }
    }

    /**
     * Verify OTP
     */
    public OtpResponse verifyOtp(VerifyOtpRequest request) {
        String phoneNumber = normalizePhoneNumber(request.getPhoneNumber());

        // Find the OTP
        Optional<Otp> otpOptional = otpRepository
                .findTopByPhoneNumberAndPurposeAndVerifiedFalseAndExpiredFalseOrderByCreatedAtDesc(
                        phoneNumber, request.getPurpose());

        if (otpOptional.isEmpty()) {
            return buildErrorResponse(phoneNumber, request.getPurpose(),
                    "No active OTP found. Please request a new OTP.");
        }

        Otp otp = otpOptional.get();

        // Check if expired
        if (otp.isExpired()) {
            otp.markExpired();
            otpRepository.save(otp);
            return buildErrorResponse(phoneNumber, request.getPurpose(),
                    "OTP has expired. Please request a new OTP.");
        }

        // Check max attempts
        if (otp.isMaxAttemptsExceeded(maxAttempts)) {
            otp.markExpired();
            otpRepository.save(otp);
            return buildErrorResponse(phoneNumber, request.getPurpose(),
                    "Maximum verification attempts exceeded. Please request a new OTP.");
        }

        // Verify OTP code
        if (!otp.getOtpCode().equals(request.getOtpCode())) {
            otp.incrementAttempts();
            otpRepository.save(otp);
            int remaining = maxAttempts - otp.getAttempts();
            return OtpResponse.builder()
                    .success(false)
                    .verified(false)
                    .message("Invalid OTP. " + remaining + " attempts remaining.")
                    .phoneNumber(maskPhoneNumber(phoneNumber))
                    .purpose(request.getPurpose())
                    .remainingAttempts(remaining)
                    .timestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
                    .build();
        }

        // OTP is valid - mark as verified
        otp.markVerified();
        otpRepository.save(otp);

        logger.info("OTP verified successfully for: {}", maskPhoneNumber(phoneNumber));
        return OtpResponse.builder()
                .success(true)
                .verified(true)
                .message("OTP verified successfully")
                .phoneNumber(maskPhoneNumber(phoneNumber))
                .purpose(request.getPurpose())
                .timestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
                .build();
    }

    /**
     * Resend OTP
     */
    public OtpResponse resendOtp(SendOtpRequest request, String ipAddress) {
        // This is essentially the same as sendOtp with cooldown check
        return sendOtp(request, ipAddress);
    }

    /**
     * Check if phone is verified
     */
    public boolean isPhoneVerified(String phoneNumber, String purpose) {
        String normalized = normalizePhoneNumber(phoneNumber);
        Optional<Otp> verifiedOtp = otpRepository
                .findByPhoneNumberAndVerifiedTrue(normalized)
                .stream()
                .filter(otp -> otp.getPurpose().equals(purpose))
                .findFirst();
        return verifiedOtp.isPresent();
    }

    // ========== Helper Methods ==========

    /**
     * Generate random OTP
     */
    private String generateOtp() {
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < otpLength; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }

    /**
     * Send SMS via Twilio
     */
    private boolean sendSms(String to, String otpCode, String purpose) {
        if (!twilioConfig.isConfigured()) {
            logger.warn("Twilio not configured. Skipping SMS send.");
            return false;
        }

        try {
            String messageBody = buildSmsMessage(otpCode, purpose);
            
            Message message = Message.creator(
                    new PhoneNumber(to),
                    new PhoneNumber(twilioConfig.getPhoneNumber()),
                    messageBody
            ).create();

            logger.info("SMS sent successfully. SID: {}", message.getSid());
            return true;
        } catch (Exception e) {
            logger.error("Failed to send SMS to {}: {}", maskPhoneNumber(to), e.getMessage());
            return false;
        }
    }

    /**
     * Build SMS message based on purpose
     */
    private String buildSmsMessage(String otpCode, String purpose) {
        String purposeText = switch (purpose.toUpperCase()) {
            case "REGISTRATION" -> "complete your ODOP registration";
            case "LOGIN" -> "login to your ODOP account";
            case "PASSWORD_RESET" -> "reset your ODOP password";
            case "PHONE_VERIFICATION" -> "verify your phone number";
            default -> "complete verification";
        };
        
        return String.format("Your ODOP verification code is: %s. Use this to %s. " +
                "Valid for %d minutes. Do not share this code.", 
                otpCode, purposeText, otpExpiryMinutes);
    }

    /**
     * Normalize phone number to E.164 format
     */
    private String normalizePhoneNumber(String phoneNumber) {
        String cleaned = phoneNumber.replaceAll("[^0-9+]", "");
        if (!cleaned.startsWith("+")) {
            // Assume India if no country code
            if (cleaned.length() == 10) {
                cleaned = "+91" + cleaned;
            }
        }
        return cleaned;
    }

    /**
     * Mask phone number for privacy
     */
    private String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() < 4) {
            return "****";
        }
        int visibleDigits = 4;
        return "*".repeat(phoneNumber.length() - visibleDigits) + 
               phoneNumber.substring(phoneNumber.length() - visibleDigits);
    }

    /**
     * Build error response
     */
    private OtpResponse buildErrorResponse(String phoneNumber, String purpose, String message) {
        return OtpResponse.builder()
                .success(false)
                .verified(false)
                .message(message)
                .phoneNumber(maskPhoneNumber(phoneNumber))
                .purpose(purpose)
                .timestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
                .build();
    }

    /**
     * Cleanup expired OTPs - runs every hour
     */
    @Scheduled(fixedRate = 3600000) // 1 hour
    public void cleanupExpiredOtps() {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
        otpRepository.deleteByExpiresAtBefore(cutoff);
        logger.info("Cleaned up expired OTPs older than 24 hours");
    }
}
