package com.odop.root.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for sending OTP
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SendOtpRequest {

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[1-9]\\d{9,14}$", message = "Invalid phone number format. Use E.164 format (e.g., +919876543210)")
    private String phoneNumber;

    @NotBlank(message = "Purpose is required")
    private String purpose; // REGISTRATION, LOGIN, PASSWORD_RESET, PHONE_VERIFICATION

    private String userId; // Optional - for linking OTP to user

    private String userType; // CUSTOMER, VENDOR, ADMIN
}
