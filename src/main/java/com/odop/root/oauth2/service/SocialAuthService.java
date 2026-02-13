package com.odop.root.oauth2.service;

import com.odop.root.dto.CustomerDto;
import com.odop.root.models.Customer;
import com.odop.root.oauth2.dto.SocialLoginRequest;
import com.odop.root.oauth2.dto.SocialLoginResponse;
import com.odop.root.oauth2.dto.SocialUserInfo;
import com.odop.root.oauth2.model.SocialAccount;
import com.odop.root.oauth2.repository.SocialAccountRepository;
import com.odop.root.repository.CustomerRepository;
import com.odop.root.services.UserDetailsServiceImpl;
import com.odop.root.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for handling social login/registration
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SocialAuthService {
    
    private final SocialTokenVerifierService tokenVerifierService;
    private final SocialAccountRepository socialAccountRepository;
    private final CustomerRepository customerRepository;
    private final UserDetailsServiceImpl userDetailsService;
    private final JwtUtil jwtUtil;
    
    /**
     * Process social login/registration
     */
    public SocialLoginResponse processSocialLogin(SocialLoginRequest request) {
        log.info("Processing social login for provider: {}", request.getProvider());
        
        // 1. Verify the token and get user info
        SocialUserInfo socialUser = tokenVerifierService.verifyToken(
                request.getProvider(),
                request.getIdToken(),
                request.getAccessToken()
        );
        
        if (socialUser == null) {
            log.warn("Failed to verify social token for provider: {}", request.getProvider());
            return SocialLoginResponse.builder()
                    .message("Invalid or expired token")
                    .build();
        }
        
        if (socialUser.getEmail() == null || socialUser.getEmail().isEmpty()) {
            log.warn("No email provided by social provider: {}", request.getProvider());
            return SocialLoginResponse.builder()
                    .message("Email is required. Please grant email permission.")
                    .build();
        }
        
        // 2. Check if social account already linked
        Optional<SocialAccount> existingSocialAccount = socialAccountRepository
                .findByProviderAndSocialId(socialUser.getProvider(), socialUser.getId());
        
        if (existingSocialAccount.isPresent()) {
            // Existing social login - return user
            return loginExistingSocialUser(existingSocialAccount.get(), socialUser);
        }
        
        // 3. Check if email already exists in our system
        Customer existingCustomer = customerRepository.findByEmailAddress(socialUser.getEmail().toLowerCase());
        
        if (existingCustomer != null) {
            // Link social account to existing customer
            return linkSocialToExistingUser(existingCustomer, socialUser);
        }
        
        // 4. Create new customer account
        return createNewSocialUser(socialUser);
    }
    
    /**
     * Login existing social user
     */
    private SocialLoginResponse loginExistingSocialUser(SocialAccount socialAccount, SocialUserInfo socialUser) {
        log.info("Existing social user login: {}", socialAccount.getEmail());
        
        // Update last login time
        socialAccount.setLastLoginAt(LocalDateTime.now());
        socialAccount.setAccessToken(socialUser.getAccessToken());
        socialAccountRepository.save(socialAccount);
        
        // Get the customer
        Customer customer = customerRepository.findById(socialAccount.getUserId()).orElse(null);
        
        if (customer == null) {
            log.error("Customer not found for social account: {}", socialAccount.getUserId());
            return SocialLoginResponse.builder()
                    .message("User account not found")
                    .build();
        }
        
        // Check if account is active
        if (customer.getStatus() != null && !customer.getStatus().equalsIgnoreCase("active")) {
            return SocialLoginResponse.builder()
                    .message("Account is inactive or banned")
                    .build();
        }
        
        // Generate JWT
        UserDetails userDetails = userDetailsService.loadUserByUsername(customer.getEmailAddress());
        String token = jwtUtil.generateToken(userDetails);
        
        return SocialLoginResponse.builder()
                .token(token)
                .user(toCustomerDto(customer))
                .userType("CUSTOMER")
                .newUser(false)
                .requiresPhone(customer.getContactNumber() == 0)
                .message("Login successful")
                .build();
    }
    
    /**
     * Link social account to existing user
     */
    private SocialLoginResponse linkSocialToExistingUser(Customer customer, SocialUserInfo socialUser) {
        log.info("Linking social account to existing user: {}", customer.getEmailAddress());
        
        // Create social account link
        SocialAccount socialAccount = SocialAccount.builder()
                .provider(socialUser.getProvider())
                .socialId(socialUser.getId())
                .email(socialUser.getEmail())
                .userId(customer.getCustomerId())
                .userType("CUSTOMER")
                .name(socialUser.getName())
                .firstName(socialUser.getFirstName())
                .lastName(socialUser.getLastName())
                .pictureUrl(socialUser.getPictureUrl())
                .locale(socialUser.getLocale())
                .accessToken(socialUser.getAccessToken())
                .linkedAt(LocalDateTime.now())
                .lastLoginAt(LocalDateTime.now())
                .active(true)
                .build();
        
        socialAccountRepository.save(socialAccount);
        
        // Update profile picture if not set
        if (customer.getProfilePictureUrl() == null && socialUser.getPictureUrl() != null) {
            customer.setProfilePictureUrl(socialUser.getPictureUrl());
            customer.setUpdatedAt(LocalDateTime.now());
            customerRepository.save(customer);
        }
        
        // Generate JWT
        UserDetails userDetails = userDetailsService.loadUserByUsername(customer.getEmailAddress());
        String token = jwtUtil.generateToken(userDetails);
        
        return SocialLoginResponse.builder()
                .token(token)
                .user(toCustomerDto(customer))
                .userType("CUSTOMER")
                .newUser(false)
                .requiresPhone(customer.getContactNumber() == 0)
                .message("Social account linked successfully")
                .build();
    }
    
    /**
     * Create new user from social login
     */
    private SocialLoginResponse createNewSocialUser(SocialUserInfo socialUser) {
        log.info("Creating new customer from social login: {}", socialUser.getEmail());
        
        // Create new customer
        Customer customer = new Customer();
        customer.setEmailAddress(socialUser.getEmail().toLowerCase());
        customer.setFullName(socialUser.getName());
        customer.setProfilePictureUrl(socialUser.getPictureUrl());
        customer.setPassword(UUID.randomUUID().toString()); // Random password (user can set later)
        customer.setStatus("active");
        customer.setRoles(List.of("ROLE_CUSTOMER"));
        customer.setCreatedAt(LocalDateTime.now());
        customer.setUpdatedAt(LocalDateTime.now());
        customer.setCartIds(new ArrayList<>());
        customer.setOrderIds(new ArrayList<>());
        customer.setWishlistProductIds(new ArrayList<>());
        
        customer = customerRepository.save(customer);
        
        // Create social account link
        SocialAccount socialAccount = SocialAccount.builder()
                .provider(socialUser.getProvider())
                .socialId(socialUser.getId())
                .email(socialUser.getEmail())
                .userId(customer.getCustomerId())
                .userType("CUSTOMER")
                .name(socialUser.getName())
                .firstName(socialUser.getFirstName())
                .lastName(socialUser.getLastName())
                .pictureUrl(socialUser.getPictureUrl())
                .locale(socialUser.getLocale())
                .accessToken(socialUser.getAccessToken())
                .linkedAt(LocalDateTime.now())
                .lastLoginAt(LocalDateTime.now())
                .active(true)
                .build();
        
        socialAccountRepository.save(socialAccount);
        
        // Generate JWT
        UserDetails userDetails = userDetailsService.loadUserByUsername(customer.getEmailAddress());
        String token = jwtUtil.generateToken(userDetails);
        
        return SocialLoginResponse.builder()
                .token(token)
                .user(toCustomerDto(customer))
                .userType("CUSTOMER")
                .newUser(true)
                .requiresPhone(true)  // New users need to add phone
                .message("Account created successfully")
                .build();
    }
    
    /**
     * Get linked social accounts for a user
     */
    public List<SocialAccount> getLinkedAccounts(String userId) {
        return socialAccountRepository.findByUserId(userId);
    }
    
    /**
     * Unlink social account
     */
    public boolean unlinkSocialAccount(String userId, String provider) {
        // Check if user has password or other social accounts
        Customer customer = customerRepository.findById(userId).orElse(null);
        if (customer == null) {
            return false;
        }
        
        long linkedCount = socialAccountRepository.countByUserId(userId);
        
        // Don't allow unlinking if it's the only login method
        // (In a real app, you'd check if user has set a password)
        if (linkedCount <= 1) {
            log.warn("Cannot unlink only social account for user: {}", userId);
            return false;
        }
        
        socialAccountRepository.deleteByUserIdAndProvider(userId, provider.toUpperCase());
        return true;
    }
    
    /**
     * Link additional social account to existing user
     */
    public SocialLoginResponse linkAdditionalAccount(String userId, SocialLoginRequest request) {
        // Verify token
        SocialUserInfo socialUser = tokenVerifierService.verifyToken(
                request.getProvider(),
                request.getIdToken(),
                request.getAccessToken()
        );
        
        if (socialUser == null) {
            return SocialLoginResponse.builder()
                    .message("Invalid token")
                    .build();
        }
        
        // Check if already linked to another account
        Optional<SocialAccount> existingLink = socialAccountRepository
                .findByProviderAndSocialId(socialUser.getProvider(), socialUser.getId());
        
        if (existingLink.isPresent() && !existingLink.get().getUserId().equals(userId)) {
            return SocialLoginResponse.builder()
                    .message("This social account is already linked to another user")
                    .build();
        }
        
        if (existingLink.isPresent()) {
            return SocialLoginResponse.builder()
                    .message("Account already linked")
                    .build();
        }
        
        // Get user
        Customer customer = customerRepository.findById(userId).orElse(null);
        if (customer == null) {
            return SocialLoginResponse.builder()
                    .message("User not found")
                    .build();
        }
        
        // Create link
        SocialAccount socialAccount = SocialAccount.builder()
                .provider(socialUser.getProvider())
                .socialId(socialUser.getId())
                .email(socialUser.getEmail())
                .userId(userId)
                .userType("CUSTOMER")
                .name(socialUser.getName())
                .firstName(socialUser.getFirstName())
                .lastName(socialUser.getLastName())
                .pictureUrl(socialUser.getPictureUrl())
                .locale(socialUser.getLocale())
                .accessToken(socialUser.getAccessToken())
                .linkedAt(LocalDateTime.now())
                .lastLoginAt(LocalDateTime.now())
                .active(true)
                .build();
        
        socialAccountRepository.save(socialAccount);
        
        return SocialLoginResponse.builder()
                .message("Account linked successfully")
                .build();
    }
    
    private CustomerDto toCustomerDto(Customer customer) {
        CustomerDto dto = new CustomerDto();
        dto.setCustomerId(customer.getCustomerId());
        dto.setFullName(customer.getFullName());
        dto.setEmailAddress(customer.getEmailAddress());
        dto.setContactNumber(customer.getContactNumber());
        dto.setAddress(customer.getAddress());
        dto.setCity(customer.getCity());
        dto.setState(customer.getState());
        dto.setPinCode(customer.getPinCode());
        dto.setProfilePictureUrl(customer.getProfilePictureUrl());
        dto.setStatus(customer.getStatus());
        return dto;
    }
}
