package com.odop.root.odopfeatures.crafts.dto;

import com.odop.root.odopfeatures.crafts.model.CraftCategory;
import lombok.*;

import java.util.List;

/**
 * DTOs for Craft Categories
 */
public class CraftCategoryDto {
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CraftCategoryResponse {
        private String id;
        private String slug;
        private String name;
        private String nameHindi;
        private String description;
        private String parentId;
        private int level;
        private String ancestorPath;
        private String imageUrl;
        private String iconName;
        private String bannerImageUrl;
        private String themeColor;
        private List<CategoryAttributeDto> attributes;
        private List<String> relatedGiTags;
        private List<String> majorStates;
        private long productCount;
        private long artisanCount;
        private boolean featured;
        private int displayOrder;
        private List<CraftCategoryResponse> children;
        
        public static CraftCategoryResponse from(CraftCategory category) {
            return CraftCategoryResponse.builder()
                    .id(category.getId())
                    .slug(category.getSlug())
                    .name(category.getName())
                    .nameHindi(category.getNameHindi())
                    .description(category.getDescription())
                    .parentId(category.getParentId())
                    .level(category.getLevel())
                    .ancestorPath(category.getAncestorPath())
                    .imageUrl(category.getImageUrl())
                    .iconName(category.getIconName())
                    .bannerImageUrl(category.getBannerImageUrl())
                    .themeColor(category.getThemeColor())
                    .attributes(category.getAttributes() != null ?
                            category.getAttributes().stream()
                                    .map(CategoryAttributeDto::from)
                                    .toList() : null)
                    .relatedGiTags(category.getRelatedGiTags())
                    .majorStates(category.getMajorStates())
                    .productCount(category.getProductCount())
                    .artisanCount(category.getArtisanCount())
                    .featured(category.isFeatured())
                    .displayOrder(category.getDisplayOrder())
                    .build();
        }
        
        public static CraftCategoryResponse simpleFrom(CraftCategory category) {
            return CraftCategoryResponse.builder()
                    .id(category.getId())
                    .slug(category.getSlug())
                    .name(category.getName())
                    .nameHindi(category.getNameHindi())
                    .iconName(category.getIconName())
                    .imageUrl(category.getImageUrl())
                    .themeColor(category.getThemeColor())
                    .productCount(category.getProductCount())
                    .level(category.getLevel())
                    .displayOrder(category.getDisplayOrder())
                    .build();
        }
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CategoryAttributeDto {
        private String name;
        private String label;
        private String labelHindi;
        private String type;
        private List<String> options;
        private String unit;
        private boolean required;
        private boolean filterable;
        
        public static CategoryAttributeDto from(CraftCategory.CategoryAttribute attr) {
            return CategoryAttributeDto.builder()
                    .name(attr.getName())
                    .label(attr.getLabel())
                    .labelHindi(attr.getLabelHindi())
                    .type(attr.getType().name())
                    .options(attr.getOptions())
                    .unit(attr.getUnit())
                    .required(attr.isRequired())
                    .filterable(attr.isFilterable())
                    .build();
        }
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateCraftCategoryRequest {
        private String slug;
        private String name;
        private String nameHindi;
        private String description;
        private String descriptionHindi;
        private String parentId;
        private String imageUrl;
        private String iconName;
        private String bannerImageUrl;
        private String themeColor;
        private List<String> relatedGiTags;
        private List<String> majorStates;
        private List<String> famousDistricts;
        private boolean active;
        private boolean featured;
        private int displayOrder;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CategoryTreeResponse {
        private List<CraftCategoryResponse> categories;
        private int totalCategories;
        private int mainCategories;
        private int subCategories;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CategoryFilterResponse {
        private String categoryId;
        private String categoryName;
        private List<FilterOption> filters;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FilterOption {
        private String name;
        private String label;
        private String type;
        private List<FilterValue> values;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FilterValue {
        private String value;
        private String label;
        private long count;
    }
}
