package com.odop.root.oauth2.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Social user information extracted from OAuth2 providers
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SocialUserInfo {
    
    private String id;              // Provider's user ID
    private String email;
    private String name;
    private String firstName;
    private String lastName;
    private String pictureUrl;
    private String provider;        // GOOGLE, FACEBOOK
    private boolean emailVerified;
    private String locale;
    
    // Additional fields from providers
    private String accessToken;
    private String refreshToken;
    private Long expiresAt;
}
