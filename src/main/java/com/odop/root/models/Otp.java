package com.odop.root.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;

/**
 * OTP entity for SMS verification
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "otps")
public class Otp {

    @Id
    private String id;

    @Indexed
    private String phoneNumber;

    private String otpCode;

    private String purpose; // REGISTRATION, LOGIN, PASSWORD_RESET, PHONE_VERIFICATION

    private LocalDateTime createdAt;

    private LocalDateTime expiresAt;

    private boolean verified;

    private LocalDateTime verifiedAt;

    private int attempts; // Number of verification attempts

    private String userId; // Optional - linked user ID

    private String userType; // CUSTOMER, VENDOR, ADMIN

    private boolean expired;

    private String ipAddress; // For security tracking

    /**
     * Check if OTP is expired
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt) || expired;
    }

    /**
     * Check if max attempts exceeded
     */
    public boolean isMaxAttemptsExceeded(int maxAttempts) {
        return attempts >= maxAttempts;
    }

    /**
     * Increment attempts
     */
    public void incrementAttempts() {
        this.attempts++;
    }

    /**
     * Mark as verified
     */
    public void markVerified() {
        this.verified = true;
        this.verifiedAt = LocalDateTime.now();
    }

    /**
     * Mark as expired
     */
    public void markExpired() {
        this.expired = true;
    }
}
