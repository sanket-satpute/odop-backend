package com.odop.root.controller;

import com.odop.root.dto.ImageUploadResponse;
import com.odop.root.dto.MultiImageUploadResponse;
import com.odop.root.models.ImageMetadata;
import com.odop.root.services.ImageUploadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST Controller for Image Upload operations
 * 
 * Endpoints:
 * - POST /odop/images/upload - Upload single image
 * - POST /odop/images/upload-multiple - Upload multiple images
 * - GET /odop/images/{entityType}/{entityId} - Get all images for entity
 * - GET /odop/images/{imageId} - Get image metadata by ID
 * - DELETE /odop/images/{publicId} - Delete image
 * - DELETE /odop/images/{entityType}/{entityId}/all - Delete all images for entity
 * - PUT /odop/images/{imageId}/set-primary - Set image as primary
 * - GET /odop/images/transform - Get transformed image URL
 * - GET /odop/images/health - Health check
 */
@RestController
@RequestMapping("/odop/images")
@CrossOrigin(origins = "*")
public class ImageUploadController {

    private static final Logger logger = LoggerFactory.getLogger(ImageUploadController.class);

    @Autowired
    private ImageUploadService imageUploadService;

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Image Upload Service");
        response.put("timestamp", System.currentTimeMillis());
        response.put("supportedTypes", "JPEG, PNG, GIF, WebP");
        response.put("maxSizeMB", 10);
        return ResponseEntity.ok(response);
    }

    /**
     * Upload a single image
     * 
     * @param file The image file
     * @param entityType Type of entity (product, vendor, customer, category)
     * @param entityId ID of the entity to associate with
     * @param description Optional description/alt text
     * @param uploadedBy User ID who is uploading
     * @param userType Type of user (admin, vendor, customer)
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImageUploadResponse> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("entityType") String entityType,
            @RequestParam(value = "entityId", required = false) String entityId,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "uploadedBy", required = false) String uploadedBy,
            @RequestParam(value = "userType", required = false) String userType) {

        logger.info("📤 Upload request - Type: {}, Entity: {}, File: {}", 
                   entityType, entityId, file.getOriginalFilename());

        ImageUploadResponse response = imageUploadService.uploadImage(
            file, entityType, entityId, description, uploadedBy, userType);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Upload multiple images at once
     */
    @PostMapping(value = "/upload-multiple", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MultiImageUploadResponse> uploadMultipleImages(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam("entityType") String entityType,
            @RequestParam(value = "entityId", required = false) String entityId,
            @RequestParam(value = "uploadedBy", required = false) String uploadedBy,
            @RequestParam(value = "userType", required = false) String userType) {

        logger.info("📤 Multiple upload request - Type: {}, Entity: {}, Files: {}", 
                   entityType, entityId, files.length);

        if (files.length == 0) {
            return ResponseEntity.badRequest()
                .body(MultiImageUploadResponse.error("No files provided"));
        }

        if (files.length > 10) {
            return ResponseEntity.badRequest()
                .body(MultiImageUploadResponse.error("Maximum 10 files allowed per request"));
        }

        MultiImageUploadResponse response = imageUploadService.uploadMultipleImages(
            files, entityType, entityId, uploadedBy, userType);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(response);
        }
    }

    /**
     * Get all images for a specific entity
     */
    @GetMapping("/{entityType}/{entityId}")
    public ResponseEntity<Map<String, Object>> getImagesForEntity(
            @PathVariable String entityType,
            @PathVariable String entityId) {

        List<ImageMetadata> images = imageUploadService.getImagesForEntity(entityType, entityId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("entityType", entityType);
        response.put("entityId", entityId);
        response.put("count", images.size());
        response.put("images", images);

        return ResponseEntity.ok(response);
    }

    /**
     * Get primary image for an entity
     */
    @GetMapping("/{entityType}/{entityId}/primary")
    public ResponseEntity<Map<String, Object>> getPrimaryImage(
            @PathVariable String entityType,
            @PathVariable String entityId) {

        Optional<ImageMetadata> primaryImage = imageUploadService.getPrimaryImage(entityType, entityId);
        
        Map<String, Object> response = new HashMap<>();
        if (primaryImage.isPresent()) {
            response.put("success", true);
            response.put("image", primaryImage.get());
        } else {
            response.put("success", false);
            response.put("message", "No primary image found");
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Delete an image by its Cloudinary public ID
     */
    @DeleteMapping("/{publicId}")
    public ResponseEntity<Map<String, Object>> deleteImage(@PathVariable String publicId) {
        // Handle folder paths in public ID (replace / with _)
        String normalizedId = publicId.replace("_", "/");
        
        boolean deleted = imageUploadService.deleteImage(normalizedId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", deleted);
        response.put("publicId", normalizedId);
        response.put("message", deleted ? "Image deleted successfully" : "Failed to delete image");

        return deleted ? ResponseEntity.ok(response) : 
                        ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * Delete all images for an entity
     */
    @DeleteMapping("/{entityType}/{entityId}/all")
    public ResponseEntity<Map<String, Object>> deleteAllImagesForEntity(
            @PathVariable String entityType,
            @PathVariable String entityId) {

        int deletedCount = imageUploadService.deleteAllImagesForEntity(entityType, entityId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("entityType", entityType);
        response.put("entityId", entityId);
        response.put("deletedCount", deletedCount);
        response.put("message", deletedCount + " images deleted");

        return ResponseEntity.ok(response);
    }

    /**
     * Set an image as primary for its entity
     */
    @PutMapping("/{imageId}/set-primary")
    public ResponseEntity<Map<String, Object>> setPrimaryImage(@PathVariable String imageId) {
        boolean success = imageUploadService.setPrimaryImage(imageId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("imageId", imageId);
        response.put("message", success ? "Image set as primary" : "Failed to set primary image");

        return success ? ResponseEntity.ok(response) : 
                        ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * Get a transformed/resized image URL
     */
    @GetMapping("/transform")
    public ResponseEntity<Map<String, Object>> getTransformedUrl(
            @RequestParam String publicId,
            @RequestParam(defaultValue = "500") int width,
            @RequestParam(defaultValue = "500") int height,
            @RequestParam(defaultValue = "fill") String crop) {

        String transformedUrl = imageUploadService.getTransformedUrl(publicId, width, height, crop);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("publicId", publicId);
        response.put("width", width);
        response.put("height", height);
        response.put("crop", crop);
        response.put("url", transformedUrl);

        return ResponseEntity.ok(response);
    }

    /**
     * Upload product image (convenience endpoint)
     */
    @PostMapping(value = "/product/{productId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImageUploadResponse> uploadProductImage(
            @PathVariable String productId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "vendorId", required = false) String vendorId) {

        return uploadImage(file, "product", productId, description, vendorId, "vendor");
    }

    /**
     * Upload vendor profile image (convenience endpoint)
     */
    @PostMapping(value = "/vendor/{vendorId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImageUploadResponse> uploadVendorImage(
            @PathVariable String vendorId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "description", required = false) String description) {

        return uploadImage(file, "vendor", vendorId, description, vendorId, "vendor");
    }

    /**
     * Upload customer profile image (convenience endpoint)
     */
    @PostMapping(value = "/customer/{customerId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImageUploadResponse> uploadCustomerImage(
            @PathVariable String customerId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "description", required = false) String description) {

        return uploadImage(file, "customer", customerId, description, customerId, "customer");
    }

    /**
     * Upload category image (convenience endpoint)
     */
    @PostMapping(value = "/category/{categoryId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImageUploadResponse> uploadCategoryImage(
            @PathVariable String categoryId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "adminId", required = false) String adminId) {

        return uploadImage(file, "category", categoryId, description, adminId, "admin");
    }
}
