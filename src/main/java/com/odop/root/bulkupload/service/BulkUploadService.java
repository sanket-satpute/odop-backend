package com.odop.root.bulkupload.service;

import com.odop.root.bulkupload.dto.*;
import com.odop.root.bulkupload.model.BulkUploadJob;
import com.odop.root.bulkupload.model.BulkUploadJob.*;
import com.odop.root.bulkupload.repository.BulkUploadJobRepository;
import com.odop.root.models.Products;
import com.odop.root.repository.ProductRepository;
import com.odop.root.variant.model.ProductVariant;
import com.odop.root.variant.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Service for bulk upload operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BulkUploadService {
    
    private final BulkUploadJobRepository jobRepository;
    private final CsvParserService csvParserService;
    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    
    private static final int MAX_CONCURRENT_JOBS = 3;
    
    // ==================== JOB MANAGEMENT ====================
    
    /**
     * Create a new upload job
     */
    public BulkUploadJob createUploadJob(String vendorId, MultipartFile file, BulkUploadRequest request) {
        // Check concurrent job limit
        long runningJobs = jobRepository.countRunningJobsByVendor(vendorId);
        if (runningJobs >= MAX_CONCURRENT_JOBS) {
            throw new IllegalStateException("Maximum concurrent uploads reached. Please wait for existing uploads to complete.");
        }
        
        // Validate file
        CsvParserService.ValidationResult validation = csvParserService.validateFile(file);
        if (!validation.isValid()) {
            throw new IllegalArgumentException(String.join(", ", validation.getErrors()));
        }
        
        // Parse to get row count
        CsvParserService.ParseResult parseResult = csvParserService.parseFileAutoDetect(file, request.isHasHeader());
        if (!parseResult.isSuccess()) {
            throw new IllegalArgumentException(String.join(", ", parseResult.getErrors()));
        }
        
        // Create job
        BulkUploadJob job = BulkUploadJob.builder()
                .vendorId(vendorId)
                .originalFileName(file.getOriginalFilename())
                .storedFileName(UUID.randomUUID().toString() + ".csv")
                .fileSize(file.getSize())
                .uploadType(UploadType.valueOf(request.getUploadType()))
                .status(UploadStatus.PENDING)
                .totalRows(parseResult.getTotalRows())
                .processedRows(0)
                .successCount(0)
                .errorCount(0)
                .skippedCount(0)
                .progressPercent(0)
                .columnMapping(request.getColumnMapping())
                .updateExisting(request.isUpdateExisting())
                .skipInvalid(request.isSkipInvalid())
                .generateSkus(request.isGenerateSkus())
                .errors(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .build();
        
        return jobRepository.save(job);
    }
    
    /**
     * Process upload job asynchronously
     */
    @Async
    public CompletableFuture<BulkUploadJob> processUploadAsync(String jobId, MultipartFile file, BulkUploadRequest request) {
        BulkUploadJob job = jobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found: " + jobId));
        
        try {
            job.setStatus(UploadStatus.VALIDATING);
            job.setStartedAt(LocalDateTime.now());
            jobRepository.save(job);
            
            // Parse CSV
            CsvParserService.ParseResult parseResult = csvParserService.parseFileAutoDetect(file, request.isHasHeader());
            
            if (!parseResult.isSuccess()) {
                job.markFailed(String.join(", ", parseResult.getErrors()));
                jobRepository.save(job);
                return CompletableFuture.completedFuture(job);
            }
            
            job.setStatus(UploadStatus.PROCESSING);
            jobRepository.save(job);
            
            // Process based on upload type
            switch (job.getUploadType()) {
                case PRODUCTS:
                    processProducts(job, parseResult.getRecords(), request);
                    break;
                case VARIANTS:
                    processVariants(job, parseResult.getRecords(), request);
                    break;
                case PRICE_UPDATE:
                    processPriceUpdates(job, parseResult.getRecords(), request);
                    break;
                case STOCK_UPDATE:
                    processStockUpdates(job, parseResult.getRecords(), request);
                    break;
                default:
                    job.markFailed("Unsupported upload type: " + job.getUploadType());
            }
            
            job.markCompleted();
            
        } catch (Exception e) {
            log.error("Error processing upload job: {}", jobId, e);
            job.markFailed("Processing error: " + e.getMessage());
        }
        
        jobRepository.save(job);
        return CompletableFuture.completedFuture(job);
    }
    
    // ==================== PRODUCT PROCESSING ====================
    
    private void processProducts(BulkUploadJob job, List<Map<String, String>> records, BulkUploadRequest request) {
        Map<String, String> mapping = request.getColumnMapping();
        int processed = 0;
        int success = 0;
        int errors = 0;
        int skipped = 0;
        
        for (Map<String, String> row : records) {
            int rowNum = Integer.parseInt(row.getOrDefault("_rowNumber", "0"));
            
            try {
                String productName = getMappedValue(row, mapping, "product_name");
                if (productName == null || productName.isEmpty()) {
                    if (request.isSkipInvalid()) {
                        skipped++;
                        addRowError(job, rowNum, "VALIDATION", "Product name is required", row);
                    } else {
                        errors++;
                        addRowError(job, rowNum, "VALIDATION", "Product name is required", row);
                    }
                    processed++;
                    continue;
                }
                
                // Parse price
                double price = parseDouble(getMappedValue(row, mapping, "price"), 0);
                if (price <= 0) {
                    if (request.isSkipInvalid()) {
                        skipped++;
                        addRowError(job, rowNum, "VALIDATION", "Invalid price", row);
                    } else {
                        errors++;
                        addRowError(job, rowNum, "VALIDATION", "Invalid price", row);
                    }
                    processed++;
                    continue;
                }
                
                // Build product - using existing Products model
                Products product = new Products();
                product.setProductName(productName);
                product.setPrice(price);
                product.setProductDescription(getMappedValue(row, mapping, "description"));
                product.setProductQuantity((long) parseDouble(getMappedValue(row, mapping, "stock_quantity"), 0));
                product.setVendorId(job.getVendorId());
                product.setCreatedAt(LocalDateTime.now());
                
                // Stock status
                if (product.getProductQuantity() > 0) {
                    product.setStockStatus("In Stock");
                } else {
                    product.setStockStatus("Out of Stock");
                }
                
                // Category
                String categoryId = getMappedValue(row, mapping, "category_id");
                if (categoryId == null || categoryId.isEmpty()) {
                    categoryId = request.getDefaultCategoryId();
                }
                product.setCategoryId(categoryId);
                
                // SubCategory
                product.setSubCategoryId(getMappedValue(row, mapping, "sub_category_id"));
                
                // Discount
                product.setDiscount((int) parseDouble(getMappedValue(row, mapping, "discount"), 0));
                
                // Tags
                String tags = getMappedValue(row, mapping, "tags");
                if (tags != null && !tags.isEmpty()) {
                    product.setTags(Arrays.asList(tags.split(",")));
                }
                
                // Image URL
                String imageUrl = getMappedValue(row, mapping, "image_url");
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    product.setProductImageURL(imageUrl);
                }
                
                // Specification & Warranty
                product.setSpecification(getMappedValue(row, mapping, "specification"));
                product.setWarranty(getMappedValue(row, mapping, "warranty"));
                
                // ODOP specific fields
                product.setOriginDistrict(getMappedValue(row, mapping, "origin_district"));
                product.setOriginState(getMappedValue(row, mapping, "origin_state"));
                product.setOriginPinCode(getMappedValue(row, mapping, "origin_pincode"));
                product.setLocalName(getMappedValue(row, mapping, "local_name"));
                product.setCraftType(getMappedValue(row, mapping, "craft_type"));
                product.setMadeBy(getMappedValue(row, mapping, "made_by"));
                product.setMaterialsUsed(getMappedValue(row, mapping, "materials_used"));
                product.setOriginStory(getMappedValue(row, mapping, "origin_story"));
                
                // GI Tag
                product.setGiTagNumber(getMappedValue(row, mapping, "gi_tag_number"));
                product.setGiTagCertified(parseBoolean(getMappedValue(row, mapping, "gi_tag_certified"), false));
                product.setGiTagCertificateUrl(getMappedValue(row, mapping, "gi_tag_certificate_url"));
                
                // Promotion
                product.setPromotionEnabled(parseBoolean(getMappedValue(row, mapping, "promotion_enabled"), false));
                
                productRepository.save(product);
                success++;
                
            } catch (Exception e) {
                errors++;
                addRowError(job, rowNum, "SYSTEM", e.getMessage(), row);
            }
            
            processed++;
            
            // Update progress every 10 rows
            if (processed % 10 == 0) {
                job.updateProgress(processed, success, errors);
                job.setSkippedCount(skipped);
                jobRepository.save(job);
            }
        }
        
        job.updateProgress(processed, success, errors);
        job.setSkippedCount(skipped);
    }
    
    // ==================== VARIANT PROCESSING ====================
    
    private void processVariants(BulkUploadJob job, List<Map<String, String>> records, BulkUploadRequest request) {
        Map<String, String> mapping = request.getColumnMapping();
        int processed = 0;
        int success = 0;
        int errors = 0;
        int skipped = 0;
        
        for (Map<String, String> row : records) {
            int rowNum = Integer.parseInt(row.getOrDefault("_rowNumber", "0"));
            
            try {
                String productId = getMappedValue(row, mapping, "product_id");
                if (productId == null || productId.isEmpty()) {
                    skipped++;
                    addRowError(job, rowNum, "VALIDATION", "Product ID is required", row);
                    processed++;
                    continue;
                }
                
                // Check product exists
                if (!productRepository.existsById(productId)) {
                    skipped++;
                    addRowError(job, rowNum, "VALIDATION", "Product not found: " + productId, row);
                    processed++;
                    continue;
                }
                
                // Build attributes
                Map<String, String> attributes = new HashMap<>();
                String size = getMappedValue(row, mapping, "size");
                String color = getMappedValue(row, mapping, "color");
                String material = getMappedValue(row, mapping, "material");
                String weight = getMappedValue(row, mapping, "weight");
                
                if (size != null && !size.isEmpty()) attributes.put("size", size);
                if (color != null && !color.isEmpty()) attributes.put("color", color);
                if (material != null && !material.isEmpty()) attributes.put("material", material);
                if (weight != null && !weight.isEmpty()) attributes.put("weight", weight);
                
                // Build variant
                ProductVariant variant = ProductVariant.builder()
                        .productId(productId)
                        .attributes(attributes)
                        .price(parseDouble(getMappedValue(row, mapping, "price"), 0))
                        .mrp(parseDouble(getMappedValue(row, mapping, "mrp"), 0))
                        .stockQuantity((int) parseDouble(getMappedValue(row, mapping, "stock_quantity"), 0))
                        .active(parseBoolean(getMappedValue(row, mapping, "is_active"), true))
                        .isDefault(parseBoolean(getMappedValue(row, mapping, "is_default"), false))
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();
                
                // SKU
                String sku = getMappedValue(row, mapping, "sku");
                if ((sku == null || sku.isEmpty()) && request.isGenerateSkus()) {
                    sku = generateVariantSku(productId, attributes);
                }
                variant.setSku(sku);
                
                // Image
                String imageUrl = getMappedValue(row, mapping, "image_url");
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    variant.setImageUrls(List.of(imageUrl));
                    variant.setThumbnailUrl(imageUrl);
                }
                
                variantRepository.save(variant);
                success++;
                
            } catch (Exception e) {
                errors++;
                addRowError(job, rowNum, "SYSTEM", e.getMessage(), row);
            }
            
            processed++;
            
            if (processed % 10 == 0) {
                job.updateProgress(processed, success, errors);
                job.setSkippedCount(skipped);
                jobRepository.save(job);
            }
        }
        
        job.updateProgress(processed, success, errors);
        job.setSkippedCount(skipped);
    }
    
    // ==================== PRICE UPDATE PROCESSING ====================
    
    private void processPriceUpdates(BulkUploadJob job, List<Map<String, String>> records, BulkUploadRequest request) {
        Map<String, String> mapping = request.getColumnMapping();
        int processed = 0;
        int success = 0;
        int errors = 0;
        
        for (Map<String, String> row : records) {
            int rowNum = Integer.parseInt(row.getOrDefault("_rowNumber", "0"));
            
            try {
                String identifier = getMappedValue(row, mapping, "identifier");
                String identifierType = getMappedValue(row, mapping, "identifier_type");
                double newPrice = parseDouble(getMappedValue(row, mapping, "new_price"), 0);
                
                if (identifier == null || newPrice <= 0) {
                    errors++;
                    addRowError(job, rowNum, "VALIDATION", "Identifier and new_price are required", row);
                    processed++;
                    continue;
                }
                
                boolean updated = false;
                
                // Try to find by type
                if ("PRODUCT_ID".equals(identifierType)) {
                    Optional<Products> product = productRepository.findById(identifier);
                    if (product.isPresent()) {
                        Products p = product.get();
                        p.setPrice(newPrice);
                        p.setUpdatedAt(LocalDateTime.now());
                        productRepository.save(p);
                        updated = true;
                    }
                } else if ("VARIANT_ID".equals(identifierType)) {
                    Optional<ProductVariant> variant = variantRepository.findById(identifier);
                    if (variant.isPresent()) {
                        ProductVariant v = variant.get();
                        v.setPrice(newPrice);
                        double newMrp = parseDouble(getMappedValue(row, mapping, "new_mrp"), 0);
                        if (newMrp > 0) v.setMrp(newMrp);
                        v.setUpdatedAt(LocalDateTime.now());
                        variantRepository.save(v);
                        updated = true;
                    }
                } else {
                    // Try product ID first
                    Optional<Products> product = productRepository.findById(identifier);
                    if (product.isPresent()) {
                        Products p = product.get();
                        p.setPrice(newPrice);
                        p.setUpdatedAt(LocalDateTime.now());
                        productRepository.save(p);
                        updated = true;
                    } else {
                        // Try variant SKU
                        Optional<ProductVariant> variant = variantRepository.findBySku(identifier);
                        if (variant.isPresent()) {
                            ProductVariant v = variant.get();
                            v.setPrice(newPrice);
                            double newMrp = parseDouble(getMappedValue(row, mapping, "new_mrp"), 0);
                            if (newMrp > 0) v.setMrp(newMrp);
                            v.setUpdatedAt(LocalDateTime.now());
                            variantRepository.save(v);
                            updated = true;
                        }
                    }
                }
                
                if (updated) {
                    success++;
                } else {
                    errors++;
                    addRowError(job, rowNum, "VALIDATION", "Item not found: " + identifier, row);
                }
                
            } catch (Exception e) {
                errors++;
                addRowError(job, rowNum, "SYSTEM", e.getMessage(), row);
            }
            
            processed++;
            
            if (processed % 10 == 0) {
                job.updateProgress(processed, success, errors);
                jobRepository.save(job);
            }
        }
        
        job.updateProgress(processed, success, errors);
    }
    
    // ==================== STOCK UPDATE PROCESSING ====================
    
    private void processStockUpdates(BulkUploadJob job, List<Map<String, String>> records, BulkUploadRequest request) {
        Map<String, String> mapping = request.getColumnMapping();
        int processed = 0;
        int success = 0;
        int errors = 0;
        
        for (Map<String, String> row : records) {
            int rowNum = Integer.parseInt(row.getOrDefault("_rowNumber", "0"));
            
            try {
                String identifier = getMappedValue(row, mapping, "identifier");
                String identifierType = getMappedValue(row, mapping, "identifier_type");
                int quantity = (int) parseDouble(getMappedValue(row, mapping, "quantity"), 0);
                String adjustmentType = getMappedValue(row, mapping, "adjustment_type");
                boolean isAbsolute = !"RELATIVE".equalsIgnoreCase(adjustmentType);
                
                if (identifier == null) {
                    errors++;
                    addRowError(job, rowNum, "VALIDATION", "Identifier is required", row);
                    processed++;
                    continue;
                }
                
                boolean updated = false;
                
                // Try to find by type
                if ("PRODUCT_ID".equals(identifierType)) {
                    Optional<Products> product = productRepository.findById(identifier);
                    if (product.isPresent()) {
                        Products p = product.get();
                        if (isAbsolute) {
                            p.setProductQuantity(Math.max(0, quantity));
                        } else {
                            p.setProductQuantity(Math.max(0, p.getProductQuantity() + quantity));
                        }
                        // Update stock status
                        p.setStockStatus(p.getProductQuantity() > 0 ? "In Stock" : "Out of Stock");
                        p.setUpdatedAt(LocalDateTime.now());
                        productRepository.save(p);
                        updated = true;
                    }
                } else if ("VARIANT_ID".equals(identifierType)) {
                    Optional<ProductVariant> variant = variantRepository.findById(identifier);
                    if (variant.isPresent()) {
                        ProductVariant v = variant.get();
                        if (isAbsolute) {
                            v.setStockQuantity(Math.max(0, quantity));
                        } else {
                            v.setStockQuantity(Math.max(0, v.getStockQuantity() + quantity));
                        }
                        v.setUpdatedAt(LocalDateTime.now());
                        variantRepository.save(v);
                        updated = true;
                    }
                } else {
                    // Try product ID
                    Optional<Products> product = productRepository.findById(identifier);
                    if (product.isPresent()) {
                        Products p = product.get();
                        if (isAbsolute) {
                            p.setProductQuantity(Math.max(0, quantity));
                        } else {
                            p.setProductQuantity(Math.max(0, p.getProductQuantity() + quantity));
                        }
                        p.setStockStatus(p.getProductQuantity() > 0 ? "In Stock" : "Out of Stock");
                        p.setUpdatedAt(LocalDateTime.now());
                        productRepository.save(p);
                        updated = true;
                    } else {
                        // Try variant SKU
                        Optional<ProductVariant> variant = variantRepository.findBySku(identifier);
                        if (variant.isPresent()) {
                            ProductVariant v = variant.get();
                            if (isAbsolute) {
                                v.setStockQuantity(Math.max(0, quantity));
                            } else {
                                v.setStockQuantity(Math.max(0, v.getStockQuantity() + quantity));
                            }
                            v.setUpdatedAt(LocalDateTime.now());
                            variantRepository.save(v);
                            updated = true;
                        }
                    }
                }
                
                if (updated) {
                    success++;
                } else {
                    errors++;
                    addRowError(job, rowNum, "VALIDATION", "Item not found: " + identifier, row);
                }
                
            } catch (Exception e) {
                errors++;
                addRowError(job, rowNum, "SYSTEM", e.getMessage(), row);
            }
            
            processed++;
            
            if (processed % 10 == 0) {
                job.updateProgress(processed, success, errors);
                jobRepository.save(job);
            }
        }
        
        job.updateProgress(processed, success, errors);
    }
    
    // ==================== JOB QUERIES ====================
    
    public BulkUploadResponse getJobStatus(String jobId) {
        return jobRepository.findById(jobId)
                .map(BulkUploadResponse::fromJob)
                .orElse(BulkUploadResponse.error("Job not found"));
    }
    
    public List<BulkUploadResponse> getVendorJobs(String vendorId, int page, int size) {
        Page<BulkUploadJob> jobs = jobRepository.findByVendorIdOrderByCreatedAtDesc(
                vendorId, PageRequest.of(page, size));
        return jobs.map(BulkUploadResponse::fromJob).getContent();
    }
    
    public void cancelJob(String jobId, String vendorId) {
        BulkUploadJob job = jobRepository.findById(jobId).orElse(null);
        if (job != null && job.getVendorId().equals(vendorId) && job.isRunning()) {
            job.setStatus(UploadStatus.CANCELLED);
            job.setCompletedAt(LocalDateTime.now());
            jobRepository.save(job);
        }
    }
    
    // ==================== TEMPLATES ====================
    
    public UploadTemplateInfo getTemplate(String uploadType) {
        return switch (uploadType.toUpperCase()) {
            case "PRODUCTS" -> UploadTemplateInfo.getProductTemplate();
            case "VARIANTS" -> UploadTemplateInfo.getVariantTemplate();
            case "PRICE_UPDATE" -> UploadTemplateInfo.getPriceUpdateTemplate();
            case "STOCK_UPDATE" -> UploadTemplateInfo.getStockUpdateTemplate();
            default -> throw new IllegalArgumentException("Unknown template type: " + uploadType);
        };
    }
    
    // ==================== HELPER METHODS ====================
    
    private String getMappedValue(Map<String, String> row, Map<String, String> mapping, String field) {
        if (mapping == null || mapping.isEmpty()) {
            // Try direct field name
            String value = row.get(field);
            if (value != null) return value.trim();
            
            // Try common variations
            for (String key : row.keySet()) {
                if (key.equalsIgnoreCase(field) || 
                    key.replace("_", "").equalsIgnoreCase(field.replace("_", "")) ||
                    key.replace(" ", "_").equalsIgnoreCase(field)) {
                    return row.get(key).trim();
                }
            }
            return null;
        }
        
        String csvColumn = mapping.get(field);
        if (csvColumn == null) return null;
        return row.get(csvColumn);
    }
    
    private double parseDouble(String value, double defaultValue) {
        if (value == null || value.isEmpty()) return defaultValue;
        try {
            return Double.parseDouble(value.replaceAll("[^0-9.-]", ""));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    private boolean parseBoolean(String value, boolean defaultValue) {
        if (value == null || value.isEmpty()) return defaultValue;
        return "true".equalsIgnoreCase(value) || "1".equals(value) || "yes".equalsIgnoreCase(value);
    }
    
    private void addRowError(BulkUploadJob job, int rowNum, String errorType, String message, Map<String, String> rowData) {
        if (job.getErrors() == null) {
            job.setErrors(new ArrayList<>());
        }
        
        // Limit errors stored
        if (job.getErrors().size() < 1000) {
            job.getErrors().add(RowError.builder()
                    .rowNumber(rowNum)
                    .errorType(errorType)
                    .errorMessage(message)
                    .rowData(rowData)
                    .build());
        }
    }
    
    private String generateProductSku(String productName, String vendorId) {
        String prefix = productName.length() >= 3 
                ? productName.substring(0, 3).toUpperCase().replaceAll("[^A-Z]", "X")
                : "PRD";
        return prefix + "-" + vendorId.substring(0, Math.min(4, vendorId.length())).toUpperCase() 
                + "-" + System.currentTimeMillis() % 100000;
    }
    
    private String generateVariantSku(String productId, Map<String, String> attributes) {
        StringBuilder sku = new StringBuilder();
        sku.append(productId.substring(0, Math.min(6, productId.length())).toUpperCase());
        
        for (String value : attributes.values()) {
            sku.append("-").append(value.substring(0, Math.min(3, value.length())).toUpperCase());
        }
        
        sku.append("-").append(System.currentTimeMillis() % 10000);
        return sku.toString();
    }
}
