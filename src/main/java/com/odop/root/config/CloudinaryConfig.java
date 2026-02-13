package com.odop.root.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cloudinary Configuration for Cloud Image Upload
 * 
 * Cloudinary provides:
 * - Cloud-based image storage
 * - Automatic CDN delivery
 * - Image transformations (resize, crop, filters)
 * - Free tier: 25GB storage + 25GB bandwidth/month
 * 
 * Setup:
 * 1. Sign up at https://cloudinary.com
 * 2. Go to Dashboard -> Copy cloud name, API key, API secret
 * 3. Add to application.yml
 */
@Configuration
public class CloudinaryConfig {

    private static final Logger logger = LoggerFactory.getLogger(CloudinaryConfig.class);

    @Value("${cloudinary.cloud-name:}")
    private String cloudName;

    @Value("${cloudinary.api-key:}")
    private String apiKey;

    @Value("${cloudinary.api-secret:}")
    private String apiSecret;

    @PostConstruct
    public void init() {
        if (isConfigured()) {
            logger.info("✅ Cloudinary configured with cloud name: {}", cloudName);
        } else {
            logger.warn("⚠️ Cloudinary not configured. Image uploads will work in DEMO MODE (local storage)");
            logger.warn("   To enable cloud uploads, add Cloudinary credentials to application.yml:");
            logger.warn("   cloudinary:");
            logger.warn("     cloud-name: YOUR_CLOUD_NAME");
            logger.warn("     api-key: YOUR_API_KEY");
            logger.warn("     api-secret: YOUR_API_SECRET");
        }
    }

    @Bean
    public Cloudinary cloudinary() {
        if (!isConfigured()) {
            // Return a dummy Cloudinary instance for demo mode
            return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "demo",
                "api_key", "demo",
                "api_secret", "demo"
            ));
        }
        
        return new Cloudinary(ObjectUtils.asMap(
            "cloud_name", cloudName,
            "api_key", apiKey,
            "api_secret", apiSecret,
            "secure", true
        ));
    }

    public boolean isConfigured() {
        return cloudName != null && !cloudName.isEmpty() 
            && !cloudName.equals("YOUR_CLOUD_NAME")
            && apiKey != null && !apiKey.isEmpty() 
            && !apiKey.equals("YOUR_API_KEY")
            && apiSecret != null && !apiSecret.isEmpty()
            && !apiSecret.equals("YOUR_API_SECRET");
    }
}
