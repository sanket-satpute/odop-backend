package com.odop.root.odopfeatures.festivals.service;

import com.odop.root.odopfeatures.festivals.dto.FestivalDto.*;
import com.odop.root.odopfeatures.festivals.model.FestivalCollection;
import com.odop.root.odopfeatures.festivals.repository.FestivalCollectionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for Festival Collections management
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FestivalService {
    
    private final FestivalCollectionRepository festivalRepository;
    
    // ==================== Initialization ====================
    
    @EventListener(ApplicationReadyEvent.class)
    public void initializeDefaultFestivals() {
        if (festivalRepository.count() == 0) {
            log.info("Initializing default festival collections...");
            List<FestivalCollection> defaultFestivals = FestivalCollection.getDefaultFestivals();
            
            // Set timestamps
            LocalDateTime now = LocalDateTime.now();
            for (FestivalCollection festival : defaultFestivals) {
                festival.setCreatedAt(now);
                festival.setUpdatedAt(now);
            }
            
            festivalRepository.saveAll(defaultFestivals);
            log.info("Created {} default festival collections", defaultFestivals.size());
        }
    }
    
    // ==================== Read Operations ====================
    
    /**
     * Get all active festivals
     */
    public FestivalListResponse getAllFestivals() {
        List<FestivalCollection> festivals = festivalRepository.findByActiveTrueOrderByDisplayOrderAsc();
        LocalDate today = LocalDate.now();
        
        return FestivalListResponse.builder()
                .festivals(festivals.stream()
                        .map(FestivalCollectionResponse::from)
                        .collect(Collectors.toList()))
                .total(festivals.size())
                .activeCount((int) festivals.stream()
                        .filter(f -> isLive(f, today))
                        .count())
                .upcomingCount((int) festivals.stream()
                        .filter(f -> f.getFestivalDate() != null && f.getFestivalDate().isAfter(today))
                        .count())
                .build();
    }
    
    /**
     * Get featured festivals for homepage
     */
    public List<FestivalCollectionResponse> getFeaturedFestivals() {
        return festivalRepository.findByFeaturedTrueAndActiveTrueOrderByDisplayOrderAsc()
                .stream()
                .map(FestivalCollectionResponse::summary)
                .collect(Collectors.toList());
    }
    
    /**
     * Get live festivals (currently active)
     */
    public List<FestivalCollectionResponse> getLiveFestivals() {
        return festivalRepository.findLiveFestivals(LocalDate.now())
                .stream()
                .map(FestivalCollectionResponse::from)
                .collect(Collectors.toList());
    }
    
    /**
     * Get upcoming festivals
     */
    public List<FestivalCollectionResponse> getUpcomingFestivals() {
        return festivalRepository.findUpcomingFestivals(LocalDate.now())
                .stream()
                .sorted(Comparator.comparing(FestivalCollection::getFestivalDate))
                .map(FestivalCollectionResponse::summary)
                .collect(Collectors.toList());
    }
    
    /**
     * Get festival by ID
     */
    public FestivalCollectionResponse getFestivalById(String id) {
        FestivalCollection festival = festivalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Festival not found: " + id));
        
        // Increment view count
        incrementViewCount(id);
        
        return FestivalCollectionResponse.from(festival);
    }
    
    /**
     * Get festival by slug
     */
    public FestivalCollectionResponse getFestivalBySlug(String slug) {
        FestivalCollection festival = festivalRepository.findBySlugAndActiveTrue(slug)
                .orElseThrow(() -> new RuntimeException("Festival not found: " + slug));
        
        // Increment view count
        incrementViewCount(festival.getId());
        
        return FestivalCollectionResponse.from(festival);
    }
    
    /**
     * Get festivals by type
     */
    public List<FestivalCollectionResponse> getFestivalsByType(String type) {
        FestivalCollection.FestivalType festivalType = 
                FestivalCollection.FestivalType.valueOf(type.toUpperCase());
        
        return festivalRepository.findByTypeAndActiveTrueOrderByDisplayOrderAsc(festivalType)
                .stream()
                .map(FestivalCollectionResponse::summary)
                .collect(Collectors.toList());
    }
    
    /**
     * Get festivals by category
     */
    public List<FestivalCollectionResponse> getFestivalsByCategory(String category) {
        FestivalCollection.FestivalCategory festivalCategory = 
                FestivalCollection.FestivalCategory.valueOf(category.toUpperCase());
        
        return festivalRepository.findByCategoryAndActiveTrueOrderByDisplayOrderAsc(festivalCategory)
                .stream()
                .map(FestivalCollectionResponse::from)
                .collect(Collectors.toList());
    }
    
    /**
     * Get festivals by state
     */
    public List<FestivalCollectionResponse> getFestivalsByState(String state) {
        return festivalRepository.findByPrimaryState(state)
                .stream()
                .map(FestivalCollectionResponse::summary)
                .collect(Collectors.toList());
    }
    
    /**
     * Get festivals for a specific year
     */
    public List<FestivalCollectionResponse> getFestivalsByYear(int year) {
        return festivalRepository.findByYearAndActiveTrueOrderByFestivalDateAsc(year)
                .stream()
                .map(FestivalCollectionResponse::summary)
                .collect(Collectors.toList());
    }
    
    /**
     * Search festivals
     */
    public List<FestivalCollectionResponse> searchFestivals(String query) {
        return festivalRepository.searchByText(query)
                .stream()
                .map(FestivalCollectionResponse::summary)
                .collect(Collectors.toList());
    }
    
    /**
     * Get festival calendar (next 12 months)
     */
    public Map<String, List<FestivalCollectionResponse>> getFestivalCalendar() {
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusMonths(12);
        
        List<FestivalCollection> festivals = festivalRepository.findFestivalsBetweenDates(start, end);
        
        // Group by month
        Map<String, List<FestivalCollectionResponse>> calendar = new LinkedHashMap<>();
        
        for (FestivalCollection festival : festivals) {
            if (festival.getFestivalDate() != null) {
                String monthKey = festival.getFestivalDate().getMonth().toString() + " " + 
                        festival.getFestivalDate().getYear();
                
                calendar.computeIfAbsent(monthKey, k -> new ArrayList<>())
                        .add(FestivalCollectionResponse.summary(festival));
            }
        }
        
        return calendar;
    }
    
    /**
     * Get gift guide for a festival
     */
    public List<GiftSuggestionResponse> getGiftGuide(String festivalId) {
        FestivalCollection festival = festivalRepository.findById(festivalId)
                .orElseThrow(() -> new RuntimeException("Festival not found"));
        
        if (festival.getGiftGuide() == null) {
            return Collections.emptyList();
        }
        
        return festival.getGiftGuide().stream()
                .map(GiftSuggestionResponse::from)
                .collect(Collectors.toList());
    }
    
    /**
     * Get sections for a festival
     */
    public List<CollectionSectionResponse> getFestivalSections(String festivalId) {
        FestivalCollection festival = festivalRepository.findById(festivalId)
                .orElseThrow(() -> new RuntimeException("Festival not found"));
        
        if (festival.getSections() == null) {
            return Collections.emptyList();
        }
        
        return festival.getSections().stream()
                .sorted(Comparator.comparingInt(FestivalCollection.CollectionSection::getDisplayOrder))
                .map(CollectionSectionResponse::from)
                .collect(Collectors.toList());
    }
    
    // ==================== Write Operations ====================
    
    /**
     * Create new festival
     */
    @Transactional
    public FestivalCollectionResponse createFestival(CreateFestivalRequest request, String userId) {
        // Validate unique slug
        if (festivalRepository.existsBySlug(request.getSlug())) {
            throw new RuntimeException("Festival with slug already exists: " + request.getSlug());
        }
        
        FestivalCollection festival = new FestivalCollection();
        mapRequestToFestival(request, festival);
        festival.setCreatedAt(LocalDateTime.now());
        festival.setUpdatedAt(LocalDateTime.now());
        festival.setCreatedBy(userId);
        
        FestivalCollection saved = festivalRepository.save(festival);
        log.info("Created festival: {} ({})", saved.getName(), saved.getId());
        
        return FestivalCollectionResponse.from(saved);
    }
    
    /**
     * Update festival
     */
    @Transactional
    public FestivalCollectionResponse updateFestival(String id, CreateFestivalRequest request) {
        FestivalCollection festival = festivalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Festival not found: " + id));
        
        // Check slug uniqueness if changed
        if (!festival.getSlug().equals(request.getSlug()) && 
                festivalRepository.existsBySlug(request.getSlug())) {
            throw new RuntimeException("Festival with slug already exists: " + request.getSlug());
        }
        
        mapRequestToFestival(request, festival);
        festival.setUpdatedAt(LocalDateTime.now());
        
        FestivalCollection saved = festivalRepository.save(festival);
        log.info("Updated festival: {} ({})", saved.getName(), saved.getId());
        
        return FestivalCollectionResponse.from(saved);
    }
    
    /**
     * Delete festival (soft delete)
     */
    @Transactional
    public void deleteFestival(String id) {
        FestivalCollection festival = festivalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Festival not found: " + id));
        
        festival.setActive(false);
        festival.setUpdatedAt(LocalDateTime.now());
        festivalRepository.save(festival);
        
        log.info("Deleted festival: {} ({})", festival.getName(), id);
    }
    
    /**
     * Update featured products for festival
     */
    @Transactional
    public FestivalCollectionResponse updateFeaturedProducts(String id, List<String> productIds) {
        FestivalCollection festival = festivalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Festival not found"));
        
        festival.setFeaturedProductIds(productIds);
        festival.setProductCount(productIds.size());
        festival.setUpdatedAt(LocalDateTime.now());
        
        return FestivalCollectionResponse.from(festivalRepository.save(festival));
    }
    
    /**
     * Add section to festival
     */
    @Transactional
    public FestivalCollectionResponse addSection(String id, FestivalCollection.CollectionSection section) {
        FestivalCollection festival = festivalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Festival not found"));
        
        if (festival.getSections() == null) {
            festival.setSections(new ArrayList<>());
        }
        
        festival.getSections().add(section);
        festival.setUpdatedAt(LocalDateTime.now());
        
        return FestivalCollectionResponse.from(festivalRepository.save(festival));
    }
    
    /**
     * Update discount info
     */
    @Transactional
    public FestivalCollectionResponse updateDiscount(String id, FestivalCollection.DiscountInfo discount) {
        FestivalCollection festival = festivalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Festival not found"));
        
        festival.setDiscountInfo(discount);
        festival.setUpdatedAt(LocalDateTime.now());
        
        return FestivalCollectionResponse.from(festivalRepository.save(festival));
    }
    
    // ==================== Helper Methods ====================
    
    private void mapRequestToFestival(CreateFestivalRequest request, FestivalCollection festival) {
        festival.setSlug(request.getSlug());
        festival.setName(request.getName());
        festival.setNameHindi(request.getNameHindi());
        festival.setDescription(request.getDescription());
        festival.setDescriptionHindi(request.getDescriptionHindi());
        festival.setTagline(request.getTagline());
        festival.setStartDate(request.getStartDate());
        festival.setEndDate(request.getEndDate());
        festival.setFestivalDate(request.getFestivalDate());
        festival.setHeroImageUrl(request.getHeroImageUrl());
        festival.setBannerImageUrl(request.getBannerImageUrl());
        festival.setThumbnailUrl(request.getThumbnailUrl());
        festival.setMobileHeroUrl(request.getMobileHeroUrl());
        festival.setVideoUrl(request.getVideoUrl());
        festival.setThemeColor(request.getThemeColor());
        festival.setAccentColor(request.getAccentColor());
        festival.setPrimaryStates(request.getPrimaryStates());
        festival.setSecondaryStates(request.getSecondaryStates());
        festival.setHasCountdownTimer(request.isHasCountdownTimer());
        festival.setHasEarlyBirdOffers(request.isHasEarlyBirdOffers());
        festival.setActive(request.isActive());
        festival.setFeatured(request.isFeatured());
        festival.setDisplayOrder(request.getDisplayOrder());
        festival.setYear(request.getYear());
        festival.setSeoTitle(request.getSeoTitle());
        festival.setSeoDescription(request.getSeoDescription());
        festival.setSeoKeywords(request.getSeoKeywords());
        
        if (request.getType() != null) {
            festival.setType(FestivalCollection.FestivalType.valueOf(request.getType().toUpperCase()));
        }
        if (request.getCategory() != null) {
            festival.setCategory(FestivalCollection.FestivalCategory.valueOf(request.getCategory().toUpperCase()));
        }
        if (request.getRegionalRelevance() != null) {
            festival.setRegionalRelevance(FestivalCollection.RegionalRelevance.valueOf(
                    request.getRegionalRelevance().toUpperCase()));
        }
    }
    
    private void incrementViewCount(String festivalId) {
        festivalRepository.findById(festivalId).ifPresent(festival -> {
            festival.setViewCount(festival.getViewCount() + 1);
            festivalRepository.save(festival);
        });
    }
    
    private boolean isLive(FestivalCollection festival, LocalDate date) {
        return festival.isActive() &&
                (festival.getStartDate() == null || !date.isBefore(festival.getStartDate())) &&
                (festival.getEndDate() == null || !date.isAfter(festival.getEndDate()));
    }
    
    // ==================== Statistics ====================
    
    public Map<String, Object> getFestivalStatistics() {
        Map<String, Object> stats = new HashMap<>();
        LocalDate today = LocalDate.now();
        
        stats.put("totalFestivals", festivalRepository.countByActiveTrue());
        stats.put("featuredFestivals", festivalRepository.countByFeaturedTrueAndActiveTrue());
        stats.put("upcomingFestivals", festivalRepository.countUpcomingFestivals(today));
        stats.put("liveFestivals", festivalRepository.findLiveFestivals(today).size());
        
        // Most viewed
        List<FestivalCollection> mostViewed = festivalRepository.findMostViewedFestivals();
        if (!mostViewed.isEmpty()) {
            stats.put("mostViewedFestival", FestivalCollectionResponse.summary(mostViewed.get(0)));
        }
        
        return stats;
    }
}
