package com.odop.root.bulkupload.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Model for tracking bulk upload jobs
 */
@Document(collection = "bulk_uploads")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkUploadJob {
    
    @Id
    private String id;
    
    @Indexed
    private String vendorId;
    
    // Original file info
    private String originalFileName;
    private String storedFileName;
    private String fileUrl;
    private long fileSize;
    
    // Upload type - products, variants, categories, prices, stock
    @Indexed
    private UploadType uploadType;
    
    // Processing status
    @Indexed
    private UploadStatus status;
    
    // Processing stats
    private int totalRows;
    private int processedRows;
    private int successCount;
    private int errorCount;
    private int skippedCount;
    
    // Progress percentage (0-100)
    private int progressPercent;
    
    // Error details
    private List<RowError> errors;
    
    // Mapping configuration used
    private Map<String, String> columnMapping;
    
    // Options
    private boolean updateExisting;
    private boolean skipInvalid;
    private boolean generateSkus;
    
    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime lastUpdatedAt;
    
    // Processing time in seconds
    private long processingTimeSeconds;
    
    /**
     * Error for a specific row
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RowError {
        private int rowNumber;
        private String errorMessage;
        private String errorType;  // VALIDATION, DUPLICATE, SYSTEM
        private Map<String, String> rowData;
    }
    
    /**
     * Type of bulk upload
     */
    public enum UploadType {
        PRODUCTS,           // Full product import
        VARIANTS,           // Product variants
        CATEGORIES,         // Category import
        PRICE_UPDATE,       // Bulk price update
        STOCK_UPDATE,       // Bulk stock update
        IMAGES              // Bulk image URLs
    }
    
    /**
     * Status of upload job
     */
    public enum UploadStatus {
        PENDING,            // File uploaded, waiting to start
        VALIDATING,         // Validating file structure
        PROCESSING,         // Processing rows
        COMPLETED,          // Successfully completed
        COMPLETED_WITH_ERRORS, // Completed but had some errors
        FAILED,             // Failed completely
        CANCELLED           // Cancelled by user
    }
    
    /**
     * Check if job is still running
     */
    public boolean isRunning() {
        return status == UploadStatus.PENDING 
            || status == UploadStatus.VALIDATING 
            || status == UploadStatus.PROCESSING;
    }
    
    /**
     * Update progress
     */
    public void updateProgress(int processed, int success, int errors) {
        this.processedRows = processed;
        this.successCount = success;
        this.errorCount = errors;
        this.progressPercent = totalRows > 0 ? (processed * 100) / totalRows : 0;
        this.lastUpdatedAt = LocalDateTime.now();
    }
    
    /**
     * Mark as completed
     */
    public void markCompleted() {
        this.completedAt = LocalDateTime.now();
        this.status = errorCount > 0 ? UploadStatus.COMPLETED_WITH_ERRORS : UploadStatus.COMPLETED;
        if (startedAt != null) {
            this.processingTimeSeconds = java.time.Duration.between(startedAt, completedAt).getSeconds();
        }
        this.progressPercent = 100;
    }
    
    /**
     * Mark as failed
     */
    public void markFailed(String reason) {
        this.completedAt = LocalDateTime.now();
        this.status = UploadStatus.FAILED;
        if (errors == null) {
            errors = new java.util.ArrayList<>();
        }
        errors.add(RowError.builder()
                .rowNumber(0)
                .errorType("SYSTEM")
                .errorMessage(reason)
                .build());
    }
}
