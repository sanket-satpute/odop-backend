package com.odop.root.odopfeatures.districtmap.dto;

import com.odop.root.odopfeatures.districtmap.model.DistrictInfo;
import lombok.*;

import java.util.List;

/**
 * DTOs for District Map Browse Feature
 */
public class DistrictMapDto {
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DistrictResponse {
        private String id;
        private String districtCode;
        private String name;
        private String nameHindi;
        private String stateCode;
        private String stateName;
        private String stateNameHindi;
        private String region;
        private String regionDisplayName;
        private double latitude;
        private double longitude;
        private String primaryCraft;
        private List<OdopProductResponse> odopProducts;
        private List<CraftTraditionResponse> craftTraditions;
        private List<String> famousFor;
        private String historicalSignificance;
        private String heroImageUrl;
        private String thumbnailUrl;
        private String bannerImageUrl;
        private List<String> galleryImages;
        private String videoUrl;
        private long registeredArtisans;
        private long activeVendors;
        private long totalProducts;
        private List<FamousArtisanResponse> famousArtisans;
        private boolean featured;
        private String mapTileColor;
        
        public static DistrictResponse from(DistrictInfo district) {
            return DistrictResponse.builder()
                    .id(district.getId())
                    .districtCode(district.getDistrictCode())
                    .name(district.getName())
                    .nameHindi(district.getNameHindi())
                    .stateCode(district.getStateCode())
                    .stateName(district.getStateName())
                    .stateNameHindi(district.getStateNameHindi())
                    .region(district.getRegion() != null ? district.getRegion().name() : null)
                    .regionDisplayName(district.getRegion() != null ? district.getRegion().getDisplayName() : null)
                    .latitude(district.getLatitude())
                    .longitude(district.getLongitude())
                    .primaryCraft(district.getPrimaryCraft())
                    .odopProducts(district.getOdopProducts() != null ?
                            district.getOdopProducts().stream()
                                    .map(OdopProductResponse::from)
                                    .toList() : null)
                    .craftTraditions(district.getCraftTraditions() != null ?
                            district.getCraftTraditions().stream()
                                    .map(CraftTraditionResponse::from)
                                    .toList() : null)
                    .famousFor(district.getFamousFor())
                    .historicalSignificance(district.getHistoricalSignificance())
                    .heroImageUrl(district.getHeroImageUrl())
                    .thumbnailUrl(district.getThumbnailUrl())
                    .bannerImageUrl(district.getBannerImageUrl())
                    .galleryImages(district.getGalleryImages())
                    .videoUrl(district.getVideoUrl())
                    .registeredArtisans(district.getRegisteredArtisans())
                    .activeVendors(district.getActiveVendors())
                    .totalProducts(district.getTotalProducts())
                    .famousArtisans(district.getFamousArtisans() != null ?
                            district.getFamousArtisans().stream()
                                    .map(FamousArtisanResponse::from)
                                    .toList() : null)
                    .featured(district.isFeatured())
                    .mapTileColor(district.getMapTileColor())
                    .build();
        }
        
        public static DistrictResponse summary(DistrictInfo district) {
            return DistrictResponse.builder()
                    .id(district.getId())
                    .districtCode(district.getDistrictCode())
                    .name(district.getName())
                    .nameHindi(district.getNameHindi())
                    .stateCode(district.getStateCode())
                    .stateName(district.getStateName())
                    .latitude(district.getLatitude())
                    .longitude(district.getLongitude())
                    .primaryCraft(district.getPrimaryCraft())
                    .thumbnailUrl(district.getThumbnailUrl())
                    .totalProducts(district.getTotalProducts())
                    .registeredArtisans(district.getRegisteredArtisans())
                    .mapTileColor(district.getMapTileColor())
                    .featured(district.isFeatured())
                    .build();
        }
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OdopProductResponse {
        private String name;
        private String nameHindi;
        private String description;
        private String imageUrl;
        private boolean giTagged;
        private String giTagNumber;
        private int yearRecognized;
        
        public static OdopProductResponse from(DistrictInfo.OdopProduct product) {
            return OdopProductResponse.builder()
                    .name(product.getName())
                    .nameHindi(product.getNameHindi())
                    .description(product.getDescription())
                    .imageUrl(product.getImageUrl())
                    .giTagged(product.isGiTagged())
                    .giTagNumber(product.getGiTagNumber())
                    .yearRecognized(product.getYearRecognized())
                    .build();
        }
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CraftTraditionResponse {
        private String name;
        private String nameHindi;
        private String description;
        private String history;
        private List<String> materials;
        private List<String> techniques;
        private String imageUrl;
        private int ageInYears;
        private String unescoStatus;
        
        public static CraftTraditionResponse from(DistrictInfo.CraftTradition tradition) {
            return CraftTraditionResponse.builder()
                    .name(tradition.getName())
                    .nameHindi(tradition.getNameHindi())
                    .description(tradition.getDescription())
                    .history(tradition.getHistory())
                    .materials(tradition.getMaterials())
                    .techniques(tradition.getTechniques())
                    .imageUrl(tradition.getImageUrl())
                    .ageInYears(tradition.getAgeInYears())
                    .unescoStatus(tradition.getUnescoStatus())
                    .build();
        }
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FamousArtisanResponse {
        private String name;
        private String vendorId;
        private String craft;
        private String achievement;
        private String imageUrl;
        private String storyUrl;
        
        public static FamousArtisanResponse from(DistrictInfo.FamousArtisan artisan) {
            return FamousArtisanResponse.builder()
                    .name(artisan.getName())
                    .vendorId(artisan.getVendorId())
                    .craft(artisan.getCraft())
                    .achievement(artisan.getAchievement())
                    .imageUrl(artisan.getImageUrl())
                    .storyUrl(artisan.getStoryUrl())
                    .build();
        }
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StateResponse {
        private String stateCode;
        private String name;
        private String nameHindi;
        private String region;
        private String regionDisplayName;
        private String mapColor;
        private int districtCount;
        private long totalProducts;
        private long totalArtisans;
        private List<String> topCrafts;
        private List<DistrictResponse> featuredDistricts;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RegionResponse {
        private String region;
        private String displayName;
        private String displayNameHindi;
        private List<StateResponse> states;
        private int totalDistricts;
        private long totalProducts;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MapDataResponse {
        private List<StateResponse> states;
        private List<DistrictResponse> featuredDistricts;
        private MapStatistics statistics;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MapStatistics {
        private int totalStates;
        private int totalDistricts;
        private long totalProducts;
        private long totalArtisans;
        private int giTaggedProducts;
        private List<TopCraftResponse> topCrafts;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TopCraftResponse {
        private String craftName;
        private int districtCount;
        private long productCount;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateDistrictRequest {
        private String districtCode;
        private String name;
        private String nameHindi;
        private String nameLocal;
        private String stateCode;
        private String stateName;
        private String stateNameHindi;
        private String region;
        private String division;
        private double latitude;
        private double longitude;
        private String primaryCraft;
        private List<String> famousFor;
        private String historicalSignificance;
        private String craftHistory;
        private String heroImageUrl;
        private String thumbnailUrl;
        private String bannerImageUrl;
        private String videoUrl;
        private String mapTileColor;
        private boolean active;
        private boolean featured;
        private int displayPriority;
        private String seoTitle;
        private String seoDescription;
        private List<String> seoKeywords;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DistrictSearchResponse {
        private List<DistrictResponse> districts;
        private int total;
        private String query;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MapMarker {
        private String id;
        private String name;
        private double latitude;
        private double longitude;
        private String primaryCraft;
        private String thumbnailUrl;
        private String color;
        private long productCount;
        private MarkerType type;
    }
    
    public enum MarkerType {
        DISTRICT,
        ARTISAN_CLUSTER,
        CRAFT_CENTER,
        GI_TAG_LOCATION
    }
}
