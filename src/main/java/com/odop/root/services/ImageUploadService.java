package com.odop.root.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import com.odop.root.config.CloudinaryConfig;
import com.odop.root.dto.ImageUploadResponse;
import com.odop.root.dto.MultiImageUploadResponse;
import com.odop.root.models.ImageMetadata;
import com.odop.root.repository.ImageMetadataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Service for handling image uploads to Cloudinary
 * 
 * Features:
 * - Single and multiple image uploads
 * - Automatic thumbnail generation
 * - Image transformations (resize, crop)
 * - Image deletion
 * - Demo mode for testing without Cloudinary config
 */
@Service
public class ImageUploadService {

    private static final Logger logger = LoggerFactory.getLogger(ImageUploadService.class);

    @Autowired
    private Cloudinary cloudinary;

    @Autowired
    private CloudinaryConfig cloudinaryConfig;

    @Autowired
    private ImageMetadataRepository imageMetadataRepository;

    @Value("${image.upload.max-size-mb:10}")
    private int maxSizeMb;

    @Value("${image.upload.allowed-types:image/jpeg,image/png,image/gif,image/webp}")
    private String allowedTypes;

    @Value("${image.upload.product-folder:odop/products}")
    private String productFolder;

    @Value("${image.upload.vendor-folder:odop/vendors}")
    private String vendorFolder;

    @Value("${image.upload.customer-folder:odop/customers}")
    private String customerFolder;

    @Value("${image.upload.category-folder:odop/categories}")
    private String categoryFolder;

    /**
     * Upload a single image
     */
    public ImageUploadResponse uploadImage(MultipartFile file, String entityType, 
                                            String entityId, String description,
                                            String uploadedBy, String userType) {
        try {
            // Validate file
            String validationError = validateFile(file);
            if (validationError != null) {
                return ImageUploadResponse.error(validationError);
            }

            // Check if Cloudinary is configured
            if (!cloudinaryConfig.isConfigured()) {
                return handleDemoModeUpload(file, entityType, entityId);
            }

            // Get folder based on entity type
            String folder = getFolderForEntityType(entityType);

            // Upload to Cloudinary with transformations
            @SuppressWarnings("rawtypes")
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                "folder", folder,
                "resource_type", "image",
                "use_filename", true,
                "unique_filename", true,
                "overwrite", false,
                "transformation", new Transformation<>()
                    .quality("auto")
                    .fetchFormat("auto")
            ));

            // Build response with different size URLs
            ImageUploadResponse response = buildResponseFromCloudinaryResult(uploadResult, entityType, entityId);

            // Save metadata to database
            saveImageMetadata(uploadResult, entityType, entityId, description, 
                             file.getOriginalFilename(), uploadedBy, userType);

            logger.info("✅ Image uploaded successfully: {}", uploadResult.get("public_id"));
            return response;

        } catch (IOException e) {
            logger.error("❌ Failed to upload image: {}", e.getMessage());
            return ImageUploadResponse.error("Failed to upload image: " + e.getMessage());
        }
    }

    /**
     * Upload multiple images
     */
    public MultiImageUploadResponse uploadMultipleImages(MultipartFile[] files, String entityType,
                                                          String entityId, String uploadedBy, 
                                                          String userType) {
        List<ImageUploadResponse> successfulUploads = new ArrayList<>();
        List<String> failedUploads = new ArrayList<>();

        for (MultipartFile file : files) {
            ImageUploadResponse result = uploadImage(file, entityType, entityId, null, uploadedBy, userType);
            if (result.isSuccess()) {
                successfulUploads.add(result);
            } else {
                failedUploads.add(file.getOriginalFilename() + ": " + result.getMessage());
            }
        }

        if (failedUploads.isEmpty()) {
            return MultiImageUploadResponse.success(successfulUploads, entityType, entityId);
        } else if (successfulUploads.isEmpty()) {
            return MultiImageUploadResponse.error("All uploads failed");
        } else {
            return MultiImageUploadResponse.partial(successfulUploads, failedUploads, entityType, entityId);
        }
    }

    /**
     * Delete an image by its Cloudinary public ID
     */
    public boolean deleteImage(String publicId) {
        try {
            if (!cloudinaryConfig.isConfigured()) {
                logger.info("🗑️ [DEMO MODE] Would delete image: {}", publicId);
                imageMetadataRepository.deleteByPublicId(publicId);
                return true;
            }

            @SuppressWarnings("rawtypes")
            Map result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            
            if ("ok".equals(result.get("result"))) {
                imageMetadataRepository.deleteByPublicId(publicId);
                logger.info("✅ Image deleted: {}", publicId);
                return true;
            }

            logger.warn("⚠️ Failed to delete image: {}", publicId);
            return false;

        } catch (IOException e) {
            logger.error("❌ Error deleting image: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Delete all images for an entity
     */
    public int deleteAllImagesForEntity(String entityType, String entityId) {
        List<ImageMetadata> images = imageMetadataRepository.findByEntityTypeAndEntityId(entityType, entityId);
        int deletedCount = 0;

        for (ImageMetadata image : images) {
            if (deleteImage(image.getPublicId())) {
                deletedCount++;
            }
        }

        logger.info("🗑️ Deleted {} images for {} {}", deletedCount, entityType, entityId);
        return deletedCount;
    }

    /**
     * Get all images for an entity
     */
    public List<ImageMetadata> getImagesForEntity(String entityType, String entityId) {
        return imageMetadataRepository.findByEntityTypeAndEntityId(entityType, entityId);
    }

    /**
     * Get primary image for an entity
     */
    public Optional<ImageMetadata> getPrimaryImage(String entityType, String entityId) {
        return imageMetadataRepository.findByEntityTypeAndEntityIdAndIsPrimaryTrue(entityType, entityId);
    }

    /**
     * Set an image as primary for its entity
     */
    public boolean setPrimaryImage(String imageId) {
        Optional<ImageMetadata> imageOpt = imageMetadataRepository.findById(imageId);
        if (imageOpt.isEmpty()) {
            return false;
        }

        ImageMetadata image = imageOpt.get();

        // Remove primary flag from other images of same entity
        List<ImageMetadata> entityImages = imageMetadataRepository
            .findByEntityTypeAndEntityId(image.getEntityType(), image.getEntityId());
        
        for (ImageMetadata img : entityImages) {
            if (img.getIsPrimary() != null && img.getIsPrimary()) {
                img.setIsPrimary(false);
                img.setUpdatedAt(LocalDateTime.now());
                imageMetadataRepository.save(img);
            }
        }

        // Set this image as primary
        image.setIsPrimary(true);
        image.setUpdatedAt(LocalDateTime.now());
        imageMetadataRepository.save(image);

        logger.info("✅ Set image {} as primary for {} {}", imageId, image.getEntityType(), image.getEntityId());
        return true;
    }

    /**
     * Generate transformation URL for an image
     */
    public String getTransformedUrl(String publicId, int width, int height, String crop) {
        if (!cloudinaryConfig.isConfigured()) {
            return "https://via.placeholder.com/" + width + "x" + height;
        }

        return cloudinary.url()
            .transformation(new Transformation<>()
                .width(width)
                .height(height)
                .crop(crop)
                .quality("auto")
                .fetchFormat("auto"))
            .generate(publicId);
    }

    // ========== Private Helper Methods ==========

    private String validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return "File is empty or not provided";
        }

        // Check file size
        long maxSizeBytes = (long) maxSizeMb * 1024 * 1024;
        if (file.getSize() > maxSizeBytes) {
            return "File size exceeds maximum allowed size of " + maxSizeMb + "MB";
        }

        // Check content type
        String contentType = file.getContentType();
        List<String> allowed = Arrays.asList(allowedTypes.split(","));
        if (contentType == null || !allowed.contains(contentType)) {
            return "File type not allowed. Allowed types: " + allowedTypes;
        }

        return null;
    }

    private String getFolderForEntityType(String entityType) {
        return switch (entityType.toLowerCase()) {
            case "product" -> productFolder;
            case "vendor" -> vendorFolder;
            case "customer" -> customerFolder;
            case "category" -> categoryFolder;
            default -> "odop/misc";
        };
    }

    @SuppressWarnings("rawtypes")
    private ImageUploadResponse buildResponseFromCloudinaryResult(Map result, String entityType, String entityId) {
        ImageUploadResponse response = new ImageUploadResponse();
        response.setSuccess(true);
        response.setMessage("Image uploaded successfully");
        response.setImageId((String) result.get("public_id"));
        response.setImageUrl((String) result.get("url"));
        response.setSecureUrl((String) result.get("secure_url"));
        response.setFormat((String) result.get("format"));
        
        // Handle size - could be Integer or Long
        Object sizeObj = result.get("bytes");
        if (sizeObj instanceof Integer) {
            response.setSize(((Integer) sizeObj).longValue());
        } else if (sizeObj instanceof Long) {
            response.setSize((Long) sizeObj);
        }
        
        response.setWidth((Integer) result.get("width"));
        response.setHeight((Integer) result.get("height"));
        response.setEntityType(entityType);
        response.setEntityId(entityId);
        response.setUploadedAt(LocalDateTime.now());

        // Generate different size URLs
        String publicId = (String) result.get("public_id");
        response.setThumbnailUrl(getTransformedUrl(publicId, 150, 150, "fill"));
        response.setMediumUrl(getTransformedUrl(publicId, 500, 500, "fill"));
        response.setLargeUrl(getTransformedUrl(publicId, 1000, 1000, "fit"));

        return response;
    }

    @SuppressWarnings("rawtypes")
    private void saveImageMetadata(Map cloudinaryResult, String entityType, String entityId,
                                    String description, String originalFilename,
                                    String uploadedBy, String userType) {
        ImageMetadata metadata = new ImageMetadata();
        metadata.setPublicId((String) cloudinaryResult.get("public_id"));
        metadata.setEntityType(entityType);
        metadata.setEntityId(entityId);
        metadata.setOriginalUrl((String) cloudinaryResult.get("url"));
        metadata.setSecureUrl((String) cloudinaryResult.get("secure_url"));
        metadata.setFormat((String) cloudinaryResult.get("format"));
        
        // Handle size
        Object sizeObj = cloudinaryResult.get("bytes");
        if (sizeObj instanceof Integer) {
            metadata.setFileSize(((Integer) sizeObj).longValue());
        } else if (sizeObj instanceof Long) {
            metadata.setFileSize((Long) sizeObj);
        }
        
        metadata.setWidth((Integer) cloudinaryResult.get("width"));
        metadata.setHeight((Integer) cloudinaryResult.get("height"));
        metadata.setOriginalFilename(originalFilename);
        metadata.setDescription(description);
        metadata.setUploadedBy(uploadedBy);
        metadata.setUserType(userType);

        String publicId = (String) cloudinaryResult.get("public_id");
        metadata.setThumbnailUrl(getTransformedUrl(publicId, 150, 150, "fill"));
        metadata.setMediumUrl(getTransformedUrl(publicId, 500, 500, "fill"));
        metadata.setLargeUrl(getTransformedUrl(publicId, 1000, 1000, "fit"));

        // Set as primary if this is the first image for the entity
        long existingCount = imageMetadataRepository.countByEntityTypeAndEntityId(entityType, entityId);
        metadata.setIsPrimary(existingCount == 0);

        // Store full Cloudinary response
        @SuppressWarnings("unchecked")
        Map<String, Object> cloudinaryData = new HashMap<>(cloudinaryResult);
        metadata.setCloudinaryData(cloudinaryData);

        imageMetadataRepository.save(metadata);
    }

    private ImageUploadResponse handleDemoModeUpload(MultipartFile file, String entityType, String entityId) {
        // Demo mode - generate placeholder response
        String fakeId = "demo_" + UUID.randomUUID().toString().substring(0, 8);
        String placeholderUrl = "https://via.placeholder.com/500x500?text=" + 
            (file.getOriginalFilename() != null ? file.getOriginalFilename() : "Image");

        logger.info("======= DEMO MODE: Image upload simulated =======");
        logger.info("📷 File: {}", file.getOriginalFilename());
        logger.info("📦 Size: {} bytes", file.getSize());
        logger.info("🏷️ Entity: {} / {}", entityType, entityId);
        logger.info("🔗 Demo URL: {}", placeholderUrl);
        logger.info("================================================");

        ImageUploadResponse response = new ImageUploadResponse();
        response.setSuccess(true);
        response.setMessage("[DEMO MODE] Image upload simulated. Configure Cloudinary for real uploads.");
        response.setImageId(fakeId);
        response.setImageUrl(placeholderUrl);
        response.setSecureUrl(placeholderUrl);
        response.setThumbnailUrl("https://via.placeholder.com/150x150");
        response.setMediumUrl("https://via.placeholder.com/500x500");
        response.setLargeUrl("https://via.placeholder.com/1000x1000");
        response.setFormat(file.getContentType() != null ? file.getContentType().split("/")[1] : "unknown");
        response.setSize(file.getSize());
        response.setEntityType(entityType);
        response.setEntityId(entityId);
        response.setUploadedAt(LocalDateTime.now());

        return response;
    }
}
