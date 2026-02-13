package com.odop.root.odopfeatures.govschemes.dto;

import com.odop.root.odopfeatures.govschemes.model.GovernmentScheme.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

/**
 * DTOs for Government Scheme API responses and requests
 */
public class GovernmentSchemeDto {
    
    // ==================== Response DTOs ====================
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SchemeResponse {
        private String id;
        private String slug;
        private String name;
        private String nameHindi;
        private String shortName;
        private String description;
        private String descriptionHindi;
        private String tagline;
        
        private String type;
        private String typeDisplayName;
        private String category;
        private List<BeneficiaryDto> targetBeneficiaries;
        private String level;
        
        private String ministry;
        private String ministryHindi;
        private String implementingAgency;
        private String contactEmail;
        private String contactPhone;
        private String helplineNumber;
        
        private List<BenefitDto> benefits;
        private String maxFundingAmount;
        private String subsidyPercentage;
        private String interestRate;
        private String collateralRequirement;
        
        private String eligibilitySummary;
        private String eligibilitySummaryHindi;
        private List<EligibilityDto> eligibilityCriteria;
        private List<String> requiredDocuments;
        
        private String applicationProcess;
        private String applicationProcessHindi;
        private List<ApplicationStepDto> applicationSteps;
        private String applicationUrl;
        private String applicationFormUrl;
        private String applicationMode;
        private boolean onlineApplicationAvailable;
        
        private LocalDate launchDate;
        private LocalDate lastDateToApply;
        private String processingTime;
        private boolean openForApplications;
        private boolean applicationsClosed;
        private int daysUntilDeadline;
        
        private List<String> applicableStates;
        private boolean panIndiaScheme;
        
        private String logoUrl;
        private String bannerUrl;
        private String thumbnailUrl;
        private String pdfBrochureUrl;
        
        private List<SuccessStoryDto> successStories;
        private List<FaqDto> faqs;
        
        private boolean featured;
        private long viewCount;
        
        private String seoTitle;
        private String seoDescription;
        private List<String> seoKeywords;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SchemeListItem {
        private String id;
        private String slug;
        private String name;
        private String nameHindi;
        private String shortName;
        private String description;
        private String tagline;
        
        private String type;
        private String typeDisplayName;
        private String category;
        private List<String> targetBeneficiaryNames;
        
        private String ministry;
        private String maxFundingAmount;
        private boolean collateralFree;
        
        private String applicationUrl;
        private boolean onlineApplicationAvailable;
        private boolean openForApplications;
        
        private String logoUrl;
        private String thumbnailUrl;
        
        private boolean featured;
        private boolean panIndiaScheme;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BenefitDto {
        private String title;
        private String titleHindi;
        private String description;
        private String descriptionHindi;
        private String iconName;
        private String amount;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BeneficiaryDto {
        private String code;
        private String name;
        private String nameHindi;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EligibilityDto {
        private String criterion;
        private String criterionHindi;
        private boolean mandatory;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ApplicationStepDto {
        private int stepNumber;
        private String title;
        private String titleHindi;
        private String description;
        private String descriptionHindi;
        private String actionUrl;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SuccessStoryDto {
        private String beneficiaryName;
        private String location;
        private String craft;
        private String story;
        private String imageUrl;
        private String amountReceived;
        private int year;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FaqDto {
        private String question;
        private String questionHindi;
        private String answer;
        private String answerHindi;
    }
    
    // ==================== Summary & Statistics ====================
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SchemesOverview {
        private int totalSchemes;
        private int activeSchemes;
        private int centralSchemes;
        private int stateSchemes;
        private int loanSchemes;
        private int grantSchemes;
        private int trainingSchemes;
        private List<SchemeListItem> featuredSchemes;
        private List<CategoryCount> schemesByCategory;
        private List<TypeCount> schemesByType;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CategoryCount {
        private String category;
        private String displayName;
        private long count;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TypeCount {
        private String type;
        private String displayName;
        private long count;
    }
    
    // ==================== Finder/Filter Response ====================
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SchemeFinderResponse {
        private List<SchemeListItem> recommendedSchemes;
        private List<SchemeListItem> otherSchemes;
        private int totalMatching;
        private String searchSummary;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SchemeFilters {
        private List<FilterOption> types;
        private List<FilterOption> categories;
        private List<FilterOption> beneficiaries;
        private List<FilterOption> levels;
        private List<FilterOption> states;
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
    
    // ==================== Request DTOs ====================
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateSchemeRequest {
        private String slug;
        private String name;
        private String nameHindi;
        private String shortName;
        private String description;
        private String descriptionHindi;
        private String tagline;
        
        private SchemeType type;
        private SchemeCategory category;
        private List<TargetBeneficiary> targetBeneficiaries;
        private GovernmentLevel level;
        
        private String ministry;
        private String ministryHindi;
        private String implementingAgency;
        private String implementingAgencyWebsite;
        private String contactEmail;
        private String contactPhone;
        private String helplineNumber;
        
        private List<SchemeBenefit> benefits;
        private String maxFundingAmount;
        private String subsidyPercentage;
        private String interestRate;
        private String collateralRequirement;
        
        private List<EligibilityCriterion> eligibilityCriteria;
        private String eligibilitySummary;
        private String eligibilitySummaryHindi;
        private List<String> requiredDocuments;
        
        private String applicationProcess;
        private String applicationProcessHindi;
        private List<ApplicationStep> applicationSteps;
        private String applicationUrl;
        private String applicationFormUrl;
        private ApplicationMode applicationMode;
        private boolean onlineApplicationAvailable;
        
        private LocalDate launchDate;
        private LocalDate lastDateToApply;
        private String processingTime;
        
        private List<String> applicableStates;
        private boolean panIndiaScheme;
        
        private String logoUrl;
        private String bannerUrl;
        private String pdfBrochureUrl;
        
        private List<String> relatedSchemeIds;
        private List<String> craftCategoryIds;
        private List<String> tags;
        
        private boolean active;
        private boolean featured;
        private int displayOrder;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateSchemeRequest {
        private String name;
        private String nameHindi;
        private String shortName;
        private String description;
        private String descriptionHindi;
        private String tagline;
        
        private SchemeType type;
        private SchemeCategory category;
        private List<TargetBeneficiary> targetBeneficiaries;
        private GovernmentLevel level;
        
        private String ministry;
        private String ministryHindi;
        private String implementingAgency;
        private String implementingAgencyWebsite;
        private String contactEmail;
        private String contactPhone;
        private String helplineNumber;
        
        private List<SchemeBenefit> benefits;
        private String maxFundingAmount;
        private String subsidyPercentage;
        private String interestRate;
        private String collateralRequirement;
        
        private List<EligibilityCriterion> eligibilityCriteria;
        private String eligibilitySummary;
        private String eligibilitySummaryHindi;
        private List<String> requiredDocuments;
        
        private String applicationProcess;
        private String applicationProcessHindi;
        private List<ApplicationStep> applicationSteps;
        private String applicationUrl;
        private String applicationFormUrl;
        private ApplicationMode applicationMode;
        private boolean onlineApplicationAvailable;
        
        private LocalDate launchDate;
        private LocalDate lastDateToApply;
        private String processingTime;
        private boolean openForApplications;
        
        private List<String> applicableStates;
        private boolean panIndiaScheme;
        
        private String logoUrl;
        private String bannerUrl;
        private String pdfBrochureUrl;
        
        private List<String> relatedSchemeIds;
        private List<String> craftCategoryIds;
        private List<String> tags;
        
        private boolean active;
        private boolean featured;
        private int displayOrder;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SchemeFinderRequest {
        private List<TargetBeneficiary> beneficiaryTypes;
        private String craft;
        private String state;
        private SchemeType schemeType;
        private boolean onlineApplicationPreferred;
        private boolean collateralFree;
        private String fundingRequirement;       // LOW, MEDIUM, HIGH
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AddSuccessStoryRequest {
        private String beneficiaryName;
        private String location;
        private String craft;
        private String story;
        private String imageUrl;
        private String amountReceived;
        private int year;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AddFaqRequest {
        private String question;
        private String questionHindi;
        private String answer;
        private String answerHindi;
    }
}
