package com.odop.root.odopfeatures.artisans.service;

import com.odop.root.odopfeatures.artisans.dto.ArtisanStoryDto.*;
import com.odop.root.odopfeatures.artisans.model.ArtisanStory;
import com.odop.root.odopfeatures.artisans.model.ArtisanStory.*;
import com.odop.root.odopfeatures.artisans.repository.ArtisanStoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service layer for Artisan Story operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ArtisanStoryService {
    
    private final ArtisanStoryRepository storyRepository;
    
    // ==================== Initialization ====================
    
    @EventListener(ApplicationReadyEvent.class)
    public void initializeSampleStories() {
        if (storyRepository.count() == 0) {
            log.info("Initializing sample artisan stories...");
            List<ArtisanStory> sampleStories = ArtisanStory.getSampleStories();
            LocalDateTime now = LocalDateTime.now();
            
            for (ArtisanStory story : sampleStories) {
                story.setCreatedAt(now);
                story.setUpdatedAt(now);
                story.setPublishedAt(now);
            }
            
            storyRepository.saveAll(sampleStories);
            log.info("Initialized {} sample artisan stories", sampleStories.size());
        }
    }
    
    // ==================== Public Query Methods ====================
    
    public List<ArtisanListItem> getAllPublishedStories() {
        return storyRepository.findByStatusOrderByDisplayOrderAsc(StoryStatus.PUBLISHED)
                .stream()
                .map(this::toListItem)
                .collect(Collectors.toList());
    }
    
    public List<ArtisanListItem> getFeaturedStories() {
        return storyRepository.findByStatusAndFeaturedTrueOrderByDisplayOrderAsc(StoryStatus.PUBLISHED)
                .stream()
                .map(this::toListItem)
                .collect(Collectors.toList());
    }
    
    public ArtisanStoryResponse getStoryById(String id) {
        ArtisanStory story = storyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Artisan story not found: " + id));
        
        // Increment view count
        story.setViewCount(story.getViewCount() + 1);
        storyRepository.save(story);
        
        return toResponse(story);
    }
    
    public ArtisanStoryResponse getStoryBySlug(String slug) {
        ArtisanStory story = storyRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Artisan story not found: " + slug));
        
        // Increment view count
        story.setViewCount(story.getViewCount() + 1);
        storyRepository.save(story);
        
        return toResponse(story);
    }
    
    // ==================== Filter Methods ====================
    
    public List<ArtisanListItem> getStoriesByState(String stateCode) {
        return storyRepository.findPublishedByStateCode(stateCode)
                .stream()
                .map(this::toListItem)
                .collect(Collectors.toList());
    }
    
    public List<ArtisanListItem> getStoriesByCraft(String craft) {
        return storyRepository.findPublishedByCraft(craft)
                .stream()
                .map(this::toListItem)
                .collect(Collectors.toList());
    }
    
    public List<ArtisanListItem> getNationalAwardees() {
        return storyRepository.findNationalAwardees()
                .stream()
                .map(this::toListItem)
                .collect(Collectors.toList());
    }
    
    public List<ArtisanListItem> getStateAwardees() {
        return storyRepository.findStateAwardees()
                .stream()
                .map(this::toListItem)
                .collect(Collectors.toList());
    }
    
    public List<ArtisanListItem> getGiTagHolders() {
        return storyRepository.findGiTagHolders()
                .stream()
                .map(this::toListItem)
                .collect(Collectors.toList());
    }
    
    public List<ArtisanListItem> getAvailableForWorkshops() {
        return storyRepository.findAvailableForWorkshops()
                .stream()
                .map(this::toListItem)
                .collect(Collectors.toList());
    }
    
    public List<ArtisanListItem> getAvailableForCommissions() {
        return storyRepository.findAvailableForCommissions()
                .stream()
                .map(this::toListItem)
                .collect(Collectors.toList());
    }
    
    public List<ArtisanListItem> getExperiencedArtisans(int minYears) {
        return storyRepository.findWithMinExperience(minYears)
                .stream()
                .map(this::toListItem)
                .collect(Collectors.toList());
    }
    
    public List<ArtisanListItem> getMultiGenerationArtisans(int minGenerations) {
        return storyRepository.findWithMinGenerations(minGenerations)
                .stream()
                .map(this::toListItem)
                .collect(Collectors.toList());
    }
    
    // ==================== Search ====================
    
    public List<ArtisanListItem> searchStories(String query) {
        return storyRepository.searchStories(query)
                .stream()
                .map(this::toListItem)
                .collect(Collectors.toList());
    }
    
    // ==================== Map Data ====================
    
    public List<ArtisanMapMarker> getMapMarkers() {
        return storyRepository.findAllPublished()
                .stream()
                .filter(s -> s.getLatitude() != null && s.getLongitude() != null)
                .map(this::toMapMarker)
                .collect(Collectors.toList());
    }
    
    public List<ArtisanMapMarker> getMapMarkersInBounds(
            Double minLat, Double minLng, Double maxLat, Double maxLng) {
        return storyRepository.findInBoundingBox(minLat, minLng, maxLat, maxLng)
                .stream()
                .map(this::toMapMarker)
                .collect(Collectors.toList());
    }
    
    // ==================== Overview & Statistics ====================
    
    public ArtisanStoriesOverview getOverview() {
        long totalPublished = storyRepository.countByStatus(StoryStatus.PUBLISHED);
        long featuredCount = storyRepository.countByStatusAndFeaturedTrue(StoryStatus.PUBLISHED);
        long nationalAwardees = storyRepository.countByStatusAndNationalAwardeeTrue(StoryStatus.PUBLISHED);
        long giTagHolders = storyRepository.countByStatusAndGiTagHolderTrue(StoryStatus.PUBLISHED);
        
        List<CraftCount> craftStats = storyRepository.getArtisanCountByCraft()
                .stream()
                .map(r -> CraftCount.builder()
                        .craft(r.get_id())
                        .count(r.getCount())
                        .build())
                .collect(Collectors.toList());
        
        List<StateCount> stateStats = storyRepository.getArtisanCountByState()
                .stream()
                .map(r -> StateCount.builder()
                        .state(r.get_id() != null ? r.get_id().getState() : "Unknown")
                        .stateCode(r.get_id() != null ? r.get_id().getStateCode() : "XX")
                        .count(r.getCount())
                        .build())
                .collect(Collectors.toList());
        
        return ArtisanStoriesOverview.builder()
                .totalStories((int) totalPublished)
                .featuredStories((int) featuredCount)
                .nationalAwardees((int) nationalAwardees)
                .giTagHolders((int) giTagHolders)
                .featuredArtisans(getFeaturedStories())
                .artisansByCraft(craftStats)
                .artisansByState(stateStats)
                .build();
    }
    
    public ArtisanFilters getFilters() {
        List<FilterOption> crafts = storyRepository.getArtisanCountByCraft()
                .stream()
                .map(r -> FilterOption.builder()
                        .value(r.get_id())
                        .label(r.get_id())
                        .count(r.getCount())
                        .build())
                .collect(Collectors.toList());
        
        List<FilterOption> states = storyRepository.getArtisanCountByState()
                .stream()
                .map(r -> FilterOption.builder()
                        .value(r.get_id() != null ? r.get_id().getStateCode() : "XX")
                        .label(r.get_id() != null ? r.get_id().getState() : "Unknown")
                        .count(r.getCount())
                        .build())
                .collect(Collectors.toList());
        
        List<FilterOption> awardLevels = Arrays.asList(
                FilterOption.builder().value("NATIONAL").label("National Award").count(
                        storyRepository.countByStatusAndNationalAwardeeTrue(StoryStatus.PUBLISHED)
                ).build(),
                FilterOption.builder().value("STATE").label("State Award").count(
                        storyRepository.findStateAwardees().size()
                ).build(),
                FilterOption.builder().value("GI_TAG").label("GI Tag Holder").count(
                        storyRepository.countByStatusAndGiTagHolderTrue(StoryStatus.PUBLISHED)
                ).build()
        );
        
        return ArtisanFilters.builder()
                .crafts(crafts)
                .states(states)
                .awardLevels(awardLevels)
                .build();
    }
    
    // ==================== Share Tracking ====================
    
    public void trackShare(String storyId) {
        storyRepository.findById(storyId).ifPresent(story -> {
            story.setShareCount(story.getShareCount() + 1);
            storyRepository.save(story);
        });
    }
    
    // ==================== Admin Methods ====================
    
    @Transactional
    public ArtisanStoryResponse createStory(CreateArtisanStoryRequest request) {
        if (storyRepository.findBySlug(request.getSlug()).isPresent()) {
            throw new RuntimeException("Story with slug already exists: " + request.getSlug());
        }
        
        ArtisanStory story = mapCreateRequestToStory(request);
        story.setStatus(StoryStatus.DRAFT);
        story.setCreatedAt(LocalDateTime.now());
        story.setUpdatedAt(LocalDateTime.now());
        
        ArtisanStory saved = storyRepository.save(story);
        log.info("Created new artisan story: {}", saved.getArtisanName());
        
        return toResponse(saved);
    }
    
    @Transactional
    public ArtisanStoryResponse updateStory(String id, UpdateArtisanStoryRequest request) {
        ArtisanStory story = storyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Story not found: " + id));
        
        updateStoryFromRequest(story, request);
        story.setUpdatedAt(LocalDateTime.now());
        
        ArtisanStory saved = storyRepository.save(story);
        log.info("Updated artisan story: {}", saved.getArtisanName());
        
        return toResponse(saved);
    }
    
    @Transactional
    public ArtisanStoryResponse publishStory(String id) {
        ArtisanStory story = storyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Story not found: " + id));
        
        story.setStatus(StoryStatus.PUBLISHED);
        story.setPublishedAt(LocalDateTime.now());
        story.setUpdatedAt(LocalDateTime.now());
        
        ArtisanStory saved = storyRepository.save(story);
        log.info("Published artisan story: {}", saved.getArtisanName());
        
        return toResponse(saved);
    }
    
    @Transactional
    public ArtisanStoryResponse archiveStory(String id) {
        ArtisanStory story = storyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Story not found: " + id));
        
        story.setStatus(StoryStatus.ARCHIVED);
        story.setUpdatedAt(LocalDateTime.now());
        
        ArtisanStory saved = storyRepository.save(story);
        log.info("Archived artisan story: {}", saved.getArtisanName());
        
        return toResponse(saved);
    }
    
    @Transactional
    public void deleteStory(String id) {
        ArtisanStory story = storyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Story not found: " + id));
        
        storyRepository.delete(story);
        log.info("Deleted artisan story: {}", story.getArtisanName());
    }
    
    @Transactional
    public ArtisanStoryResponse toggleFeatured(String id) {
        ArtisanStory story = storyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Story not found: " + id));
        
        story.setFeatured(!story.isFeatured());
        story.setUpdatedAt(LocalDateTime.now());
        
        ArtisanStory saved = storyRepository.save(story);
        return toResponse(saved);
    }
    
    @Transactional
    public ArtisanStoryResponse verifyStory(String id) {
        ArtisanStory story = storyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Story not found: " + id));
        
        story.setVerified(true);
        story.setUpdatedAt(LocalDateTime.now());
        
        ArtisanStory saved = storyRepository.save(story);
        return toResponse(saved);
    }
    
    @Transactional
    public ArtisanStoryResponse addMedia(String id, AddMediaRequest request) {
        ArtisanStory story = storyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Story not found: " + id));
        
        if (story.getGallery() == null) {
            story.setGallery(new ArrayList<>());
        }
        
        int nextOrder = story.getGallery().size();
        story.getGallery().add(MediaItem.builder()
                .url(request.getUrl())
                .thumbnailUrl(request.getThumbnailUrl())
                .caption(request.getCaption())
                .captionHindi(request.getCaptionHindi())
                .type(request.getType())
                .orderIndex(nextOrder)
                .build());
        
        story.setUpdatedAt(LocalDateTime.now());
        ArtisanStory saved = storyRepository.save(story);
        
        return toResponse(saved);
    }
    
    @Transactional
    public ArtisanStoryResponse addVideo(String id, AddVideoRequest request) {
        ArtisanStory story = storyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Story not found: " + id));
        
        if (story.getVideos() == null) {
            story.setVideos(new ArrayList<>());
        }
        
        story.getVideos().add(VideoItem.builder()
                .title(request.getTitle())
                .titleHindi(request.getTitleHindi())
                .description(request.getDescription())
                .videoUrl(request.getVideoUrl())
                .thumbnailUrl(request.getThumbnailUrl())
                .duration(request.getDuration())
                .type(request.getType())
                .featured(request.isFeatured())
                .build());
        
        story.setUpdatedAt(LocalDateTime.now());
        ArtisanStory saved = storyRepository.save(story);
        
        return toResponse(saved);
    }
    
    @Transactional
    public ArtisanStoryResponse addAward(String id, AddAwardRequest request) {
        ArtisanStory story = storyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Story not found: " + id));
        
        if (story.getAwards() == null) {
            story.setAwards(new ArrayList<>());
        }
        
        Award award = Award.builder()
                .name(request.getName())
                .nameHindi(request.getNameHindi())
                .organization(request.getOrganization())
                .year(request.getYear())
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .level(request.getLevel())
                .build();
        
        story.getAwards().add(award);
        
        // Update award flags
        if (request.getLevel() == AwardLevel.NATIONAL) {
            story.setNationalAwardee(true);
        } else if (request.getLevel() == AwardLevel.STATE) {
            story.setStateAwardee(true);
        }
        
        story.setUpdatedAt(LocalDateTime.now());
        ArtisanStory saved = storyRepository.save(story);
        
        return toResponse(saved);
    }
    
    // ==================== Admin Query Methods ====================
    
    public List<ArtisanListItem> getDraftStories() {
        return storyRepository.findByStatus(StoryStatus.DRAFT)
                .stream()
                .map(this::toListItem)
                .collect(Collectors.toList());
    }
    
    public List<ArtisanListItem> getPendingReviewStories() {
        return storyRepository.findByStatus(StoryStatus.PENDING_REVIEW)
                .stream()
                .map(this::toListItem)
                .collect(Collectors.toList());
    }
    
    // ==================== Mapping Methods ====================
    
    private ArtisanStoryResponse toResponse(ArtisanStory story) {
        return ArtisanStoryResponse.builder()
                .id(story.getId())
                .slug(story.getSlug())
                .artisanName(story.getArtisanName())
                .artisanNameHindi(story.getArtisanNameHindi())
                .title(story.getTitle())
                .titleHindi(story.getTitleHindi())
                .profileImageUrl(story.getProfileImageUrl())
                .coverImageUrl(story.getCoverImageUrl())
                .village(story.getVillage())
                .district(story.getDistrict())
                .state(story.getState())
                .stateCode(story.getStateCode())
                .coordinates(story.getLatitude() != null && story.getLongitude() != null ?
                        LocationCoordinates.builder()
                                .latitude(story.getLatitude())
                                .longitude(story.getLongitude())
                                .build() : null)
                .primaryCraft(story.getPrimaryCraft())
                .primaryCraftHindi(story.getPrimaryCraftHindi())
                .craftCategoryId(story.getCraftCategoryId())
                .additionalCrafts(story.getAdditionalCrafts())
                .yearsOfExperience(story.getYearsOfExperience())
                .generationsInCraft(story.getGenerationsInCraft())
                .craftOriginStory(story.getCraftOriginStory())
                .craftOriginStoryHindi(story.getCraftOriginStoryHindi())
                .shortBio(story.getShortBio())
                .shortBioHindi(story.getShortBioHindi())
                .fullStory(story.getFullStory())
                .fullStoryHindi(story.getFullStoryHindi())
                .quote(story.getQuote())
                .quoteHindi(story.getQuoteHindi())
                .storySections(mapStorySections(story.getStorySections()))
                .familyBackground(mapFamilyBackground(story.getFamilyBackground()))
                .teachingPhilosophy(story.getTeachingPhilosophy())
                .apprenticesTrained(story.getApprenticesTrained())
                .familyMembersInCraft(story.getFamilyMembersInCraft())
                .gallery(mapGallery(story.getGallery()))
                .videos(mapVideos(story.getVideos()))
                .primaryVideoUrl(story.getPrimaryVideoUrl())
                .primaryVideoThumbnail(story.getPrimaryVideoThumbnail())
                .featuredProductIds(story.getFeaturedProductIds())
                .signatureWorks(mapSignatureWorks(story.getSignatureWorks()))
                .workshopDescription(story.getWorkshopDescription())
                .workshopImageUrl(story.getWorkshopImageUrl())
                .awards(mapAwards(story.getAwards()))
                .certifications(story.getCertifications())
                .giTagHolder(story.isGiTagHolder())
                .giTagDetails(story.getGiTagDetails())
                .nationalAwardee(story.isNationalAwardee())
                .stateAwardee(story.isStateAwardee())
                .contactInfo(mapContactInfo(story.getContactInfo()))
                .socialLinks(mapSocialLinks(story.getSocialLinks()))
                .vendorId(story.getVendorId())
                .exhibitions(mapExhibitions(story.getExhibitions()))
                .availableForWorkshops(story.isAvailableForWorkshops())
                .availableForCommissions(story.isAvailableForCommissions())
                .workshopDetails(story.getWorkshopDetails())
                .featured(story.isFeatured())
                .verified(story.isVerified())
                .viewCount(story.getViewCount())
                .shareCount(story.getShareCount())
                .tags(story.getTags())
                .build();
    }
    
    private ArtisanListItem toListItem(ArtisanStory story) {
        return ArtisanListItem.builder()
                .id(story.getId())
                .slug(story.getSlug())
                .artisanName(story.getArtisanName())
                .artisanNameHindi(story.getArtisanNameHindi())
                .title(story.getTitle())
                .titleHindi(story.getTitleHindi())
                .profileImageUrl(story.getProfileImageUrl())
                .village(story.getVillage())
                .district(story.getDistrict())
                .state(story.getState())
                .primaryCraft(story.getPrimaryCraft())
                .primaryCraftHindi(story.getPrimaryCraftHindi())
                .yearsOfExperience(story.getYearsOfExperience())
                .generationsInCraft(story.getGenerationsInCraft())
                .shortBio(story.getShortBio())
                .quote(story.getQuote())
                .nationalAwardee(story.isNationalAwardee())
                .stateAwardee(story.isStateAwardee())
                .giTagHolder(story.isGiTagHolder())
                .featured(story.isFeatured())
                .verified(story.isVerified())
                .build();
    }
    
    private ArtisanMapMarker toMapMarker(ArtisanStory story) {
        return ArtisanMapMarker.builder()
                .id(story.getId())
                .slug(story.getSlug())
                .artisanName(story.getArtisanName())
                .craft(story.getPrimaryCraft())
                .profileImageUrl(story.getProfileImageUrl())
                .village(story.getVillage())
                .district(story.getDistrict())
                .state(story.getState())
                .latitude(story.getLatitude())
                .longitude(story.getLongitude())
                .featured(story.isFeatured())
                .nationalAwardee(story.isNationalAwardee())
                .build();
    }
    
    // ==================== Helper Mapping Methods ====================
    
    private List<StorySectionDto> mapStorySections(List<StorySection> sections) {
        if (sections == null) return null;
        return sections.stream()
                .map(s -> StorySectionDto.builder()
                        .title(s.getTitle())
                        .titleHindi(s.getTitleHindi())
                        .content(s.getContent())
                        .contentHindi(s.getContentHindi())
                        .imageUrl(s.getImageUrl())
                        .imageCaption(s.getImageCaption())
                        .orderIndex(s.getOrderIndex())
                        .build())
                .collect(Collectors.toList());
    }
    
    private FamilyBackgroundDto mapFamilyBackground(FamilyBackground fb) {
        if (fb == null) return null;
        return FamilyBackgroundDto.builder()
                .fatherName(fb.getFatherName())
                .fatherCraft(fb.getFatherCraft())
                .grandfatherName(fb.getGrandfatherName())
                .grandfatherCraft(fb.getGrandfatherCraft())
                .familyHistory(fb.getFamilyHistory())
                .familyHistoryHindi(fb.getFamilyHistoryHindi())
                .generationCount(fb.getGenerationCount())
                .familyPhotoUrl(fb.getFamilyPhotoUrl())
                .build();
    }
    
    private List<MediaItemDto> mapGallery(List<MediaItem> items) {
        if (items == null) return null;
        return items.stream()
                .map(m -> MediaItemDto.builder()
                        .url(m.getUrl())
                        .thumbnailUrl(m.getThumbnailUrl())
                        .caption(m.getCaption())
                        .captionHindi(m.getCaptionHindi())
                        .type(m.getType() != null ? m.getType().name() : null)
                        .orderIndex(m.getOrderIndex())
                        .build())
                .collect(Collectors.toList());
    }
    
    private List<VideoItemDto> mapVideos(List<VideoItem> videos) {
        if (videos == null) return null;
        return videos.stream()
                .map(v -> VideoItemDto.builder()
                        .title(v.getTitle())
                        .titleHindi(v.getTitleHindi())
                        .description(v.getDescription())
                        .videoUrl(v.getVideoUrl())
                        .thumbnailUrl(v.getThumbnailUrl())
                        .duration(v.getDuration())
                        .type(v.getType() != null ? v.getType().name() : null)
                        .featured(v.isFeatured())
                        .build())
                .collect(Collectors.toList());
    }
    
    private List<SignatureDto> mapSignatureWorks(List<Signature> signatures) {
        if (signatures == null) return null;
        return signatures.stream()
                .map(s -> SignatureDto.builder()
                        .name(s.getName())
                        .nameHindi(s.getNameHindi())
                        .description(s.getDescription())
                        .descriptionHindi(s.getDescriptionHindi())
                        .imageUrl(s.getImageUrl())
                        .price(s.getPrice())
                        .available(s.isAvailable())
                        .build())
                .collect(Collectors.toList());
    }
    
    private List<AwardDto> mapAwards(List<Award> awards) {
        if (awards == null) return null;
        return awards.stream()
                .map(a -> AwardDto.builder()
                        .name(a.getName())
                        .nameHindi(a.getNameHindi())
                        .organization(a.getOrganization())
                        .year(a.getYear())
                        .description(a.getDescription())
                        .imageUrl(a.getImageUrl())
                        .level(a.getLevel() != null ? a.getLevel().name() : null)
                        .build())
                .collect(Collectors.toList());
    }
    
    private ContactInfoDto mapContactInfo(ContactInfo ci) {
        if (ci == null) return null;
        return ContactInfoDto.builder()
                .phone(ci.getPhone())
                .email(ci.getEmail())
                .address(ci.getAddress())
                .workshopAddress(ci.getWorkshopAddress())
                .bestTimeToContact(ci.getBestTimeToContact())
                .build();
    }
    
    private SocialLinksDto mapSocialLinks(SocialLinks sl) {
        if (sl == null) return null;
        return SocialLinksDto.builder()
                .facebook(sl.getFacebook())
                .instagram(sl.getInstagram())
                .youtube(sl.getYoutube())
                .twitter(sl.getTwitter())
                .website(sl.getWebsite())
                .build();
    }
    
    private List<ExhibitionDto> mapExhibitions(List<Exhibition> exhibitions) {
        if (exhibitions == null) return null;
        return exhibitions.stream()
                .map(e -> ExhibitionDto.builder()
                        .name(e.getName())
                        .location(e.getLocation())
                        .startDate(e.getStartDate())
                        .endDate(e.getEndDate())
                        .description(e.getDescription())
                        .upcoming(e.isUpcoming())
                        .build())
                .collect(Collectors.toList());
    }
    
    private ArtisanStory mapCreateRequestToStory(CreateArtisanStoryRequest request) {
        return ArtisanStory.builder()
                .slug(request.getSlug())
                .artisanName(request.getArtisanName())
                .artisanNameHindi(request.getArtisanNameHindi())
                .title(request.getTitle())
                .titleHindi(request.getTitleHindi())
                .profileImageUrl(request.getProfileImageUrl())
                .coverImageUrl(request.getCoverImageUrl())
                .village(request.getVillage())
                .district(request.getDistrict())
                .state(request.getState())
                .stateCode(request.getStateCode())
                .pincode(request.getPincode())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .primaryCraft(request.getPrimaryCraft())
                .primaryCraftHindi(request.getPrimaryCraftHindi())
                .craftCategoryId(request.getCraftCategoryId())
                .additionalCrafts(request.getAdditionalCrafts())
                .yearsOfExperience(request.getYearsOfExperience())
                .generationsInCraft(request.getGenerationsInCraft())
                .craftOriginStory(request.getCraftOriginStory())
                .craftOriginStoryHindi(request.getCraftOriginStoryHindi())
                .shortBio(request.getShortBio())
                .shortBioHindi(request.getShortBioHindi())
                .fullStory(request.getFullStory())
                .fullStoryHindi(request.getFullStoryHindi())
                .quote(request.getQuote())
                .quoteHindi(request.getQuoteHindi())
                .storySections(request.getStorySections())
                .familyBackground(request.getFamilyBackground())
                .teachingPhilosophy(request.getTeachingPhilosophy())
                .apprenticesTrained(request.getApprenticesTrained())
                .familyMembersInCraft(request.getFamilyMembersInCraft())
                .gallery(request.getGallery())
                .videos(request.getVideos())
                .primaryVideoUrl(request.getPrimaryVideoUrl())
                .primaryVideoThumbnail(request.getPrimaryVideoThumbnail())
                .featuredProductIds(request.getFeaturedProductIds())
                .signatureWorks(request.getSignatureWorks())
                .workshopDescription(request.getWorkshopDescription())
                .workshopImageUrl(request.getWorkshopImageUrl())
                .awards(request.getAwards())
                .certifications(request.getCertifications())
                .giTagHolder(request.isGiTagHolder())
                .giTagDetails(request.getGiTagDetails())
                .contactInfo(request.getContactInfo())
                .socialLinks(request.getSocialLinks())
                .vendorId(request.getVendorId())
                .exhibitions(request.getExhibitions())
                .availableForWorkshops(request.isAvailableForWorkshops())
                .availableForCommissions(request.isAvailableForCommissions())
                .workshopDetails(request.getWorkshopDetails())
                .featured(request.isFeatured())
                .displayOrder(request.getDisplayOrder())
                .tags(request.getTags())
                .build();
    }
    
    private void updateStoryFromRequest(ArtisanStory story, UpdateArtisanStoryRequest request) {
        if (request.getArtisanName() != null) story.setArtisanName(request.getArtisanName());
        if (request.getArtisanNameHindi() != null) story.setArtisanNameHindi(request.getArtisanNameHindi());
        if (request.getTitle() != null) story.setTitle(request.getTitle());
        if (request.getTitleHindi() != null) story.setTitleHindi(request.getTitleHindi());
        if (request.getProfileImageUrl() != null) story.setProfileImageUrl(request.getProfileImageUrl());
        if (request.getCoverImageUrl() != null) story.setCoverImageUrl(request.getCoverImageUrl());
        if (request.getVillage() != null) story.setVillage(request.getVillage());
        if (request.getDistrict() != null) story.setDistrict(request.getDistrict());
        if (request.getState() != null) story.setState(request.getState());
        if (request.getStateCode() != null) story.setStateCode(request.getStateCode());
        if (request.getPincode() != null) story.setPincode(request.getPincode());
        if (request.getLatitude() != null) story.setLatitude(request.getLatitude());
        if (request.getLongitude() != null) story.setLongitude(request.getLongitude());
        if (request.getPrimaryCraft() != null) story.setPrimaryCraft(request.getPrimaryCraft());
        if (request.getPrimaryCraftHindi() != null) story.setPrimaryCraftHindi(request.getPrimaryCraftHindi());
        if (request.getCraftCategoryId() != null) story.setCraftCategoryId(request.getCraftCategoryId());
        if (request.getAdditionalCrafts() != null) story.setAdditionalCrafts(request.getAdditionalCrafts());
        if (request.getYearsOfExperience() != null) story.setYearsOfExperience(request.getYearsOfExperience());
        if (request.getGenerationsInCraft() != null) story.setGenerationsInCraft(request.getGenerationsInCraft());
        if (request.getCraftOriginStory() != null) story.setCraftOriginStory(request.getCraftOriginStory());
        if (request.getShortBio() != null) story.setShortBio(request.getShortBio());
        if (request.getShortBioHindi() != null) story.setShortBioHindi(request.getShortBioHindi());
        if (request.getFullStory() != null) story.setFullStory(request.getFullStory());
        if (request.getFullStoryHindi() != null) story.setFullStoryHindi(request.getFullStoryHindi());
        if (request.getQuote() != null) story.setQuote(request.getQuote());
        if (request.getQuoteHindi() != null) story.setQuoteHindi(request.getQuoteHindi());
        if (request.getStorySections() != null) story.setStorySections(request.getStorySections());
        if (request.getFamilyBackground() != null) story.setFamilyBackground(request.getFamilyBackground());
        if (request.getTeachingPhilosophy() != null) story.setTeachingPhilosophy(request.getTeachingPhilosophy());
        if (request.getApprenticesTrained() != null) story.setApprenticesTrained(request.getApprenticesTrained());
        if (request.getGallery() != null) story.setGallery(request.getGallery());
        if (request.getVideos() != null) story.setVideos(request.getVideos());
        if (request.getPrimaryVideoUrl() != null) story.setPrimaryVideoUrl(request.getPrimaryVideoUrl());
        if (request.getAwards() != null) story.setAwards(request.getAwards());
        if (request.getCertifications() != null) story.setCertifications(request.getCertifications());
        if (request.getGiTagHolder() != null) story.setGiTagHolder(request.getGiTagHolder());
        if (request.getGiTagDetails() != null) story.setGiTagDetails(request.getGiTagDetails());
        if (request.getContactInfo() != null) story.setContactInfo(request.getContactInfo());
        if (request.getSocialLinks() != null) story.setSocialLinks(request.getSocialLinks());
        if (request.getVendorId() != null) story.setVendorId(request.getVendorId());
        if (request.getExhibitions() != null) story.setExhibitions(request.getExhibitions());
        if (request.getAvailableForWorkshops() != null) story.setAvailableForWorkshops(request.getAvailableForWorkshops());
        if (request.getAvailableForCommissions() != null) story.setAvailableForCommissions(request.getAvailableForCommissions());
        if (request.getWorkshopDetails() != null) story.setWorkshopDetails(request.getWorkshopDetails());
        if (request.getFeatured() != null) story.setFeatured(request.getFeatured());
        if (request.getDisplayOrder() != null) story.setDisplayOrder(request.getDisplayOrder());
        if (request.getTags() != null) story.setTags(request.getTags());
        if (request.getStatus() != null) story.setStatus(request.getStatus());
    }
}
