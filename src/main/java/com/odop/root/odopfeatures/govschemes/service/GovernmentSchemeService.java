package com.odop.root.odopfeatures.govschemes.service;

import com.odop.root.odopfeatures.govschemes.dto.GovernmentSchemeDto.*;
import com.odop.root.odopfeatures.govschemes.model.GovernmentScheme;
import com.odop.root.odopfeatures.govschemes.model.GovernmentScheme.*;
import com.odop.root.odopfeatures.govschemes.repository.GovernmentSchemeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service layer for Government Scheme operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GovernmentSchemeService {
    
    private final GovernmentSchemeRepository schemeRepository;
    
    // ==================== Initialization ====================
    
    @EventListener(ApplicationReadyEvent.class)
    public void initializeDefaultSchemes() {
        if (schemeRepository.count() == 0) {
            log.info("Initializing default government schemes...");
            List<GovernmentScheme> defaultSchemes = GovernmentScheme.getDefaultSchemes();
            LocalDateTime now = LocalDateTime.now();
            
            for (GovernmentScheme scheme : defaultSchemes) {
                scheme.setCreatedAt(now);
                scheme.setUpdatedAt(now);
                scheme.setLastVerified(now);
            }
            
            schemeRepository.saveAll(defaultSchemes);
            log.info("Initialized {} default government schemes", defaultSchemes.size());
        }
    }
    
    // ==================== Public Query Methods ====================
    
    public List<SchemeListItem> getAllSchemes() {
        return schemeRepository.findByActiveTrueOrderByDisplayOrderAsc()
                .stream()
                .map(this::toListItem)
                .collect(Collectors.toList());
    }
    
    public List<SchemeListItem> getFeaturedSchemes() {
        return schemeRepository.findByActiveTrueAndFeaturedTrueOrderByDisplayOrderAsc()
                .stream()
                .map(this::toListItem)
                .collect(Collectors.toList());
    }
    
    public SchemeResponse getSchemeById(String id) {
        GovernmentScheme scheme = schemeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Scheme not found: " + id));
        
        // Increment view count
        scheme.setViewCount(scheme.getViewCount() + 1);
        schemeRepository.save(scheme);
        
        return toResponse(scheme);
    }
    
    public SchemeResponse getSchemeBySlug(String slug) {
        GovernmentScheme scheme = schemeRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Scheme not found: " + slug));
        
        // Increment view count
        scheme.setViewCount(scheme.getViewCount() + 1);
        schemeRepository.save(scheme);
        
        return toResponse(scheme);
    }
    
    // ==================== Filtered Queries ====================
    
    public List<SchemeListItem> getSchemesByType(SchemeType type) {
        return schemeRepository.findByActiveTrueAndTypeOrderByDisplayOrderAsc(type)
                .stream()
                .map(this::toListItem)
                .collect(Collectors.toList());
    }
    
    public List<SchemeListItem> getSchemesByCategory(SchemeCategory category) {
        return schemeRepository.findByActiveTrueAndCategoryOrderByDisplayOrderAsc(category)
                .stream()
                .map(this::toListItem)
                .collect(Collectors.toList());
    }
    
    public List<SchemeListItem> getSchemesByBeneficiary(TargetBeneficiary beneficiary) {
        return schemeRepository.findByTargetBeneficiary(beneficiary)
                .stream()
                .map(this::toListItem)
                .collect(Collectors.toList());
    }
    
    public List<SchemeListItem> getSchemesByLevel(GovernmentLevel level) {
        return schemeRepository.findByActiveTrueAndLevelOrderByDisplayOrderAsc(level)
                .stream()
                .map(this::toListItem)
                .collect(Collectors.toList());
    }
    
    public List<SchemeListItem> getSchemesForState(String stateCode) {
        return schemeRepository.findSchemesForState(stateCode)
                .stream()
                .map(this::toListItem)
                .collect(Collectors.toList());
    }
    
    public List<SchemeListItem> getOpenSchemes() {
        return schemeRepository.findByActiveTrueAndOpenForApplicationsTrue()
                .stream()
                .map(this::toListItem)
                .collect(Collectors.toList());
    }
    
    public List<SchemeListItem> getOnlineSchemes() {
        return schemeRepository.findByActiveTrueAndOnlineApplicationAvailableTrue()
                .stream()
                .map(this::toListItem)
                .collect(Collectors.toList());
    }
    
    public List<SchemeListItem> getCollateralFreeSchemes() {
        return schemeRepository.findCollateralFreeSchemes()
                .stream()
                .map(this::toListItem)
                .collect(Collectors.toList());
    }
    
    public List<SchemeListItem> getLoanSchemes() {
        return schemeRepository.findLoanSchemes()
                .stream()
                .map(this::toListItem)
                .collect(Collectors.toList());
    }
    
    public List<SchemeListItem> getGrantSchemes() {
        return schemeRepository.findGrantAndSubsidySchemes()
                .stream()
                .map(this::toListItem)
                .collect(Collectors.toList());
    }
    
    // ==================== Search ====================
    
    public List<SchemeListItem> searchSchemes(String query) {
        return schemeRepository.searchSchemes(query)
                .stream()
                .map(this::toListItem)
                .collect(Collectors.toList());
    }
    
    // ==================== Scheme Finder ====================
    
    public SchemeFinderResponse findSchemes(SchemeFinderRequest request) {
        List<GovernmentScheme> allSchemes = schemeRepository.findByActiveTrue();
        List<GovernmentScheme> matching = new ArrayList<>();
        
        for (GovernmentScheme scheme : allSchemes) {
            int score = calculateMatchScore(scheme, request);
            if (score > 0) {
                matching.add(scheme);
            }
        }
        
        // Sort by relevance (you could calculate and store scores)
        matching.sort((a, b) -> {
            int scoreA = calculateMatchScore(a, request);
            int scoreB = calculateMatchScore(b, request);
            return Integer.compare(scoreB, scoreA);
        });
        
        // Split into recommended (high score) and others
        List<SchemeListItem> recommended = new ArrayList<>();
        List<SchemeListItem> others = new ArrayList<>();
        
        for (GovernmentScheme scheme : matching) {
            int score = calculateMatchScore(scheme, request);
            if (score >= 3) {
                recommended.add(toListItem(scheme));
            } else {
                others.add(toListItem(scheme));
            }
        }
        
        return SchemeFinderResponse.builder()
                .recommendedSchemes(recommended)
                .otherSchemes(others)
                .totalMatching(matching.size())
                .searchSummary(generateSearchSummary(request, matching.size()))
                .build();
    }
    
    private int calculateMatchScore(GovernmentScheme scheme, SchemeFinderRequest request) {
        int score = 0;
        
        // Beneficiary match
        if (request.getBeneficiaryTypes() != null && !request.getBeneficiaryTypes().isEmpty()) {
            for (TargetBeneficiary target : request.getBeneficiaryTypes()) {
                if (scheme.getTargetBeneficiaries() != null && 
                    scheme.getTargetBeneficiaries().contains(target)) {
                    score += 2;
                }
            }
        }
        
        // Scheme type match
        if (request.getSchemeType() != null && scheme.getType() == request.getSchemeType()) {
            score += 2;
        }
        
        // State match
        if (request.getState() != null && !request.getState().isEmpty()) {
            if (scheme.isPanIndiaScheme() || 
                (scheme.getApplicableStates() != null && 
                 scheme.getApplicableStates().contains(request.getState()))) {
                score += 1;
            }
        }
        
        // Online application preference
        if (request.isOnlineApplicationPreferred() && scheme.isOnlineApplicationAvailable()) {
            score += 1;
        }
        
        // Collateral-free preference
        if (request.isCollateralFree() && 
            scheme.getCollateralRequirement() != null && 
            scheme.getCollateralRequirement().toLowerCase().contains("no collateral")) {
            score += 2;
        }
        
        // Must be open for applications
        if (scheme.isOpenForApplications()) {
            score += 1;
        }
        
        return score;
    }
    
    private String generateSearchSummary(SchemeFinderRequest request, int count) {
        StringBuilder sb = new StringBuilder();
        sb.append("Found ").append(count).append(" schemes");
        
        if (request.getBeneficiaryTypes() != null && !request.getBeneficiaryTypes().isEmpty()) {
            sb.append(" for ");
            sb.append(request.getBeneficiaryTypes().stream()
                    .map(TargetBeneficiary::getDisplayName)
                    .collect(Collectors.joining(", ")));
        }
        
        if (request.getState() != null && !request.getState().isEmpty()) {
            sb.append(" in ").append(request.getState());
        }
        
        return sb.toString();
    }
    
    // ==================== Overview & Statistics ====================
    
    public SchemesOverview getOverview() {
        long totalActive = schemeRepository.countByActiveTrue();
        long centralCount = schemeRepository.countByActiveTrueAndLevel(GovernmentLevel.CENTRAL);
        long stateCount = schemeRepository.countByActiveTrueAndLevel(GovernmentLevel.STATE);
        long loanCount = schemeRepository.countByActiveTrueAndType(SchemeType.LOAN);
        long grantCount = schemeRepository.countByActiveTrueAndType(SchemeType.GRANT) +
                          schemeRepository.countByActiveTrueAndType(SchemeType.SUBSIDY);
        long trainingCount = schemeRepository.countByActiveTrueAndType(SchemeType.TRAINING);
        
        List<CategoryCount> categoryStats = schemeRepository.getSchemeCountByCategory()
                .stream()
                .map(r -> CategoryCount.builder()
                        .category(r.get_id() != null ? r.get_id().name() : "UNKNOWN")
                        .displayName(r.get_id() != null ? r.get_id().name() : "Unknown")
                        .count(r.getCount())
                        .build())
                .collect(Collectors.toList());
        
        List<TypeCount> typeStats = schemeRepository.getSchemeCountByType()
                .stream()
                .map(r -> TypeCount.builder()
                        .type(r.get_id() != null ? r.get_id().name() : "UNKNOWN")
                        .displayName(r.get_id() != null ? r.get_id().getDisplayName() : "Unknown")
                        .count(r.getCount())
                        .build())
                .collect(Collectors.toList());
        
        return SchemesOverview.builder()
                .totalSchemes((int) totalActive)
                .activeSchemes((int) totalActive)
                .centralSchemes((int) centralCount)
                .stateSchemes((int) stateCount)
                .loanSchemes((int) loanCount)
                .grantSchemes((int) grantCount)
                .trainingSchemes((int) trainingCount)
                .featuredSchemes(getFeaturedSchemes())
                .schemesByCategory(categoryStats)
                .schemesByType(typeStats)
                .build();
    }
    
    public SchemeFilters getFilters() {
        List<FilterOption> types = Arrays.stream(SchemeType.values())
                .map(t -> FilterOption.builder()
                        .value(t.name())
                        .label(t.getDisplayName())
                        .labelHindi(t.getDisplayNameHindi())
                        .count(schemeRepository.countByActiveTrueAndType(t))
                        .build())
                .filter(f -> f.getCount() > 0)
                .collect(Collectors.toList());
        
        List<FilterOption> categories = Arrays.stream(SchemeCategory.values())
                .map(c -> FilterOption.builder()
                        .value(c.name())
                        .label(c.name().replace("_", " "))
                        .count(schemeRepository.countByActiveTrueAndCategory(c))
                        .build())
                .filter(f -> f.getCount() > 0)
                .collect(Collectors.toList());
        
        List<FilterOption> beneficiaries = Arrays.stream(TargetBeneficiary.values())
                .map(b -> FilterOption.builder()
                        .value(b.name())
                        .label(b.getDisplayName())
                        .labelHindi(b.getDisplayNameHindi())
                        .build())
                .collect(Collectors.toList());
        
        List<FilterOption> levels = Arrays.stream(GovernmentLevel.values())
                .map(l -> FilterOption.builder()
                        .value(l.name())
                        .label(l.name())
                        .count(schemeRepository.countByActiveTrueAndLevel(l))
                        .build())
                .filter(f -> f.getCount() > 0)
                .collect(Collectors.toList());
        
        return SchemeFilters.builder()
                .types(types)
                .categories(categories)
                .beneficiaries(beneficiaries)
                .levels(levels)
                .states(new ArrayList<>()) // Can be populated with Indian states
                .build();
    }
    
    // ==================== Track Clicks ====================
    
    public void trackApplicationClick(String schemeId) {
        schemeRepository.findById(schemeId).ifPresent(scheme -> {
            scheme.setApplicationClicks(scheme.getApplicationClicks() + 1);
            schemeRepository.save(scheme);
        });
    }
    
    // ==================== Admin Methods ====================
    
    @Transactional
    public SchemeResponse createScheme(CreateSchemeRequest request) {
        if (schemeRepository.findBySlug(request.getSlug()).isPresent()) {
            throw new RuntimeException("Scheme with slug already exists: " + request.getSlug());
        }
        
        GovernmentScheme scheme = mapCreateRequestToScheme(request);
        scheme.setCreatedAt(LocalDateTime.now());
        scheme.setUpdatedAt(LocalDateTime.now());
        scheme.setOpenForApplications(true);
        
        GovernmentScheme saved = schemeRepository.save(scheme);
        log.info("Created new government scheme: {}", saved.getName());
        
        return toResponse(saved);
    }
    
    @Transactional
    public SchemeResponse updateScheme(String id, UpdateSchemeRequest request) {
        GovernmentScheme scheme = schemeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Scheme not found: " + id));
        
        updateSchemeFromRequest(scheme, request);
        scheme.setUpdatedAt(LocalDateTime.now());
        
        GovernmentScheme saved = schemeRepository.save(scheme);
        log.info("Updated government scheme: {}", saved.getName());
        
        return toResponse(saved);
    }
    
    @Transactional
    public void deleteScheme(String id) {
        GovernmentScheme scheme = schemeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Scheme not found: " + id));
        
        schemeRepository.delete(scheme);
        log.info("Deleted government scheme: {}", scheme.getName());
    }
    
    @Transactional
    public SchemeResponse toggleSchemeActive(String id) {
        GovernmentScheme scheme = schemeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Scheme not found: " + id));
        
        scheme.setActive(!scheme.isActive());
        scheme.setUpdatedAt(LocalDateTime.now());
        
        GovernmentScheme saved = schemeRepository.save(scheme);
        log.info("Toggled scheme active status: {} - {}", saved.getName(), saved.isActive());
        
        return toResponse(saved);
    }
    
    @Transactional
    public SchemeResponse addSuccessStory(String id, AddSuccessStoryRequest request) {
        GovernmentScheme scheme = schemeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Scheme not found: " + id));
        
        if (scheme.getSuccessStories() == null) {
            scheme.setSuccessStories(new ArrayList<>());
        }
        
        scheme.getSuccessStories().add(SuccessStory.builder()
                .beneficiaryName(request.getBeneficiaryName())
                .location(request.getLocation())
                .craft(request.getCraft())
                .story(request.getStory())
                .imageUrl(request.getImageUrl())
                .amountReceived(request.getAmountReceived())
                .year(request.getYear())
                .build());
        
        scheme.setUpdatedAt(LocalDateTime.now());
        GovernmentScheme saved = schemeRepository.save(scheme);
        
        return toResponse(saved);
    }
    
    @Transactional
    public SchemeResponse addFaq(String id, AddFaqRequest request) {
        GovernmentScheme scheme = schemeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Scheme not found: " + id));
        
        if (scheme.getFaqs() == null) {
            scheme.setFaqs(new ArrayList<>());
        }
        
        scheme.getFaqs().add(FaqItem.builder()
                .question(request.getQuestion())
                .questionHindi(request.getQuestionHindi())
                .answer(request.getAnswer())
                .answerHindi(request.getAnswerHindi())
                .build());
        
        scheme.setUpdatedAt(LocalDateTime.now());
        GovernmentScheme saved = schemeRepository.save(scheme);
        
        return toResponse(saved);
    }
    
    // ==================== Mapping Methods ====================
    
    private SchemeResponse toResponse(GovernmentScheme scheme) {
        int daysUntil = 0;
        boolean isClosed = false;
        
        if (scheme.getLastDateToApply() != null) {
            daysUntil = (int) ChronoUnit.DAYS.between(LocalDate.now(), scheme.getLastDateToApply());
            isClosed = daysUntil < 0;
        }
        
        return SchemeResponse.builder()
                .id(scheme.getId())
                .slug(scheme.getSlug())
                .name(scheme.getName())
                .nameHindi(scheme.getNameHindi())
                .shortName(scheme.getShortName())
                .description(scheme.getDescription())
                .descriptionHindi(scheme.getDescriptionHindi())
                .tagline(scheme.getTagline())
                .type(scheme.getType() != null ? scheme.getType().name() : null)
                .typeDisplayName(scheme.getType() != null ? scheme.getType().getDisplayName() : null)
                .category(scheme.getCategory() != null ? scheme.getCategory().name() : null)
                .targetBeneficiaries(mapBeneficiaries(scheme.getTargetBeneficiaries()))
                .level(scheme.getLevel() != null ? scheme.getLevel().name() : null)
                .ministry(scheme.getMinistry())
                .ministryHindi(scheme.getMinistryHindi())
                .implementingAgency(scheme.getImplementingAgency())
                .contactEmail(scheme.getContactEmail())
                .contactPhone(scheme.getContactPhone())
                .helplineNumber(scheme.getHelplineNumber())
                .benefits(mapBenefits(scheme.getBenefits()))
                .maxFundingAmount(scheme.getMaxFundingAmount())
                .subsidyPercentage(scheme.getSubsidyPercentage())
                .interestRate(scheme.getInterestRate())
                .collateralRequirement(scheme.getCollateralRequirement())
                .eligibilitySummary(scheme.getEligibilitySummary())
                .eligibilitySummaryHindi(scheme.getEligibilitySummaryHindi())
                .eligibilityCriteria(mapEligibility(scheme.getEligibilityCriteria()))
                .requiredDocuments(scheme.getRequiredDocuments())
                .applicationProcess(scheme.getApplicationProcess())
                .applicationProcessHindi(scheme.getApplicationProcessHindi())
                .applicationSteps(mapApplicationSteps(scheme.getApplicationSteps()))
                .applicationUrl(scheme.getApplicationUrl())
                .applicationFormUrl(scheme.getApplicationFormUrl())
                .applicationMode(scheme.getApplicationMode() != null ? scheme.getApplicationMode().name() : null)
                .onlineApplicationAvailable(scheme.isOnlineApplicationAvailable())
                .launchDate(scheme.getLaunchDate())
                .lastDateToApply(scheme.getLastDateToApply())
                .processingTime(scheme.getProcessingTime())
                .openForApplications(scheme.isOpenForApplications())
                .applicationsClosed(isClosed)
                .daysUntilDeadline(daysUntil)
                .applicableStates(scheme.getApplicableStates())
                .panIndiaScheme(scheme.isPanIndiaScheme())
                .logoUrl(scheme.getLogoUrl())
                .bannerUrl(scheme.getBannerUrl())
                .thumbnailUrl(scheme.getThumbnailUrl())
                .pdfBrochureUrl(scheme.getPdfBrochureUrl())
                .successStories(mapSuccessStories(scheme.getSuccessStories()))
                .faqs(mapFaqs(scheme.getFaqs()))
                .featured(scheme.isFeatured())
                .viewCount(scheme.getViewCount())
                .seoTitle(scheme.getSeoTitle())
                .seoDescription(scheme.getSeoDescription())
                .seoKeywords(scheme.getSeoKeywords())
                .build();
    }
    
    private SchemeListItem toListItem(GovernmentScheme scheme) {
        boolean isCollateralFree = scheme.getCollateralRequirement() != null &&
                scheme.getCollateralRequirement().toLowerCase().contains("no collateral");
        
        return SchemeListItem.builder()
                .id(scheme.getId())
                .slug(scheme.getSlug())
                .name(scheme.getName())
                .nameHindi(scheme.getNameHindi())
                .shortName(scheme.getShortName())
                .description(scheme.getDescription())
                .tagline(scheme.getTagline())
                .type(scheme.getType() != null ? scheme.getType().name() : null)
                .typeDisplayName(scheme.getType() != null ? scheme.getType().getDisplayName() : null)
                .category(scheme.getCategory() != null ? scheme.getCategory().name() : null)
                .targetBeneficiaryNames(scheme.getTargetBeneficiaries() != null ?
                        scheme.getTargetBeneficiaries().stream()
                                .map(TargetBeneficiary::getDisplayName)
                                .collect(Collectors.toList()) : null)
                .ministry(scheme.getMinistry())
                .maxFundingAmount(scheme.getMaxFundingAmount())
                .collateralFree(isCollateralFree)
                .applicationUrl(scheme.getApplicationUrl())
                .onlineApplicationAvailable(scheme.isOnlineApplicationAvailable())
                .openForApplications(scheme.isOpenForApplications())
                .logoUrl(scheme.getLogoUrl())
                .thumbnailUrl(scheme.getThumbnailUrl())
                .featured(scheme.isFeatured())
                .panIndiaScheme(scheme.isPanIndiaScheme())
                .build();
    }
    
    private List<BeneficiaryDto> mapBeneficiaries(List<TargetBeneficiary> beneficiaries) {
        if (beneficiaries == null) return null;
        return beneficiaries.stream()
                .map(b -> BeneficiaryDto.builder()
                        .code(b.name())
                        .name(b.getDisplayName())
                        .nameHindi(b.getDisplayNameHindi())
                        .build())
                .collect(Collectors.toList());
    }
    
    private List<BenefitDto> mapBenefits(List<SchemeBenefit> benefits) {
        if (benefits == null) return null;
        return benefits.stream()
                .map(b -> BenefitDto.builder()
                        .title(b.getTitle())
                        .titleHindi(b.getTitleHindi())
                        .description(b.getDescription())
                        .descriptionHindi(b.getDescriptionHindi())
                        .iconName(b.getIconName())
                        .amount(b.getAmount())
                        .build())
                .collect(Collectors.toList());
    }
    
    private List<EligibilityDto> mapEligibility(List<EligibilityCriterion> criteria) {
        if (criteria == null) return null;
        return criteria.stream()
                .map(c -> EligibilityDto.builder()
                        .criterion(c.getCriterion())
                        .criterionHindi(c.getCriterionHindi())
                        .mandatory(c.isMandatory())
                        .build())
                .collect(Collectors.toList());
    }
    
    private List<ApplicationStepDto> mapApplicationSteps(List<ApplicationStep> steps) {
        if (steps == null) return null;
        return steps.stream()
                .map(s -> ApplicationStepDto.builder()
                        .stepNumber(s.getStepNumber())
                        .title(s.getTitle())
                        .titleHindi(s.getTitleHindi())
                        .description(s.getDescription())
                        .descriptionHindi(s.getDescriptionHindi())
                        .actionUrl(s.getActionUrl())
                        .build())
                .collect(Collectors.toList());
    }
    
    private List<SuccessStoryDto> mapSuccessStories(List<SuccessStory> stories) {
        if (stories == null) return null;
        return stories.stream()
                .map(s -> SuccessStoryDto.builder()
                        .beneficiaryName(s.getBeneficiaryName())
                        .location(s.getLocation())
                        .craft(s.getCraft())
                        .story(s.getStory())
                        .imageUrl(s.getImageUrl())
                        .amountReceived(s.getAmountReceived())
                        .year(s.getYear())
                        .build())
                .collect(Collectors.toList());
    }
    
    private List<FaqDto> mapFaqs(List<FaqItem> faqs) {
        if (faqs == null) return null;
        return faqs.stream()
                .map(f -> FaqDto.builder()
                        .question(f.getQuestion())
                        .questionHindi(f.getQuestionHindi())
                        .answer(f.getAnswer())
                        .answerHindi(f.getAnswerHindi())
                        .build())
                .collect(Collectors.toList());
    }
    
    private GovernmentScheme mapCreateRequestToScheme(CreateSchemeRequest request) {
        return GovernmentScheme.builder()
                .slug(request.getSlug())
                .name(request.getName())
                .nameHindi(request.getNameHindi())
                .shortName(request.getShortName())
                .description(request.getDescription())
                .descriptionHindi(request.getDescriptionHindi())
                .tagline(request.getTagline())
                .type(request.getType())
                .category(request.getCategory())
                .targetBeneficiaries(request.getTargetBeneficiaries())
                .level(request.getLevel())
                .ministry(request.getMinistry())
                .ministryHindi(request.getMinistryHindi())
                .implementingAgency(request.getImplementingAgency())
                .implementingAgencyWebsite(request.getImplementingAgencyWebsite())
                .contactEmail(request.getContactEmail())
                .contactPhone(request.getContactPhone())
                .helplineNumber(request.getHelplineNumber())
                .benefits(request.getBenefits())
                .maxFundingAmount(request.getMaxFundingAmount())
                .subsidyPercentage(request.getSubsidyPercentage())
                .interestRate(request.getInterestRate())
                .collateralRequirement(request.getCollateralRequirement())
                .eligibilityCriteria(request.getEligibilityCriteria())
                .eligibilitySummary(request.getEligibilitySummary())
                .eligibilitySummaryHindi(request.getEligibilitySummaryHindi())
                .requiredDocuments(request.getRequiredDocuments())
                .applicationProcess(request.getApplicationProcess())
                .applicationProcessHindi(request.getApplicationProcessHindi())
                .applicationSteps(request.getApplicationSteps())
                .applicationUrl(request.getApplicationUrl())
                .applicationFormUrl(request.getApplicationFormUrl())
                .applicationMode(request.getApplicationMode())
                .onlineApplicationAvailable(request.isOnlineApplicationAvailable())
                .launchDate(request.getLaunchDate())
                .lastDateToApply(request.getLastDateToApply())
                .processingTime(request.getProcessingTime())
                .applicableStates(request.getApplicableStates())
                .panIndiaScheme(request.isPanIndiaScheme())
                .logoUrl(request.getLogoUrl())
                .bannerUrl(request.getBannerUrl())
                .pdfBrochureUrl(request.getPdfBrochureUrl())
                .relatedSchemeIds(request.getRelatedSchemeIds())
                .craftCategoryIds(request.getCraftCategoryIds())
                .tags(request.getTags())
                .active(request.isActive())
                .featured(request.isFeatured())
                .displayOrder(request.getDisplayOrder())
                .build();
    }
    
    private void updateSchemeFromRequest(GovernmentScheme scheme, UpdateSchemeRequest request) {
        if (request.getName() != null) scheme.setName(request.getName());
        if (request.getNameHindi() != null) scheme.setNameHindi(request.getNameHindi());
        if (request.getShortName() != null) scheme.setShortName(request.getShortName());
        if (request.getDescription() != null) scheme.setDescription(request.getDescription());
        if (request.getDescriptionHindi() != null) scheme.setDescriptionHindi(request.getDescriptionHindi());
        if (request.getTagline() != null) scheme.setTagline(request.getTagline());
        if (request.getType() != null) scheme.setType(request.getType());
        if (request.getCategory() != null) scheme.setCategory(request.getCategory());
        if (request.getTargetBeneficiaries() != null) scheme.setTargetBeneficiaries(request.getTargetBeneficiaries());
        if (request.getLevel() != null) scheme.setLevel(request.getLevel());
        if (request.getMinistry() != null) scheme.setMinistry(request.getMinistry());
        if (request.getMinistryHindi() != null) scheme.setMinistryHindi(request.getMinistryHindi());
        if (request.getImplementingAgency() != null) scheme.setImplementingAgency(request.getImplementingAgency());
        if (request.getContactEmail() != null) scheme.setContactEmail(request.getContactEmail());
        if (request.getContactPhone() != null) scheme.setContactPhone(request.getContactPhone());
        if (request.getHelplineNumber() != null) scheme.setHelplineNumber(request.getHelplineNumber());
        if (request.getBenefits() != null) scheme.setBenefits(request.getBenefits());
        if (request.getMaxFundingAmount() != null) scheme.setMaxFundingAmount(request.getMaxFundingAmount());
        if (request.getSubsidyPercentage() != null) scheme.setSubsidyPercentage(request.getSubsidyPercentage());
        if (request.getInterestRate() != null) scheme.setInterestRate(request.getInterestRate());
        if (request.getCollateralRequirement() != null) scheme.setCollateralRequirement(request.getCollateralRequirement());
        if (request.getEligibilityCriteria() != null) scheme.setEligibilityCriteria(request.getEligibilityCriteria());
        if (request.getEligibilitySummary() != null) scheme.setEligibilitySummary(request.getEligibilitySummary());
        if (request.getRequiredDocuments() != null) scheme.setRequiredDocuments(request.getRequiredDocuments());
        if (request.getApplicationProcess() != null) scheme.setApplicationProcess(request.getApplicationProcess());
        if (request.getApplicationSteps() != null) scheme.setApplicationSteps(request.getApplicationSteps());
        if (request.getApplicationUrl() != null) scheme.setApplicationUrl(request.getApplicationUrl());
        if (request.getApplicationFormUrl() != null) scheme.setApplicationFormUrl(request.getApplicationFormUrl());
        if (request.getApplicationMode() != null) scheme.setApplicationMode(request.getApplicationMode());
        scheme.setOnlineApplicationAvailable(request.isOnlineApplicationAvailable());
        if (request.getLaunchDate() != null) scheme.setLaunchDate(request.getLaunchDate());
        if (request.getLastDateToApply() != null) scheme.setLastDateToApply(request.getLastDateToApply());
        if (request.getProcessingTime() != null) scheme.setProcessingTime(request.getProcessingTime());
        scheme.setOpenForApplications(request.isOpenForApplications());
        if (request.getApplicableStates() != null) scheme.setApplicableStates(request.getApplicableStates());
        scheme.setPanIndiaScheme(request.isPanIndiaScheme());
        if (request.getLogoUrl() != null) scheme.setLogoUrl(request.getLogoUrl());
        if (request.getBannerUrl() != null) scheme.setBannerUrl(request.getBannerUrl());
        if (request.getPdfBrochureUrl() != null) scheme.setPdfBrochureUrl(request.getPdfBrochureUrl());
        if (request.getRelatedSchemeIds() != null) scheme.setRelatedSchemeIds(request.getRelatedSchemeIds());
        if (request.getCraftCategoryIds() != null) scheme.setCraftCategoryIds(request.getCraftCategoryIds());
        if (request.getTags() != null) scheme.setTags(request.getTags());
        scheme.setActive(request.isActive());
        scheme.setFeatured(request.isFeatured());
        scheme.setDisplayOrder(request.getDisplayOrder());
    }
}
