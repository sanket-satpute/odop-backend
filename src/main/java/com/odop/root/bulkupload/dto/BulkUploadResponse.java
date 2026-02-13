package com.odop.root.bulkupload.dto;

import com.odop.root.bulkupload.model.BulkUploadJob;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for bulk upload job response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkUploadResponse {
    
    private String jobId;
    private String vendorId;
    
    private String originalFileName;
    private String uploadType;
    private String status;
    
    // Progress
    private int totalRows;
    private int processedRows;
    private int successCount;
    private int errorCount;
    private int skippedCount;
    private int progressPercent;
    
    // Timing
    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private long processingTimeSeconds;
    
    // Errors (limited to first 100)
    private List<BulkUploadJob.RowError> errors;
    private int totalErrors;
    private boolean hasMoreErrors;
    
    // Messages
    private String message;
    private boolean success;
    
    /**
     * Create response from job entity
     */
    public static BulkUploadResponse fromJob(BulkUploadJob job) {
        List<BulkUploadJob.RowError> limitedErrors = null;
        int totalErrors = 0;
        boolean hasMore = false;
        
        if (job.getErrors() != null) {
            totalErrors = job.getErrors().size();
            hasMore = totalErrors > 100;
            limitedErrors = totalErrors > 100 
                    ? job.getErrors().subList(0, 100) 
                    : job.getErrors();
        }
        
        return BulkUploadResponse.builder()
                .jobId(job.getId())
                .vendorId(job.getVendorId())
                .originalFileName(job.getOriginalFileName())
                .uploadType(job.getUploadType() != null ? job.getUploadType().name() : null)
                .status(job.getStatus() != null ? job.getStatus().name() : null)
                .totalRows(job.getTotalRows())
                .processedRows(job.getProcessedRows())
                .successCount(job.getSuccessCount())
                .errorCount(job.getErrorCount())
                .skippedCount(job.getSkippedCount())
                .progressPercent(job.getProgressPercent())
                .createdAt(job.getCreatedAt())
                .startedAt(job.getStartedAt())
                .completedAt(job.getCompletedAt())
                .processingTimeSeconds(job.getProcessingTimeSeconds())
                .errors(limitedErrors)
                .totalErrors(totalErrors)
                .hasMoreErrors(hasMore)
                .success(job.getStatus() == BulkUploadJob.UploadStatus.COMPLETED)
                .build();
    }
    
    /**
     * Create error response
     */
    public static BulkUploadResponse error(String message) {
        return BulkUploadResponse.builder()
                .success(false)
                .message(message)
                .build();
    }
    
    /**
     * Create success response
     */
    public static BulkUploadResponse success(String message, String jobId) {
        return BulkUploadResponse.builder()
                .success(true)
                .message(message)
                .jobId(jobId)
                .build();
    }
}
