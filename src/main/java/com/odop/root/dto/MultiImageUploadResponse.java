package com.odop.root.dto;

import java.util.List;

/**
 * Response DTO for multiple image upload operations
 */
public class MultiImageUploadResponse {

    private boolean success;
    private String message;
    private int totalFiles;
    private int successCount;
    private int failedCount;
    private List<ImageUploadResponse> uploadedImages;
    private List<String> failedFiles;
    private String entityType;
    private String entityId;

    // Static factory methods
    public static MultiImageUploadResponse success(List<ImageUploadResponse> images, String entityType, String entityId) {
        MultiImageUploadResponse response = new MultiImageUploadResponse();
        response.setSuccess(true);
        response.setMessage("Images uploaded successfully");
        response.setUploadedImages(images);
        response.setSuccessCount(images.size());
        response.setTotalFiles(images.size());
        response.setFailedCount(0);
        response.setEntityType(entityType);
        response.setEntityId(entityId);
        return response;
    }

    public static MultiImageUploadResponse partial(List<ImageUploadResponse> successImages, 
                                                     List<String> failedFiles,
                                                     String entityType, String entityId) {
        MultiImageUploadResponse response = new MultiImageUploadResponse();
        response.setSuccess(successImages.size() > 0);
        response.setMessage("Some images failed to upload");
        response.setUploadedImages(successImages);
        response.setFailedFiles(failedFiles);
        response.setSuccessCount(successImages.size());
        response.setFailedCount(failedFiles.size());
        response.setTotalFiles(successImages.size() + failedFiles.size());
        response.setEntityType(entityType);
        response.setEntityId(entityId);
        return response;
    }

    public static MultiImageUploadResponse error(String message) {
        MultiImageUploadResponse response = new MultiImageUploadResponse();
        response.setSuccess(false);
        response.setMessage(message);
        return response;
    }

    // Constructors
    public MultiImageUploadResponse() {}

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

    public int getTotalFiles() {
        return totalFiles;
    }

    public void setTotalFiles(int totalFiles) {
        this.totalFiles = totalFiles;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(int successCount) {
        this.successCount = successCount;
    }

    public int getFailedCount() {
        return failedCount;
    }

    public void setFailedCount(int failedCount) {
        this.failedCount = failedCount;
    }

    public List<ImageUploadResponse> getUploadedImages() {
        return uploadedImages;
    }

    public void setUploadedImages(List<ImageUploadResponse> uploadedImages) {
        this.uploadedImages = uploadedImages;
    }

    public List<String> getFailedFiles() {
        return failedFiles;
    }

    public void setFailedFiles(List<String> failedFiles) {
        this.failedFiles = failedFiles;
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
}
