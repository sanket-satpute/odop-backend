package com.odop.root.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for verifying OTP
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerifyOtpRequest {

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[1-9]\\d{9,14}$", message = "Invalid phone number format")
    private String phoneNumber;

    @NotBlank(message = "OTP code is required")
    @Size(min = 4, max = 8, message = "OTP must be between 4 and 8 digits")
    private String otpCode;

    @NotBlank(message = "Purpose is required")
    private String purpose; // REGISTRATION, LOGIN, PASSWORD_RESET, PHONE_VERIFICATION
}
