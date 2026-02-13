package com.odop.root.search.service;

import com.odop.root.models.ProductCategory;
import com.odop.root.models.Products;
import com.odop.root.models.Vendor;
import com.odop.root.repository.ProductCategoryRepository;
import com.odop.root.repository.ProductRepository;
import com.odop.root.repository.VendorRepository;
import com.odop.root.search.config.ElasticsearchConfig;
import com.odop.root.search.document.ProductDocument;
import com.odop.root.search.document.VendorDocument;
import com.odop.root.search.repository.ProductSearchRepository;
import com.odop.root.search.repository.VendorSearchRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service to synchronize MongoDB data with Elasticsearch index
 * Handles initial indexing and incremental updates
 * 
 * Note: This service is only active when search.enabled=true
 */
@Service
@ConditionalOnProperty(name = "search.enabled", havingValue = "true", matchIfMissing = false)
public class IndexSyncService {

    private static final Logger logger = LoggerFactory.getLogger(IndexSyncService.class);

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private VendorRepository vendorRepository;

    @Autowired
    private ProductCategoryRepository categoryRepository;

    @Autowired
    private ProductSearchRepository productSearchRepository;

    @Autowired
    private VendorSearchRepository vendorSearchRepository;

    @Autowired
    private ElasticsearchConfig elasticsearchConfig;

    /**
     * Initial sync on application startup
     */
    @PostConstruct
    public void initialSync() {
        if (!elasticsearchConfig.isSearchEnabled()) {
            logger.info("⚠️ Elasticsearch disabled. Skipping initial sync.");
            return;
        }

        try {
            logger.info("🔄 Starting initial Elasticsearch sync...");
            syncAllProducts();
            syncAllVendors();
            logger.info("✅ Initial Elasticsearch sync completed");
        } catch (Exception e) {
            logger.warn("⚠️ Initial sync failed (Elasticsearch may not be running): {}", e.getMessage());
            logger.warn("   Application will continue with MongoDB fallback for search.");
        }
    }

    /**
     * Sync all products to Elasticsearch
     */
    public int syncAllProducts() {
        List<Products> products = productRepository.findAll();
        List<ProductDocument> documents = new ArrayList<>();

        for (Products product : products) {
            documents.add(mapToProductDocument(product));
        }

        productSearchRepository.saveAll(documents);
        logger.info("📦 Synced {} products to Elasticsearch", documents.size());
        return documents.size();
    }

    /**
     * Sync all vendors to Elasticsearch
     */
    public int syncAllVendors() {
        List<Vendor> vendors = vendorRepository.findAll();
        List<VendorDocument> documents = new ArrayList<>();

        for (Vendor vendor : vendors) {
            documents.add(mapToVendorDocument(vendor));
        }

        vendorSearchRepository.saveAll(documents);
        logger.info("🏪 Synced {} vendors to Elasticsearch", documents.size());
        return documents.size();
    }

    /**
     * Index a single product (call after product create/update)
     */
    @Async
    public void indexProduct(Products product) {
        if (!elasticsearchConfig.isSearchEnabled()) return;

        try {
            ProductDocument document = mapToProductDocument(product);
            productSearchRepository.save(document);
            logger.debug("📦 Indexed product: {}", product.getProductId());
        } catch (Exception e) {
            logger.warn("Failed to index product {}: {}", product.getProductId(), e.getMessage());
        }
    }

    /**
     * Index a single vendor (call after vendor create/update)
     */
    @Async
    public void indexVendor(Vendor vendor) {
        if (!elasticsearchConfig.isSearchEnabled()) return;

        try {
            VendorDocument document = mapToVendorDocument(vendor);
            vendorSearchRepository.save(document);
            logger.debug("🏪 Indexed vendor: {}", vendor.getVendorId());
        } catch (Exception e) {
            logger.warn("Failed to index vendor {}: {}", vendor.getVendorId(), e.getMessage());
        }
    }

    /**
     * Delete product from index (call after product delete)
     */
    @Async
    public void deleteProductFromIndex(String productId) {
        if (!elasticsearchConfig.isSearchEnabled()) return;

        try {
            productSearchRepository.deleteById(productId);
            logger.debug("🗑️ Deleted product from index: {}", productId);
        } catch (Exception e) {
            logger.warn("Failed to delete product {} from index: {}", productId, e.getMessage());
        }
    }

    /**
     * Delete vendor from index (call after vendor delete)
     */
    @Async
    public void deleteVendorFromIndex(String vendorId) {
        if (!elasticsearchConfig.isSearchEnabled()) return;

        try {
            vendorSearchRepository.deleteById(vendorId);
            logger.debug("🗑️ Deleted vendor from index: {}", vendorId);
        } catch (Exception e) {
            logger.warn("Failed to delete vendor {} from index: {}", vendorId, e.getMessage());
        }
    }

    /**
     * Scheduled full resync every 6 hours (to catch any missed updates)
     */
    @Scheduled(fixedRate = 6 * 60 * 60 * 1000) // 6 hours
    public void scheduledResync() {
        if (!elasticsearchConfig.isSearchEnabled()) return;

        try {
            logger.info("🔄 Running scheduled Elasticsearch resync...");
            syncAllProducts();
            syncAllVendors();
            logger.info("✅ Scheduled resync completed");
        } catch (Exception e) {
            logger.warn("Scheduled resync failed: {}", e.getMessage());
        }
    }

    /**
     * Map MongoDB Product to Elasticsearch ProductDocument
     */
    private ProductDocument mapToProductDocument(Products product) {
        ProductDocument doc = new ProductDocument();
        
        doc.setId(product.getProductId());
        doc.setProductName(product.getProductName());
        doc.setProductDescription(product.getProductDescription());
        doc.setCategoryId(product.getCategoryId());
        doc.setSubCategoryId(product.getSubCategoryId());
        doc.setPrice(product.getPrice());
        doc.setDiscount(product.getDiscount());
        doc.setQuantity(product.getProductQuantity());
        doc.setStockStatus(product.getStockStatus());
        doc.setProductImageURL(product.getProductImageURL());
        doc.setVendorId(product.getVendorId());
        doc.setRating(product.getRating());
        doc.setTags(product.getTags());
        doc.setPromotionEnabled(product.isPromotionEnabled());
        doc.setSpecification(product.getSpecification());
        doc.setCreatedAt(product.getCreatedAt());
        doc.setUpdatedAt(product.getUpdatedAt());

        // ODOP fields
        doc.setOriginDistrict(product.getOriginDistrict());
        doc.setOriginState(product.getOriginState());
        doc.setOriginPinCode(product.getOriginPinCode());
        doc.setLocalName(product.getLocalName());
        doc.setOriginStory(product.getOriginStory());
        doc.setCraftType(product.getCraftType());
        doc.setMadeBy(product.getMadeBy());
        doc.setMaterialsUsed(product.getMaterialsUsed());

        // GI Tag
        doc.setGiTagCertified(product.getGiTagCertified());
        doc.setGiTagNumber(product.getGiTagNumber());

        // Popularity
        doc.setPopularityScore(product.getPopularityScore());
        doc.setTotalSold(product.getTotalSold());

        // Calculate discounted price
        if (product.getDiscount() > 0) {
            double discountedPrice = product.getPrice() * (1 - product.getDiscount() / 100.0);
            doc.setDiscountedPrice(discountedPrice);
        } else {
            doc.setDiscountedPrice(product.getPrice());
        }

        // Get category name
        if (product.getCategoryId() != null) {
            Optional<ProductCategory> category = categoryRepository.findById(product.getCategoryId());
            category.ifPresent(c -> doc.setCategoryName(c.getCategoryName()));
        }

        // Get vendor info
        if (product.getVendorId() != null) {
            Vendor vendor = vendorRepository.findByVendorId(product.getVendorId());
            if (vendor != null) {
                doc.setVendorName(vendor.getShopkeeperName());
                doc.setShopName(vendor.getShoppeeName());
            }
        }

        // Build autocomplete suggestions
        List<String> suggestions = new ArrayList<>();
        if (product.getProductName() != null) suggestions.add(product.getProductName());
        if (product.getLocalName() != null) suggestions.add(product.getLocalName());
        if (product.getCraftType() != null) suggestions.add(product.getCraftType());
        if (product.getTags() != null) suggestions.addAll(product.getTags());

        doc.setSuggest(suggestions);

        return doc;
    }

    /**
     * Map MongoDB Vendor to Elasticsearch VendorDocument
     */
    private VendorDocument mapToVendorDocument(Vendor vendor) {
        VendorDocument doc = new VendorDocument();
        
        doc.setId(vendor.getVendorId());
        doc.setShoppeeName(vendor.getShoppeeName());
        doc.setShopkeeperName(vendor.getShopkeeperName());
        doc.setEmailAddress(vendor.getEmailAddress());
        doc.setBusinessDescription(vendor.getBusinessDescription());
        doc.setLocationDistrict(vendor.getLocationDistrict());
        doc.setLocationState(vendor.getLocationState());
        doc.setPinCode(vendor.getPinCode());
        doc.setCompleteAddress(vendor.getCompleteAddress());
        doc.setProductCategories(vendor.getProductCategories());
        doc.setProductCount(vendor.getProductCount());
        doc.setStatus(vendor.getStatus());
        doc.setVerified(vendor.getVerified());
        doc.setProfilePictureUrl(vendor.getProfilePictureUrl());

        // Build autocomplete suggestions
        List<String> suggestions = new ArrayList<>();
        if (vendor.getShoppeeName() != null) suggestions.add(vendor.getShoppeeName());
        if (vendor.getShopkeeperName() != null) suggestions.add(vendor.getShopkeeperName());
        if (vendor.getLocationDistrict() != null) suggestions.add(vendor.getLocationDistrict());

        doc.setSuggest(suggestions);

        return doc;
    }

    /**
     * Get sync status
     */
    public SyncStatus getSyncStatus() {
        SyncStatus status = new SyncStatus();
        status.setElasticsearchEnabled(elasticsearchConfig.isSearchEnabled());

        try {
            status.setProductsInMongo(productRepository.count());
            status.setVendorsInMongo(vendorRepository.count());

            if (elasticsearchConfig.isSearchEnabled()) {
                status.setProductsInElasticsearch(productSearchRepository.count());
                status.setVendorsInElasticsearch(vendorSearchRepository.count());
                status.setElasticsearchConnected(true);
            }
        } catch (Exception e) {
            status.setElasticsearchConnected(false);
            status.setError(e.getMessage());
        }

        return status;
    }

    /**
     * Sync status DTO
     */
    public static class SyncStatus {
        private boolean elasticsearchEnabled;
        private boolean elasticsearchConnected;
        private long productsInMongo;
        private long vendorsInMongo;
        private long productsInElasticsearch;
        private long vendorsInElasticsearch;
        private String error;

        // Getters and Setters
        public boolean isElasticsearchEnabled() { return elasticsearchEnabled; }
        public void setElasticsearchEnabled(boolean elasticsearchEnabled) { this.elasticsearchEnabled = elasticsearchEnabled; }

        public boolean isElasticsearchConnected() { return elasticsearchConnected; }
        public void setElasticsearchConnected(boolean elasticsearchConnected) { this.elasticsearchConnected = elasticsearchConnected; }

        public long getProductsInMongo() { return productsInMongo; }
        public void setProductsInMongo(long productsInMongo) { this.productsInMongo = productsInMongo; }

        public long getVendorsInMongo() { return vendorsInMongo; }
        public void setVendorsInMongo(long vendorsInMongo) { this.vendorsInMongo = vendorsInMongo; }

        public long getProductsInElasticsearch() { return productsInElasticsearch; }
        public void setProductsInElasticsearch(long productsInElasticsearch) { this.productsInElasticsearch = productsInElasticsearch; }

        public long getVendorsInElasticsearch() { return vendorsInElasticsearch; }
        public void setVendorsInElasticsearch(long vendorsInElasticsearch) { this.vendorsInElasticsearch = vendorsInElasticsearch; }

        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
    }
}
