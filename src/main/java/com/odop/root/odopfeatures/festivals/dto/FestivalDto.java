package com.odop.root.odopfeatures.festivals.dto;

import com.odop.root.odopfeatures.festivals.model.FestivalCollection;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

/**
 * DTOs for Festival Collections
 */
public class FestivalDto {
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FestivalCollectionResponse {
        private String id;
        private String slug;
        private String name;
        private String nameHindi;
        private String description;
        private String descriptionHindi;
        private String tagline;
        private String type;
        private String category;
        private LocalDate startDate;
        private LocalDate endDate;
        private LocalDate festivalDate;
        private String heroImageUrl;
        private String bannerImageUrl;
        private String thumbnailUrl;
        private String mobileHeroUrl;
        private String videoUrl;
        private String themeColor;
        private String accentColor;
        private List<String> primaryStates;
        private String regionalRelevance;
        private List<CollectionSectionResponse> sections;
        private List<GiftSuggestionResponse> giftGuide;
        private String giftGuideTitle;
        private DiscountInfoResponse discountInfo;
        private boolean hasCountdownTimer;
        private boolean hasEarlyBirdOffers;
        private boolean active;
        private boolean featured;
        private int displayOrder;
        private int year;
        private long viewCount;
        private long productCount;
        private long daysUntilFestival;
        private boolean isLive;
        
        public static FestivalCollectionResponse from(FestivalCollection festival) {
            FestivalCollectionResponse response = FestivalCollectionResponse.builder()
                    .id(festival.getId())
                    .slug(festival.getSlug())
                    .name(festival.getName())
                    .nameHindi(festival.getNameHindi())
                    .description(festival.getDescription())
                    .descriptionHindi(festival.getDescriptionHindi())
                    .tagline(festival.getTagline())
                    .type(festival.getType() != null ? festival.getType().name() : null)
                    .category(festival.getCategory() != null ? festival.getCategory().name() : null)
                    .startDate(festival.getStartDate())
                    .endDate(festival.getEndDate())
                    .festivalDate(festival.getFestivalDate())
                    .heroImageUrl(festival.getHeroImageUrl())
                    .bannerImageUrl(festival.getBannerImageUrl())
                    .thumbnailUrl(festival.getThumbnailUrl())
                    .mobileHeroUrl(festival.getMobileHeroUrl())
                    .videoUrl(festival.getVideoUrl())
                    .themeColor(festival.getThemeColor())
                    .accentColor(festival.getAccentColor())
                    .primaryStates(festival.getPrimaryStates())
                    .regionalRelevance(festival.getRegionalRelevance() != null ? 
                            festival.getRegionalRelevance().name() : null)
                    .hasCountdownTimer(festival.isHasCountdownTimer())
                    .hasEarlyBirdOffers(festival.isHasEarlyBirdOffers())
                    .active(festival.isActive())
                    .featured(festival.isFeatured())
                    .displayOrder(festival.getDisplayOrder())
                    .year(festival.getYear())
                    .viewCount(festival.getViewCount())
                    .productCount(festival.getProductCount())
                    .build();
            
            // Calculate days until festival
            if (festival.getFestivalDate() != null) {
                long days = java.time.temporal.ChronoUnit.DAYS.between(
                        LocalDate.now(), festival.getFestivalDate());
                response.setDaysUntilFestival(Math.max(0, days));
            }
            
            // Check if live
            LocalDate now = LocalDate.now();
            response.setLive(festival.isActive() && 
                    (festival.getStartDate() == null || !now.isBefore(festival.getStartDate())) &&
                    (festival.getEndDate() == null || !now.isAfter(festival.getEndDate())));
            
            // Map sections
            if (festival.getSections() != null) {
                response.setSections(festival.getSections().stream()
                        .map(CollectionSectionResponse::from)
                        .toList());
            }
            
            // Map gift guide
            if (festival.getGiftGuide() != null) {
                response.setGiftGuide(festival.getGiftGuide().stream()
                        .map(GiftSuggestionResponse::from)
                        .toList());
                response.setGiftGuideTitle(festival.getGiftGuideTitle());
            }
            
            // Map discount info
            if (festival.getDiscountInfo() != null) {
                response.setDiscountInfo(DiscountInfoResponse.from(festival.getDiscountInfo()));
            }
            
            return response;
        }
        
        public static FestivalCollectionResponse summary(FestivalCollection festival) {
            FestivalCollectionResponse response = FestivalCollectionResponse.builder()
                    .id(festival.getId())
                    .slug(festival.getSlug())
                    .name(festival.getName())
                    .nameHindi(festival.getNameHindi())
                    .tagline(festival.getTagline())
                    .thumbnailUrl(festival.getThumbnailUrl())
                    .heroImageUrl(festival.getHeroImageUrl())
                    .themeColor(festival.getThemeColor())
                    .festivalDate(festival.getFestivalDate())
                    .hasCountdownTimer(festival.isHasCountdownTimer())
                    .featured(festival.isFeatured())
                    .displayOrder(festival.getDisplayOrder())
                    .productCount(festival.getProductCount())
                    .build();
            
            if (festival.getFestivalDate() != null) {
                long days = java.time.temporal.ChronoUnit.DAYS.between(
                        LocalDate.now(), festival.getFestivalDate());
                response.setDaysUntilFestival(Math.max(0, days));
            }
            
            return response;
        }
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CollectionSectionResponse {
        private String sectionId;
        private String title;
        private String titleHindi;
        private String description;
        private String imageUrl;
        private String type;
        private List<String> productIds;
        private int displayOrder;
        
        public static CollectionSectionResponse from(FestivalCollection.CollectionSection section) {
            return CollectionSectionResponse.builder()
                    .sectionId(section.getSectionId())
                    .title(section.getTitle())
                    .titleHindi(section.getTitleHindi())
                    .description(section.getDescription())
                    .imageUrl(section.getImageUrl())
                    .type(section.getType() != null ? section.getType().name() : null)
                    .productIds(section.getProductIds())
                    .displayOrder(section.getDisplayOrder())
                    .build();
        }
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GiftSuggestionResponse {
        private String title;
        private String description;
        private String imageUrl;
        private String priceRange;
        private List<String> productIds;
        private String targetAudience;
        
        public static GiftSuggestionResponse from(FestivalCollection.GiftSuggestion suggestion) {
            return GiftSuggestionResponse.builder()
                    .title(suggestion.getTitle())
                    .description(suggestion.getDescription())
                    .imageUrl(suggestion.getImageUrl())
                    .priceRange(suggestion.getPriceRange())
                    .productIds(suggestion.getProductIds())
                    .targetAudience(suggestion.getTargetAudience())
                    .build();
        }
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DiscountInfoResponse {
        private String code;
        private int percentage;
        private double maxDiscount;
        private double minOrderValue;
        private LocalDate validFrom;
        private LocalDate validUntil;
        private String terms;
        private boolean isActive;
        
        public static DiscountInfoResponse from(FestivalCollection.DiscountInfo info) {
            LocalDate now = LocalDate.now();
            return DiscountInfoResponse.builder()
                    .code(info.getCode())
                    .percentage(info.getPercentage())
                    .maxDiscount(info.getMaxDiscount())
                    .minOrderValue(info.getMinOrderValue())
                    .validFrom(info.getValidFrom())
                    .validUntil(info.getValidUntil())
                    .terms(info.getTerms())
                    .isActive((info.getValidFrom() == null || !now.isBefore(info.getValidFrom())) &&
                             (info.getValidUntil() == null || !now.isAfter(info.getValidUntil())))
                    .build();
        }
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateFestivalRequest {
        private String slug;
        private String name;
        private String nameHindi;
        private String description;
        private String descriptionHindi;
        private String tagline;
        private String type;
        private String category;
        private LocalDate startDate;
        private LocalDate endDate;
        private LocalDate festivalDate;
        private String heroImageUrl;
        private String bannerImageUrl;
        private String thumbnailUrl;
        private String mobileHeroUrl;
        private String videoUrl;
        private String themeColor;
        private String accentColor;
        private List<String> primaryStates;
        private List<String> secondaryStates;
        private String regionalRelevance;
        private boolean hasCountdownTimer;
        private boolean hasEarlyBirdOffers;
        private boolean active;
        private boolean featured;
        private int displayOrder;
        private int year;
        private String seoTitle;
        private String seoDescription;
        private List<String> seoKeywords;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FestivalListResponse {
        private List<FestivalCollectionResponse> festivals;
        private int total;
        private int activeCount;
        private int upcomingCount;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpcomingFestivalResponse {
        private String id;
        private String slug;
        private String name;
        private String thumbnailUrl;
        private String themeColor;
        private LocalDate festivalDate;
        private long daysUntil;
        private boolean hasCollection;
    }
}
