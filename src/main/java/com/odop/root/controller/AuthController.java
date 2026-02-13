package com.odop.root.controller;

import com.odop.root.dto.AuthRequest;
import com.odop.root.dto.AuthResponse;
import com.odop.root.dto.AdminDto;
import com.odop.root.dto.CustomerDto;
import com.odop.root.dto.VendorDto;
import com.odop.root.models.Admin;
import com.odop.root.models.Customer;
import com.odop.root.models.Vendor;
import com.odop.root.repository.AdminRepository;
import com.odop.root.repository.CustomerRepository;
import com.odop.root.repository.VendorRepository;
import com.odop.root.util.JwtUtil;
import com.odop.service.CustomerPreferencesService;
import jakarta.validation.Valid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

@RestController
public class AuthController {

    private static final Logger logger = LogManager.getLogger(AuthController.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private VendorRepository vendorRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CustomerPreferencesService customerPreferencesService;

    @PostMapping("/authenticate")
    public AuthResponse createAuthenticationToken(@Valid @RequestBody AuthRequest authRequest, HttpServletRequest request) throws Exception {
        try {
            String rawUsername = authRequest.getUsername();
            String username = rawUsername == null ? null : rawUsername.trim().toLowerCase();
            String requestedRole = authRequest.getRole();
            logger.info("Authentication request received for username: [PROTECTED], role: {}", requestedRole);

        // Find the user entity and convert to DTO
        Admin admin = null;
        Customer customer = null;
        Vendor vendor = null;
        Object userDto = null;
        @SuppressWarnings("unused")
        String userType = null;
        boolean isActive = true;

        // If role is specified, only look for that specific user type
        if (requestedRole != null && !requestedRole.isEmpty()) {
            switch (requestedRole.toLowerCase()) {
                case "admin":
                    admin = adminRepository.findByEmailAddress(username);
                    if (admin != null) {
                        userDto = toAdminDto(admin);
                        userType = "ADMIN";
                        isActive = admin.isActive();
                    }
                    break;
                case "customer":
                    customer = customerRepository.findByEmailAddress(username);
                    if (customer != null) {
                        userDto = toCustomerDto(customer);
                        userType = "CUSTOMER";
                        isActive = customer.getStatus() == null || customer.getStatus().equalsIgnoreCase("active");
                    }
                    break;
                case "vendor":
                    vendor = vendorRepository.findByEmailAddress(username);
                    if (vendor != null) {
                        userDto = toVendorDto(vendor);
                        userType = "VENDOR";
                        String vendorStatus = vendor.getStatus();
                        isActive = vendorStatus == null || 
                                   (!vendorStatus.equalsIgnoreCase("rejected") && 
                                    !vendorStatus.equalsIgnoreCase("banned"));
                    }
                    break;
                default:
                    logger.warn("Invalid role specified: {}", requestedRole);
            }
        } else {
            // Original behavior: search in order admin -> customer -> vendor
            admin = adminRepository.findByEmailAddress(username);
            if (admin != null) {
                userDto = toAdminDto(admin);
                userType = "ADMIN";
                isActive = admin.isActive();
            } else {
                customer = customerRepository.findByEmailAddress(username);
                if (customer != null) {
                    userDto = toCustomerDto(customer);
                    userType = "CUSTOMER";
                    isActive = customer.getStatus() == null || customer.getStatus().equalsIgnoreCase("active");
                } else {
                    vendor = vendorRepository.findByEmailAddress(username);
                    if (vendor != null) {
                        userDto = toVendorDto(vendor);
                        userType = "VENDOR";
                        String vendorStatus = vendor.getStatus();
                        isActive = vendorStatus == null || 
                                   (!vendorStatus.equalsIgnoreCase("rejected") && 
                                    !vendorStatus.equalsIgnoreCase("banned"));
                    }
                }
            }
        }

        if (userDto == null) {
            logger.warn("No user found for username: {}", username);
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED, "Incorrect username or password");
        }
        if (!isActive) {
            logger.warn("Inactive or banned user attempted login: {}", username);
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "Account is inactive or banned");
        }

        // Verify password directly for the specific user found (bypasses UserDetailsService sequential search)
        String storedPassword = null;
        Collection<String> roles = null;
        if (admin != null) {
            storedPassword = admin.getPassword();
            roles = admin.getRoles();
        } else if (customer != null) {
            storedPassword = customer.getPassword();
            roles = customer.getRoles();
        } else if (vendor != null) {
            storedPassword = vendor.getPassword();
            roles = vendor.getRoles();
        }

        // Verify password matches
        boolean passwordMatches = false;
        try {
            if (storedPassword != null && authRequest.getPassword() != null) {
                passwordMatches = passwordEncoder.matches(authRequest.getPassword(), storedPassword);
            }
        } catch (Exception pe) {
            logger.error("Password matching error: {}", pe.getMessage());
        }
        
        if (storedPassword == null || !passwordMatches) {
            logger.warn("Password verification failed for username: [PROTECTED]");
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED, "Incorrect username or password");
        }
        logger.debug("Password verification successful for: [PROTECTED]");

        // Build UserDetails from the found user (not using UserDetailsService to avoid wrong user lookup)
        final UserDetails userDetails = new User(
            username, 
            storedPassword, 
            roles != null ? roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList()) : Collections.emptyList()
        );
        String jwt = jwtUtil.generateToken(userDetails);
        logger.info("JWT token generated successfully for: [PROTECTED]");

        // Register customer session for settings security insights
        if (customer != null && customer.getCustomerId() != null) {
            String sessionId = "SESS-" + System.currentTimeMillis();
            String userAgent = request.getHeader("User-Agent");
            String ipAddress = request.getRemoteAddr();
            customerPreferencesService.registerSession(customer.getCustomerId(), sessionId, userAgent, ipAddress);
        }

        return new AuthResponse(jwt, userDto);
        } catch (Exception e) {
            logger.error("Authentication error: {}", e.getMessage());
            throw e;
        }
    }

    private AdminDto toAdminDto(Admin admin) {
        if (admin == null) return null;
        AdminDto dto = new AdminDto();
        dto.setAdminId(admin.getAdminId());
        dto.setFullName(admin.getFullName());
        dto.setEmailAddress(admin.getEmailAddress());
        dto.setContactNumber(admin.getContactNumber());
        dto.setPositionAndRole(admin.getPositionAndRole());
        dto.setActive(admin.isActive());
        return dto;
    }

    private CustomerDto toCustomerDto(Customer customer) {
        if (customer == null) return null;
        CustomerDto dto = new CustomerDto();
        dto.setCustomerId(customer.getCustomerId());
        dto.setFullName(customer.getFullName());
        dto.setEmailAddress(customer.getEmailAddress());
        dto.setContactNumber(customer.getContactNumber());
        dto.setAddress(customer.getAddress());
        dto.setCity(customer.getCity());
        dto.setState(customer.getState());
        dto.setPinCode(customer.getPinCode());
        dto.setStatus(customer.getStatus());
        dto.setProfilePictureUrl(customer.getProfilePictureUrl());
        return dto;
    }

    private VendorDto toVendorDto(Vendor vendor) {
        if (vendor == null) return null;
        VendorDto dto = new VendorDto();
        dto.setVendorId(vendor.getVendorId());
        dto.setFullName(vendor.getShopkeeperName());
        dto.setBusinessName(vendor.getShoppeeName());
        dto.setShoppeeName(vendor.getShoppeeName());
        dto.setShopkeeperName(vendor.getShopkeeperName());
        dto.setEmailAddress(vendor.getEmailAddress());
        dto.setContactNumber(vendor.getContactNumber());
        dto.setAddress(vendor.getCompleteAddress());
        dto.setShoppeeAddress(vendor.getShoppeeAddress());
        dto.setCity(vendor.getLocationDistrict());
        dto.setState(vendor.getLocationState());
        dto.setLocationDistrict(vendor.getLocationDistrict());
        dto.setLocationState(vendor.getLocationState());
        dto.setDistrict(vendor.getLocationDistrict());
        dto.setPinCode(vendor.getPinCode());
        dto.setStatus(vendor.getStatus());
        dto.setDeliveryAvailable(vendor.getDeliveryAvailable());
        dto.setDeliveryCharge(vendor.getDeliveryCharges());
        dto.setProfilePictureUrl(vendor.getProfilePictureUrl());
        dto.setVerified(vendor.getVerified());
        dto.setBusinessDescription(vendor.getBusinessDescription());
        return dto;
    }
}
