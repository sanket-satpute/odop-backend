package com.odop.root.oauth2.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response after social login
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SocialLoginResponse {
    
    private String token;           // JWT token
    private Object user;            // User DTO (CustomerDto, VendorDto)
    private String userType;        // CUSTOMER, VENDOR
    private boolean newUser;        // Whether this is a new registration
    private boolean requiresPhone;  // Whether phone number is required
    private String message;
}
