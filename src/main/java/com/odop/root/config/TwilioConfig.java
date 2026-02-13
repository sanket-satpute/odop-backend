package com.odop.root.config;

import com.twilio.Twilio;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Twilio configuration for SMS services
 */
@Configuration
public class TwilioConfig {

    private static final Logger logger = LoggerFactory.getLogger(TwilioConfig.class);

    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @Value("${twilio.phone-number}")
    private String phoneNumber;

    @PostConstruct
    public void initTwilio() {
        if (accountSid != null && !accountSid.startsWith("YOUR_")) {
            Twilio.init(accountSid, authToken);
            logger.info("Twilio initialized successfully with phone number: {}", phoneNumber);
        } else {
            logger.warn("Twilio credentials not configured. SMS functionality will be disabled.");
        }
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public boolean isConfigured() {
        return accountSid != null && !accountSid.startsWith("YOUR_") 
                && authToken != null && !authToken.startsWith("YOUR_");
    }
}
