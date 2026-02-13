package com.odop.root.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for OTP operations
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtpResponse {

    private boolean success;
    private String message;
    private String phoneNumber;
    private String purpose;
    private int expiryMinutes;
    private int remainingAttempts;
    private long resendAvailableInSeconds;
    private String timestamp;

    // For successful verification
    private boolean verified;
    private String verificationToken; // Optional token after successful verification
}
