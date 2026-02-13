package com.odop.root.search.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.TextIndexDefinition;
import org.springframework.stereotype.Component;

/**
 * Configuration class to create MongoDB text indexes for search functionality.
 * Text indexes enable full-text search capabilities.
 */
@Component
public class MongoTextIndexConfig {

    private static final Logger logger = LoggerFactory.getLogger(MongoTextIndexConfig.class);

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * Create text indexes when application starts.
     * This ensures indexes exist for search functionality.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void createTextIndexes() {
        logger.info("Creating MongoDB text indexes for search...");
        
        try {
            createProductTextIndex();
            createVendorTextIndex();
            createCategoryTextIndex();
            createProductFilterIndexes();
            logger.info("MongoDB text indexes created successfully!");
        } catch (Exception e) {
            logger.error("Error creating text indexes: {}", e.getMessage());
            // Don't fail application startup - indexes might already exist
        }
    }

    /**
     * Create text index on products collection.
     * Weights determine search relevance for each field.
     */
    private void createProductTextIndex() {
        try {
            // Drop existing text index if any (only one text index per collection allowed)
            try {
                mongoTemplate.indexOps("products").dropIndex("product_text_index");
            } catch (Exception ignored) {
                // Index might not exist
            }

            TextIndexDefinition textIndex = TextIndexDefinition.builder()
                .onField("productName", 10F)           // Highest weight - exact product name
                .onField("localName", 8F)              // High weight - local language name
                .onField("productDescription", 5F)     // Medium weight - description
                .onField("originDistrict", 7F)         // High weight - ODOP district
                .onField("originState", 6F)            // Good weight - state
                .onField("tags", 4F)                   // Medium weight - tags
                .onField("craftType", 6F)              // Good weight - craft type
                .onField("madeBy", 3F)                 // Lower weight - artisan name
                .onField("materialsUsed", 3F)          // Lower weight - materials
                .named("product_text_index")
                .build();

            mongoTemplate.indexOps("products").ensureIndex(textIndex);
            logger.info("Product text index created");
        } catch (Exception e) {
            logger.warn("Could not create product text index: {}", e.getMessage());
        }
    }

    /**
     * Create text index on vendors collection.
     */
    private void createVendorTextIndex() {
        try {
            try {
                mongoTemplate.indexOps("vendors").dropIndex("vendor_text_index");
            } catch (Exception ignored) {}

            TextIndexDefinition textIndex = TextIndexDefinition.builder()
                .onField("shoppeeName", 10F)           // Highest - shop name
                .onField("shopkeeperName", 8F)         // High - owner name
                .onField("businessDescription", 5F)    // Medium - description
                .onField("locationDistrict", 7F)       // High - district
                .onField("locationState", 6F)          // Good - state
                .onField("tags", 4F)                   // Medium - tags
                .onField("specializations", 6F)        // Good - specializations
                .named("vendor_text_index")
                .build();

            mongoTemplate.indexOps("vendors").ensureIndex(textIndex);
            logger.info("Vendor text index created");
        } catch (Exception e) {
            logger.warn("Could not create vendor text index: {}", e.getMessage());
        }
    }

    /**
     * Create text index on product categories collection.
     */
    private void createCategoryTextIndex() {
        try {
            try {
                mongoTemplate.indexOps("product_categories").dropIndex("category_text_index");
            } catch (Exception ignored) {}

            TextIndexDefinition textIndex = TextIndexDefinition.builder()
                .onField("categoryName", 10F)
                .onField("categoryDescription", 5F)
                .named("category_text_index")
                .build();

            mongoTemplate.indexOps("product_categories").ensureIndex(textIndex);
            logger.info("Category text index created");
        } catch (Exception e) {
            logger.warn("Could not create category text index: {}", e.getMessage());
        }
    }

    /**
     * Create additional indexes for efficient filtering.
     */
    private void createProductFilterIndexes() {
        try {
            // Compound index for location-based search
            mongoTemplate.indexOps("products").ensureIndex(
                new Index()
                    .on("originState", Sort.Direction.ASC)
                    .on("originDistrict", Sort.Direction.ASC)
                    .named("product_location_index")
            );

            // Index for price range queries
            mongoTemplate.indexOps("products").ensureIndex(
                new Index()
                    .on("price", Sort.Direction.ASC)
                    .named("product_price_index")
            );

            // Index for category filter
            mongoTemplate.indexOps("products").ensureIndex(
                new Index()
                    .on("categoryId", Sort.Direction.ASC)
                    .named("product_category_index")
            );

            // Index for GI tagged products
            mongoTemplate.indexOps("products").ensureIndex(
                new Index()
                    .on("giTagCertified", Sort.Direction.ASC)
                    .named("product_gi_index")
            );

            // Index for vendor's products
            mongoTemplate.indexOps("products").ensureIndex(
                new Index()
                    .on("vendorId", Sort.Direction.ASC)
                    .named("product_vendor_index")
            );

            logger.info("Product filter indexes created");
        } catch (Exception e) {
            logger.warn("Could not create filter indexes: {}", e.getMessage());
        }
    }
}
