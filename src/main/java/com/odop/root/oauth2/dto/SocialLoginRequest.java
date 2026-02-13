package com.odop.root.oauth2.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for social login
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SocialLoginRequest {
    
    @NotBlank(message = "Provider is required")
    private String provider;        // GOOGLE, FACEBOOK
    
    @NotBlank(message = "ID token is required")
    private String idToken;         // ID token from social provider
    
    private String accessToken;     // Access token (optional, for fetching additional info)
    
    private String deviceId;        // For tracking devices (optional)
}
