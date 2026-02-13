package com.odop.root.oauth2.controller;

import com.odop.root.oauth2.dto.SocialLoginRequest;
import com.odop.root.oauth2.dto.SocialLoginResponse;
import com.odop.root.oauth2.model.SocialAccount;
import com.odop.root.oauth2.service.SocialAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller for social authentication
 */
@RestController
@RequestMapping("/odop/auth/social")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class SocialAuthController {
    
    private final SocialAuthService socialAuthService;
    
    /**
     * Login or register with social provider
     */
    @PostMapping("/login")
    public ResponseEntity<SocialLoginResponse> socialLogin(@Valid @RequestBody SocialLoginRequest request) {
        log.info("Social login request for provider: {}", request.getProvider());
        
        SocialLoginResponse response = socialAuthService.processSocialLogin(request);
        
        if (response.getToken() == null) {
            return ResponseEntity.badRequest().body(response);
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get linked social accounts for current user
     */
    @GetMapping("/linked/{userId}")
    public ResponseEntity<List<Map<String, Object>>> getLinkedAccounts(@PathVariable String userId) {
        List<SocialAccount> accounts = socialAuthService.getLinkedAccounts(userId);
        
        // Return only necessary info (not tokens)
        List<Map<String, Object>> safeAccounts = accounts.stream()
                .map(account -> Map.<String, Object>of(
                        "provider", account.getProvider(),
                        "email", account.getEmail(),
                        "name", account.getName() != null ? account.getName() : "",
                        "pictureUrl", account.getPictureUrl() != null ? account.getPictureUrl() : "",
                        "linkedAt", account.getLinkedAt().toString()
                ))
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(safeAccounts);
    }
    
    /**
     * Link additional social account to existing user
     */
    @PostMapping("/link/{userId}")
    public ResponseEntity<SocialLoginResponse> linkAccount(
            @PathVariable String userId,
            @Valid @RequestBody SocialLoginRequest request) {
        
        log.info("Linking social account for user: {} provider: {}", userId, request.getProvider());
        
        SocialLoginResponse response = socialAuthService.linkAdditionalAccount(userId, request);
        
        if (response.getMessage().contains("already") || response.getMessage().contains("Invalid")) {
            return ResponseEntity.badRequest().body(response);
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Unlink social account from user
     */
    @DeleteMapping("/unlink/{userId}/{provider}")
    public ResponseEntity<Map<String, Object>> unlinkAccount(
            @PathVariable String userId,
            @PathVariable String provider) {
        
        log.info("Unlinking social account for user: {} provider: {}", userId, provider);
        
        boolean success = socialAuthService.unlinkSocialAccount(userId, provider);
        
        if (!success) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Cannot unlink. This may be your only login method."
            ));
        }
        
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", provider + " account unlinked successfully"
        ));
    }
    
    /**
     * Health check
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "Social Auth Service",
                "providers", "GOOGLE, FACEBOOK"
        ));
    }
}
