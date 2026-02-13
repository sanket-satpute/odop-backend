package com.odop.root.controller;

import com.odop.root.dto.OtpResponse;
import com.odop.root.dto.SendOtpRequest;
import com.odop.root.dto.VerifyOtpRequest;
import com.odop.root.services.OtpService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for OTP operations
 * Base URL: /odop/otp
 */
@RestController
@RequestMapping("/odop/otp")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:63699"})
public class OtpController {

    private static final Logger logger = LoggerFactory.getLogger(OtpController.class);

    @Autowired
    private OtpService otpService;

    /**
     * Health check endpoint
     * GET /odop/otp/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "OTP Service");
        response.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.ok(response);
    }

    /**
     * Send OTP to phone number
     * POST /odop/otp/send
     */
    @PostMapping("/send")
    public ResponseEntity<OtpResponse> sendOtp(
            @Valid @RequestBody SendOtpRequest request,
            HttpServletRequest httpRequest) {
        
        logger.info("Send OTP request for phone: {}, purpose: {}", 
                maskPhone(request.getPhoneNumber()), request.getPurpose());
        
        String ipAddress = getClientIp(httpRequest);
        OtpResponse response = otpService.sendOtp(request, ipAddress);
        
        return response.isSuccess() 
                ? ResponseEntity.ok(response) 
                : ResponseEntity.badRequest().body(response);
    }

    /**
     * Verify OTP
     * POST /odop/otp/verify
     */
    @PostMapping("/verify")
    public ResponseEntity<OtpResponse> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        logger.info("Verify OTP request for phone: {}, purpose: {}", 
                maskPhone(request.getPhoneNumber()), request.getPurpose());
        
        OtpResponse response = otpService.verifyOtp(request);
        
        return response.isSuccess() 
                ? ResponseEntity.ok(response) 
                : ResponseEntity.badRequest().body(response);
    }

    /**
     * Resend OTP
     * POST /odop/otp/resend
     */
    @PostMapping("/resend")
    public ResponseEntity<OtpResponse> resendOtp(
            @Valid @RequestBody SendOtpRequest request,
            HttpServletRequest httpRequest) {
        
        logger.info("Resend OTP request for phone: {}, purpose: {}", 
                maskPhone(request.getPhoneNumber()), request.getPurpose());
        
        String ipAddress = getClientIp(httpRequest);
        OtpResponse response = otpService.resendOtp(request, ipAddress);
        
        return response.isSuccess() 
                ? ResponseEntity.ok(response) 
                : ResponseEntity.badRequest().body(response);
    }

    /**
     * Check if phone is verified
     * GET /odop/otp/check-verified?phone=...&purpose=...
     */
    @GetMapping("/check-verified")
    public ResponseEntity<Map<String, Object>> checkVerified(
            @RequestParam String phone,
            @RequestParam String purpose) {
        
        boolean verified = otpService.isPhoneVerified(phone, purpose);
        
        Map<String, Object> response = new HashMap<>();
        response.put("phone", maskPhone(phone));
        response.put("purpose", purpose);
        response.put("verified", verified);
        response.put("timestamp", LocalDateTime.now().toString());
        
        return ResponseEntity.ok(response);
    }

    // ========== Helper Methods ==========

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 4) {
            return "****";
        }
        return "*".repeat(phone.length() - 4) + phone.substring(phone.length() - 4);
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
