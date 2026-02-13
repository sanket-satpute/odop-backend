package com.odop.root.oauth2.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * Stores social account connections for users
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "social_accounts")
@CompoundIndex(name = "provider_social_idx", def = "{'provider': 1, 'socialId': 1}", unique = true)
public class SocialAccount {
    
    @Id
    private String id;
    
    // Provider info
    @Indexed
    private String provider;            // GOOGLE, FACEBOOK
    
    @Indexed
    private String socialId;            // Provider's user ID
    
    private String email;               // Email from provider
    
    // Linked user info
    @Indexed
    private String userId;              // Customer/Vendor ID
    
    private String userType;            // CUSTOMER, VENDOR
    
    // Profile from provider
    private String name;
    private String firstName;
    private String lastName;
    private String pictureUrl;
    private String locale;
    
    // Tokens (encrypted in production)
    private String accessToken;
    private String refreshToken;
    private LocalDateTime tokenExpiresAt;
    
    // Metadata
    private LocalDateTime linkedAt;
    private LocalDateTime lastLoginAt;
    private String lastLoginIp;
    private boolean active;
}
