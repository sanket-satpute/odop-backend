package com.odop.root.repository;

import com.odop.root.models.ImageMetadata;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for ImageMetadata document operations
 */
@Repository
public interface ImageMetadataRepository extends MongoRepository<ImageMetadata, String> {

    /**
     * Find all images for a specific entity
     */
    List<ImageMetadata> findByEntityTypeAndEntityId(String entityType, String entityId);

    /**
     * Find primary image for an entity
     */
    Optional<ImageMetadata> findByEntityTypeAndEntityIdAndIsPrimaryTrue(String entityType, String entityId);

    /**
     * Find image by Cloudinary public ID
     */
    Optional<ImageMetadata> findByPublicId(String publicId);

    /**
     * Find all images uploaded by a specific user
     */
    List<ImageMetadata> findByUploadedBy(String uploadedBy);

    /**
     * Find all images of a specific entity type
     */
    List<ImageMetadata> findByEntityType(String entityType);

    /**
     * Count images for an entity
     */
    long countByEntityTypeAndEntityId(String entityType, String entityId);

    /**
     * Delete all images for an entity
     */
    void deleteByEntityTypeAndEntityId(String entityType, String entityId);

    /**
     * Delete by Cloudinary public ID
     */
    void deleteByPublicId(String publicId);

    /**
     * Find images without an associated entity (orphaned images)
     */
    @Query("{ 'entityId': null }")
    List<ImageMetadata> findOrphanedImages();

    /**
     * Find all images by entity type ordered by creation date
     */
    List<ImageMetadata> findByEntityTypeOrderByCreatedAtDesc(String entityType);
}
