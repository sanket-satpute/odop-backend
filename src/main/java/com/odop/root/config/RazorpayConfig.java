package com.odop.root.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Configuration class for Razorpay Payment Gateway.
 * Initializes the Razorpay client with test/production keys.
 */
@Configuration
public class RazorpayConfig {

    private static final Logger logger = LogManager.getLogger(RazorpayConfig.class);

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    /**
     * Creates a singleton RazorpayClient bean.
     * This client is thread-safe and can be reused across the application.
     */
    @Bean
    public RazorpayClient razorpayClient() throws RazorpayException {
        logger.info("Initializing Razorpay client with Key ID: {}...", 
            razorpayKeyId.substring(0, Math.min(12, razorpayKeyId.length())));
        
        if (razorpayKeyId.contains("placeholder")) {
            logger.warn("⚠️ Using placeholder Razorpay keys! Payment operations will fail.");
            logger.warn("⚠️ Please update razorpay.key.id and razorpay.key.secret in application.yml");
        }
        
        return new RazorpayClient(razorpayKeyId, razorpayKeySecret);
    }

    /**
     * Returns the Razorpay Key ID (public key).
     * This is safe to expose to frontend for checkout initialization.
     */
    public String getRazorpayKeyId() {
        return razorpayKeyId;
    }
}
