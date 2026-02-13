package com.odop.root.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * MongoDB document to store image metadata.
 * Keeps track of all uploaded images for management and cleanup.
 * Integrated with Cloudinary for image storage.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "images")
public class ImageMetadata {

    @Id
    private String id;

    @Indexed
    private String publicId;           // Cloudinary public_id

    @Indexed
    private String entityType;         // product, vendor, customer, category

    @Indexed
    private String entityId;           // ID of associated entity

    private String originalUrl;        // Original image URL
    private String secureUrl;          // HTTPS URL
    private String thumbnailUrl;       // 150x150 thumbnail
    private String mediumUrl;          // 500x500 medium size
    private String largeUrl;           // 1000x1000 large size

    private String format;             // jpg, png, webp, etc.
    private Long fileSize;             // Size in bytes
    private Integer width;             // Original width
    private Integer height;            // Original height

    private String originalFilename;   // Original uploaded filename
    private String description;        // Alt text / description
    @Builder.Default
    private Boolean isPrimary = false; // Is primary image for entity

    private String uploadedBy;         // User ID who uploaded
    private String userType;           // admin, vendor, customer

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Map<String, Object> cloudinaryData; // Full Cloudinary response

    /**
     * Convenience constructor that sets defaults.
     */
    public ImageMetadata(String publicId, String entityType, String entityId) {
        this.publicId = publicId;
        this.entityType = entityType;
        this.entityId = entityId;
        this.createdAt = LocalDateTime.now();
        this.isPrimary = false;
    }
}
