package com.odop.root.oauth2.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.odop.root.oauth2.dto.SocialUserInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Map;

/**
 * Service to verify tokens and fetch user info from social providers
 */
@Service
@Slf4j
public class SocialTokenVerifierService {
    
    @Value("${oauth2.google.client-id:}")
    private String googleClientId;
    
    @Value("${oauth2.facebook.app-id:}")
    private String facebookAppId;
    
    @Value("${oauth2.facebook.app-secret:}")
    private String facebookAppSecret;
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    /**
     * Verify Google ID token and extract user info
     */
    public SocialUserInfo verifyGoogleToken(String idToken) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(), GsonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();
            
            GoogleIdToken googleIdToken = verifier.verify(idToken);
            
            if (googleIdToken == null) {
                log.warn("Invalid Google ID token");
                return null;
            }
            
            GoogleIdToken.Payload payload = googleIdToken.getPayload();
            
            return SocialUserInfo.builder()
                    .id(payload.getSubject())
                    .email(payload.getEmail())
                    .emailVerified(payload.getEmailVerified())
                    .name((String) payload.get("name"))
                    .firstName((String) payload.get("given_name"))
                    .lastName((String) payload.get("family_name"))
                    .pictureUrl((String) payload.get("picture"))
                    .locale((String) payload.get("locale"))
                    .provider("GOOGLE")
                    .build();
                    
        } catch (Exception e) {
            log.error("Error verifying Google token: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Verify Facebook access token and fetch user info
     */
    @SuppressWarnings("unchecked")
    public SocialUserInfo verifyFacebookToken(String accessToken) {
        try {
            // First, verify the token with Facebook
            String debugTokenUrl = String.format(
                    "https://graph.facebook.com/debug_token?input_token=%s&access_token=%s|%s",
                    accessToken, facebookAppId, facebookAppSecret);
            
            ResponseEntity<Map> debugResponse = restTemplate.getForEntity(debugTokenUrl, Map.class);
            
            if (debugResponse.getBody() == null) {
                log.warn("Invalid Facebook token - no response");
                return null;
            }
            
            Map<String, Object> data = (Map<String, Object>) debugResponse.getBody().get("data");
            
            if (data == null || !(Boolean) data.getOrDefault("is_valid", false)) {
                log.warn("Invalid Facebook token");
                return null;
            }
            
            // Fetch user info
            String userInfoUrl = String.format(
                    "https://graph.facebook.com/me?fields=id,name,first_name,last_name,email,picture.type(large)&access_token=%s",
                    accessToken);
            
            ResponseEntity<Map> userResponse = restTemplate.getForEntity(userInfoUrl, Map.class);
            
            if (userResponse.getBody() == null) {
                log.warn("Could not fetch Facebook user info");
                return null;
            }
            
            Map<String, Object> user = userResponse.getBody();
            
            // Extract picture URL
            String pictureUrl = null;
            Map<String, Object> picture = (Map<String, Object>) user.get("picture");
            if (picture != null) {
                Map<String, Object> pictureData = (Map<String, Object>) picture.get("data");
                if (pictureData != null) {
                    pictureUrl = (String) pictureData.get("url");
                }
            }
            
            return SocialUserInfo.builder()
                    .id((String) user.get("id"))
                    .email((String) user.get("email"))
                    .emailVerified(user.get("email") != null)  // Facebook only returns verified emails
                    .name((String) user.get("name"))
                    .firstName((String) user.get("first_name"))
                    .lastName((String) user.get("last_name"))
                    .pictureUrl(pictureUrl)
                    .provider("FACEBOOK")
                    .accessToken(accessToken)
                    .build();
                    
        } catch (Exception e) {
            log.error("Error verifying Facebook token: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Verify token based on provider
     */
    public SocialUserInfo verifyToken(String provider, String idToken, String accessToken) {
        return switch (provider.toUpperCase()) {
            case "GOOGLE" -> verifyGoogleToken(idToken);
            case "FACEBOOK" -> verifyFacebookToken(accessToken != null ? accessToken : idToken);
            default -> {
                log.warn("Unknown provider: {}", provider);
                yield null;
            }
        };
    }
}
