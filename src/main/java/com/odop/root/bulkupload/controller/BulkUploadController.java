package com.odop.root.bulkupload.controller;

import com.odop.root.bulkupload.dto.*;
import com.odop.root.bulkupload.model.BulkUploadJob;
import com.odop.root.bulkupload.service.BulkUploadService;
import com.odop.root.bulkupload.service.CsvParserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for bulk upload operations
 */
@RestController
@RequestMapping("/odop/bulk-upload")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class BulkUploadController {
    
    private final BulkUploadService bulkUploadService;
    private final CsvParserService csvParserService;
    
    // ==================== UPLOAD ENDPOINTS ====================
    
    /**
     * Upload CSV file for processing
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<?> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("uploadType") String uploadType,
            @RequestParam(value = "updateExisting", defaultValue = "false") boolean updateExisting,
            @RequestParam(value = "skipInvalid", defaultValue = "true") boolean skipInvalid,
            @RequestParam(value = "generateSkus", defaultValue = "true") boolean generateSkus,
            @RequestParam(value = "hasHeader", defaultValue = "true") boolean hasHeader,
            @RequestParam(value = "defaultCategoryId", required = false) String defaultCategoryId,
            Authentication auth) {
        
        try {
            String vendorId = auth.getName();
            
            BulkUploadRequest request = BulkUploadRequest.builder()
                    .uploadType(uploadType.toUpperCase())
                    .updateExisting(updateExisting)
                    .skipInvalid(skipInvalid)
                    .generateSkus(generateSkus)
                    .hasHeader(hasHeader)
                    .defaultCategoryId(defaultCategoryId)
                    .build();
            
            // Create job
            BulkUploadJob job = bulkUploadService.createUploadJob(vendorId, file, request);
            
            // Process asynchronously
            bulkUploadService.processUploadAsync(job.getId(), file, request);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Upload started. You can track progress using the job ID.",
                    "jobId", job.getId(),
                    "totalRows", job.getTotalRows()
            ));
            
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Error processing upload", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Failed to process upload"
            ));
        }
    }
    
    /**
     * Preview CSV file before upload
     */
    @PostMapping(value = "/preview", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<?> previewFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "hasHeader", defaultValue = "true") boolean hasHeader,
            @RequestParam(value = "maxRows", defaultValue = "10") int maxRows) {
        
        try {
            CsvParserService.ValidationResult validation = csvParserService.validateFile(file);
            if (!validation.isValid()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "errors", validation.getErrors()
                ));
            }
            
            CsvParserService.ParseResult preview = csvParserService.previewFile(file, maxRows, hasHeader);
            
            if (!preview.isSuccess()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "errors", preview.getErrors()
                ));
            }
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "headers", preview.getHeaders(),
                    "previewRows", preview.getRecords(),
                    "totalRows", preview.getTotalRows(),
                    "previewCount", preview.getPreviewCount()
            ));
            
        } catch (Exception e) {
            log.error("Error previewing file", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Failed to preview file"
            ));
        }
    }
    
    // ==================== JOB MANAGEMENT ====================
    
    /**
     * Get job status
     */
    @GetMapping("/job/{jobId}")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<?> getJobStatus(@PathVariable String jobId) {
        BulkUploadResponse response = bulkUploadService.getJobStatus(jobId);
        if (response.getJobId() == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get vendor's upload history
     */
    @GetMapping("/jobs")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<?> getVendorJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication auth) {
        
        String vendorId = auth.getName();
        List<BulkUploadResponse> jobs = bulkUploadService.getVendorJobs(vendorId, page, size);
        
        return ResponseEntity.ok(Map.of(
                "success", true,
                "jobs", jobs,
                "page", page,
                "size", size
        ));
    }
    
    /**
     * Cancel a running job
     */
    @PostMapping("/job/{jobId}/cancel")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<?> cancelJob(@PathVariable String jobId, Authentication auth) {
        try {
            String vendorId = auth.getName();
            bulkUploadService.cancelJob(jobId, vendorId);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Job cancellation requested"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
    
    // ==================== TEMPLATES ====================
    
    /**
     * Get template information for an upload type
     */
    @GetMapping("/template/{uploadType}")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<?> getTemplate(@PathVariable String uploadType) {
        try {
            UploadTemplateInfo template = bulkUploadService.getTemplate(uploadType);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "template", template
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Download CSV template
     */
    @GetMapping("/template/{uploadType}/download")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<byte[]> downloadTemplate(@PathVariable String uploadType) {
        try {
            UploadTemplateInfo template = bulkUploadService.getTemplate(uploadType);
            String csvContent = template.getSampleCsvContent();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/csv"));
            headers.setContentDispositionFormData("attachment", 
                    uploadType.toLowerCase() + "_template.csv");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(csvContent.getBytes());
                    
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    /**
     * Get all available template types
     */
    @GetMapping("/templates")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<?> getAllTemplates() {
        return ResponseEntity.ok(Map.of(
                "success", true,
                "templates", List.of(
                        Map.of("type", "PRODUCTS", "description", "Full product import"),
                        Map.of("type", "VARIANTS", "description", "Product variants with size, color"),
                        Map.of("type", "PRICE_UPDATE", "description", "Bulk price updates"),
                        Map.of("type", "STOCK_UPDATE", "description", "Bulk stock updates")
                )
        ));
    }
    
    // ==================== HEALTH CHECK ====================
    
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "BulkUpload"
        ));
    }
}
