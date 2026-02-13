package com.odop.root.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

/**
 * Request DTO for image upload
 */
public class ImageUploadRequest {

    @NotNull(message = "File is required")
    private MultipartFile file;

    @NotBlank(message = "Entity type is required")
    private String entityType; // product, vendor, customer, category

    private String entityId; // Optional: ID of the entity to associate image with

    private String description; // Optional: Image description/alt text

    private Boolean setAsPrimary; // Optional: Set as primary image for entity

    // Constructors
    public ImageUploadRequest() {}

    public ImageUploadRequest(MultipartFile file, String entityType) {
        this.file = file;
        this.entityType = entityType;
    }

    // Getters and Setters
    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getSetAsPrimary() {
        return setAsPrimary;
    }

    public void setSetAsPrimary(Boolean setAsPrimary) {
        this.setAsPrimary = setAsPrimary;
    }
}
