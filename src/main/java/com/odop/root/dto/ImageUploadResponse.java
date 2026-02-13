package com.odop.root.dto;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Response DTO for image upload operations
 */
public class ImageUploadResponse {

    private boolean success;
    private String message;
    private String imageId;          // Cloudinary public_id
    private String imageUrl;         // Original image URL
    private String thumbnailUrl;     // Thumbnail URL (150x150)
    private String mediumUrl;        // Medium size URL (500x500)
    private String largeUrl;         // Large size URL (1000x1000)
    private String secureUrl;        // HTTPS URL
    private String format;           // Image format (jpg, png, etc.)
    private Long size;               // File size in bytes
    private Integer width;           // Original width
    private Integer height;          // Original height
    private String entityType;       // product, vendor, customer, category
    private String entityId;         // Associated entity ID
    private LocalDateTime uploadedAt;
    private Map<String, Object> metadata; // Additional metadata

    // Static factory methods for common responses
    public static ImageUploadResponse success(String message, String imageUrl, String imageId) {
        ImageUploadResponse response = new ImageUploadResponse();
        response.setSuccess(true);
        response.setMessage(message);
        response.setImageUrl(imageUrl);
        response.setImageId(imageId);
        response.setUploadedAt(LocalDateTime.now());
        return response;
    }

    public static ImageUploadResponse error(String message) {
        ImageUploadResponse response = new ImageUploadResponse();
        response.setSuccess(false);
        response.setMessage(message);
        return response;
    }

    // Constructors
    public ImageUploadResponse() {}

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getMediumUrl() {
        return mediumUrl;
    }

    public void setMediumUrl(String mediumUrl) {
        this.mediumUrl = mediumUrl;
    }

    public String getLargeUrl() {
        return largeUrl;
    }

    public void setLargeUrl(String largeUrl) {
        this.largeUrl = largeUrl;
    }

    public String getSecureUrl() {
        return secureUrl;
    }

    public void setSecureUrl(String secureUrl) {
        this.secureUrl = secureUrl;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}
