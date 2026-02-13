package com.odop.root.odopfeatures.artisans.dto;

import com.odop.root.odopfeatures.artisans.model.ArtisanStory.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

/**
 * DTOs for Artisan Stories API
 */
public class ArtisanStoryDto {
    
    // ==================== Response DTOs ====================
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ArtisanStoryResponse {
        private String id;
        private String slug;
        
        // Artisan Info
        private String artisanName;
        private String artisanNameHindi;
        private String title;
        private String titleHindi;
        private String profileImageUrl;
        private String coverImageUrl;
        
        // Location
        private String village;
        private String district;
        private String state;
        private String stateCode;
        private LocationCoordinates coordinates;
        
        // Craft Info
        private String primaryCraft;
        private String primaryCraftHindi;
        private String craftCategoryId;
        private List<String> additionalCrafts;
        private int yearsOfExperience;
        private int generationsInCraft;
        private String craftOriginStory;
        private String craftOriginStoryHindi;
        
        // Story Content
        private String shortBio;
        private String shortBioHindi;
        private String fullStory;
        private String fullStoryHindi;
        private String quote;
        private String quoteHindi;
        private List<StorySectionDto> storySections;
        
        // Family
        private FamilyBackgroundDto familyBackground;
        private String teachingPhilosophy;
        private int apprenticesTrained;
        private List<String> familyMembersInCraft;
        
        // Media
        private List<MediaItemDto> gallery;
        private List<VideoItemDto> videos;
        private String primaryVideoUrl;
        private String primaryVideoThumbnail;
        
        // Products
        private List<String> featuredProductIds;
        private List<SignatureDto> signatureWorks;
        private String workshopDescription;
        private String workshopImageUrl;
        
        // Recognition
        private List<AwardDto> awards;
        private List<String> certifications;
        private boolean giTagHolder;
        private String giTagDetails;
        private boolean nationalAwardee;
        private boolean stateAwardee;
        
        // Contact & Social
        private ContactInfoDto contactInfo;
        private SocialLinksDto socialLinks;
        private String vendorId;
        
        // Events
        private List<ExhibitionDto> exhibitions;
        private boolean availableForWorkshops;
        private boolean availableForCommissions;
        private String workshopDetails;
        
        // Status
        private boolean featured;
        private boolean verified;
        private long viewCount;
        private long shareCount;
        
        private List<String> tags;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ArtisanListItem {
        private String id;
        private String slug;
        private String artisanName;
        private String artisanNameHindi;
        private String title;
        private String titleHindi;
        private String profileImageUrl;
        
        private String village;
        private String district;
        private String state;
        
        private String primaryCraft;
        private String primaryCraftHindi;
        private int yearsOfExperience;
        private int generationsInCraft;
        
        private String shortBio;
        private String quote;
        
        private boolean nationalAwardee;
        private boolean stateAwardee;
        private boolean giTagHolder;
        
        private boolean featured;
        private boolean verified;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LocationCoordinates {
        private Double latitude;
        private Double longitude;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StorySectionDto {
        private String title;
        private String titleHindi;
        private String content;
        private String contentHindi;
        private String imageUrl;
        private String imageCaption;
        private int orderIndex;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FamilyBackgroundDto {
        private String fatherName;
        private String fatherCraft;
        private String grandfatherName;
        private String grandfatherCraft;
        private String familyHistory;
        private String familyHistoryHindi;
        private int generationCount;
        private String familyPhotoUrl;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MediaItemDto {
        private String url;
        private String thumbnailUrl;
        private String caption;
        private String captionHindi;
        private String type;
        private int orderIndex;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class VideoItemDto {
        private String title;
        private String titleHindi;
        private String description;
        private String videoUrl;
        private String thumbnailUrl;
        private String duration;
        private String type;
        private boolean featured;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SignatureDto {
        private String name;
        private String nameHindi;
        private String description;
        private String descriptionHindi;
        private String imageUrl;
        private String price;
        private boolean available;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AwardDto {
        private String name;
        private String nameHindi;
        private String organization;
        private int year;
        private String description;
        private String imageUrl;
        private String level;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ContactInfoDto {
        private String phone;
        private String email;
        private String address;
        private String workshopAddress;
        private String bestTimeToContact;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SocialLinksDto {
        private String facebook;
        private String instagram;
        private String youtube;
        private String twitter;
        private String website;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ExhibitionDto {
        private String name;
        private String location;
        private LocalDate startDate;
        private LocalDate endDate;
        private String description;
        private boolean upcoming;
    }
    
    // ==================== Summary DTOs ====================
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ArtisanStoriesOverview {
        private int totalStories;
        private int featuredStories;
        private int nationalAwardees;
        private int giTagHolders;
        private List<ArtisanListItem> featuredArtisans;
        private List<CraftCount> artisansByCraft;
        private List<StateCount> artisansByState;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CraftCount {
        private String craft;
        private String craftHindi;
        private long count;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StateCount {
        private String state;
        private String stateCode;
        private long count;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ArtisanFilters {
        private List<FilterOption> crafts;
        private List<FilterOption> states;
        private List<FilterOption> awardLevels;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FilterOption {
        private String value;
        private String label;
        private String labelHindi;
        private long count;
    }
    
    // ==================== Map Data ====================
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ArtisanMapMarker {
        private String id;
        private String slug;
        private String artisanName;
        private String craft;
        private String profileImageUrl;
        private String village;
        private String district;
        private String state;
        private Double latitude;
        private Double longitude;
        private boolean featured;
        private boolean nationalAwardee;
    }
    
    // ==================== Request DTOs ====================
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateArtisanStoryRequest {
        private String slug;
        
        // Artisan Info
        private String artisanName;
        private String artisanNameHindi;
        private String title;
        private String titleHindi;
        private String profileImageUrl;
        private String coverImageUrl;
        
        // Location
        private String village;
        private String district;
        private String state;
        private String stateCode;
        private String pincode;
        private Double latitude;
        private Double longitude;
        
        // Craft Info
        private String primaryCraft;
        private String primaryCraftHindi;
        private String craftCategoryId;
        private List<String> additionalCrafts;
        private int yearsOfExperience;
        private int generationsInCraft;
        private String craftOriginStory;
        private String craftOriginStoryHindi;
        
        // Story Content
        private String shortBio;
        private String shortBioHindi;
        private String fullStory;
        private String fullStoryHindi;
        private String quote;
        private String quoteHindi;
        private List<StorySection> storySections;
        
        // Family
        private FamilyBackground familyBackground;
        private String teachingPhilosophy;
        private int apprenticesTrained;
        private List<String> familyMembersInCraft;
        
        // Media
        private List<MediaItem> gallery;
        private List<VideoItem> videos;
        private String primaryVideoUrl;
        private String primaryVideoThumbnail;
        
        // Products
        private List<String> featuredProductIds;
        private List<Signature> signatureWorks;
        private String workshopDescription;
        private String workshopImageUrl;
        
        // Recognition
        private List<Award> awards;
        private List<String> certifications;
        private boolean giTagHolder;
        private String giTagDetails;
        
        // Contact
        private ContactInfo contactInfo;
        private SocialLinks socialLinks;
        private String vendorId;
        
        // Events
        private List<Exhibition> exhibitions;
        private boolean availableForWorkshops;
        private boolean availableForCommissions;
        private String workshopDetails;
        
        // Status
        private boolean featured;
        private int displayOrder;
        private List<String> tags;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateArtisanStoryRequest {
        // Same fields as Create but all optional
        private String artisanName;
        private String artisanNameHindi;
        private String title;
        private String titleHindi;
        private String profileImageUrl;
        private String coverImageUrl;
        
        private String village;
        private String district;
        private String state;
        private String stateCode;
        private String pincode;
        private Double latitude;
        private Double longitude;
        
        private String primaryCraft;
        private String primaryCraftHindi;
        private String craftCategoryId;
        private List<String> additionalCrafts;
        private Integer yearsOfExperience;
        private Integer generationsInCraft;
        private String craftOriginStory;
        private String craftOriginStoryHindi;
        
        private String shortBio;
        private String shortBioHindi;
        private String fullStory;
        private String fullStoryHindi;
        private String quote;
        private String quoteHindi;
        private List<StorySection> storySections;
        
        private FamilyBackground familyBackground;
        private String teachingPhilosophy;
        private Integer apprenticesTrained;
        private List<String> familyMembersInCraft;
        
        private List<MediaItem> gallery;
        private List<VideoItem> videos;
        private String primaryVideoUrl;
        private String primaryVideoThumbnail;
        
        private List<String> featuredProductIds;
        private List<Signature> signatureWorks;
        private String workshopDescription;
        private String workshopImageUrl;
        
        private List<Award> awards;
        private List<String> certifications;
        private Boolean giTagHolder;
        private String giTagDetails;
        
        private ContactInfo contactInfo;
        private SocialLinks socialLinks;
        private String vendorId;
        
        private List<Exhibition> exhibitions;
        private Boolean availableForWorkshops;
        private Boolean availableForCommissions;
        private String workshopDetails;
        
        private Boolean featured;
        private Integer displayOrder;
        private List<String> tags;
        
        private StoryStatus status;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AddMediaRequest {
        private String url;
        private String thumbnailUrl;
        private String caption;
        private String captionHindi;
        private MediaType type;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AddVideoRequest {
        private String title;
        private String titleHindi;
        private String description;
        private String videoUrl;
        private String thumbnailUrl;
        private String duration;
        private VideoType type;
        private boolean featured;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AddAwardRequest {
        private String name;
        private String nameHindi;
        private String organization;
        private int year;
        private String description;
        private String imageUrl;
        private AwardLevel level;
    }
}
