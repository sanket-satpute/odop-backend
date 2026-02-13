package com.odop.root.odopfeatures.districtmap.service;

import com.odop.root.odopfeatures.districtmap.dto.DistrictMapDto.*;
import com.odop.root.odopfeatures.districtmap.model.DistrictInfo;
import com.odop.root.odopfeatures.districtmap.repository.DistrictInfoRepository;
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
 * Service for District Map Browse Feature
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DistrictMapService {
    
    private final DistrictInfoRepository districtRepository;
    
    // Cache for state information
    private final Map<String, DistrictInfo.StateInfo> stateCache = DistrictInfo.getIndianStates();
    
    // ==================== Initialization ====================
    
    @EventListener(ApplicationReadyEvent.class)
    public void initializeSampleData() {
        if (districtRepository.count() == 0) {
            log.info("Initializing sample district data...");
            List<DistrictInfo> sampleDistricts = DistrictInfo.getSampleDistricts();
            
            LocalDateTime now = LocalDateTime.now();
            for (DistrictInfo district : sampleDistricts) {
                district.setActive(true);
                district.setCreatedAt(now);
                district.setUpdatedAt(now);
            }
            
            districtRepository.saveAll(sampleDistricts);
            log.info("Created {} sample districts", sampleDistricts.size());
        }
    }
    
    // ==================== Map Data ====================
    
    /**
     * Get complete map data including states and featured districts
     */
    public MapDataResponse getMapData() {
        List<DistrictInfo> allDistricts = districtRepository.findByActiveTrueOrderByDisplayPriorityDesc();
        List<DistrictInfo> featuredDistricts = districtRepository.findByFeaturedTrueAndActiveTrueOrderByDisplayPriorityDesc();
        
        // Build state responses
        Map<String, List<DistrictInfo>> districtsByState = allDistricts.stream()
                .collect(Collectors.groupingBy(DistrictInfo::getStateCode));
        
        List<StateResponse> stateResponses = stateCache.entrySet().stream()
                .map(entry -> buildStateResponse(entry.getKey(), entry.getValue(), 
                        districtsByState.getOrDefault(entry.getKey(), Collections.emptyList())))
                .filter(state -> state.getDistrictCount() > 0 || hasOdopProducts(state.getStateCode()))
                .collect(Collectors.toList());
        
        return MapDataResponse.builder()
                .states(stateResponses)
                .featuredDistricts(featuredDistricts.stream()
                        .map(DistrictResponse::summary)
                        .collect(Collectors.toList()))
                .statistics(buildMapStatistics())
                .build();
    }
    
    /**
     * Get map markers for visualization
     */
    public List<MapMarker> getMapMarkers() {
        return districtRepository.findByActiveTrueOrderByDisplayPriorityDesc()
                .stream()
                .map(this::buildMapMarker)
                .collect(Collectors.toList());
    }
    
    // ==================== State Operations ====================
    
    /**
     * Get all states with summary
     */
    public List<StateResponse> getAllStates() {
        List<DistrictInfo> allDistricts = districtRepository.findByActiveTrueOrderByDisplayPriorityDesc();
        Map<String, List<DistrictInfo>> districtsByState = allDistricts.stream()
                .collect(Collectors.groupingBy(DistrictInfo::getStateCode));
        
        return stateCache.entrySet().stream()
                .map(entry -> buildStateResponse(entry.getKey(), entry.getValue(),
                        districtsByState.getOrDefault(entry.getKey(), Collections.emptyList())))
                .sorted(Comparator.comparing(StateResponse::getName))
                .collect(Collectors.toList());
    }
    
    /**
     * Get state details with districts
     */
    public StateResponse getStateByCode(String stateCode) {
        DistrictInfo.StateInfo stateInfo = stateCache.get(stateCode.toUpperCase());
        if (stateInfo == null) {
            throw new RuntimeException("State not found: " + stateCode);
        }
        
        List<DistrictInfo> districts = districtRepository
                .findByStateCodeAndActiveTrueOrderByNameAsc(stateCode.toUpperCase());
        
        StateResponse response = buildStateResponse(stateCode.toUpperCase(), stateInfo, districts);
        response.setFeaturedDistricts(districts.stream()
                .filter(DistrictInfo::isFeatured)
                .map(DistrictResponse::summary)
                .collect(Collectors.toList()));
        
        return response;
    }
    
    /**
     * Get districts by state
     */
    public List<DistrictResponse> getDistrictsByState(String stateCode) {
        return districtRepository.findByStateCodeAndActiveTrueOrderByNameAsc(stateCode.toUpperCase())
                .stream()
                .map(DistrictResponse::from)
                .collect(Collectors.toList());
    }
    
    // ==================== Region Operations ====================
    
    /**
     * Get region summary
     */
    public List<RegionResponse> getAllRegions() {
        List<DistrictInfo> allDistricts = districtRepository.findByActiveTrueOrderByDisplayPriorityDesc();
        
        Map<DistrictInfo.Region, List<DistrictInfo>> districtsByRegion = allDistricts.stream()
                .filter(d -> d.getRegion() != null)
                .collect(Collectors.groupingBy(DistrictInfo::getRegion));
        
        return Arrays.stream(DistrictInfo.Region.values())
                .map(region -> {
                    List<DistrictInfo> regionDistricts = districtsByRegion
                            .getOrDefault(region, Collections.emptyList());
                    
                    // Group by state within region
                    Map<String, List<DistrictInfo>> byState = regionDistricts.stream()
                            .collect(Collectors.groupingBy(DistrictInfo::getStateCode));
                    
                    List<StateResponse> states = byState.entrySet().stream()
                            .map(entry -> {
                                DistrictInfo.StateInfo stateInfo = stateCache.get(entry.getKey());
                                return buildStateResponse(entry.getKey(), stateInfo, entry.getValue());
                            })
                            .collect(Collectors.toList());
                    
                    return RegionResponse.builder()
                            .region(region.name())
                            .displayName(region.getDisplayName())
                            .displayNameHindi(region.getDisplayNameHindi())
                            .states(states)
                            .totalDistricts(regionDistricts.size())
                            .totalProducts(regionDistricts.stream()
                                    .mapToLong(DistrictInfo::getTotalProducts)
                                    .sum())
                            .build();
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Get districts by region
     */
    public List<DistrictResponse> getDistrictsByRegion(String region) {
        DistrictInfo.Region regionEnum = DistrictInfo.Region.valueOf(region.toUpperCase());
        return districtRepository.findByRegionAndActiveTrueOrderByDisplayPriorityDesc(regionEnum)
                .stream()
                .map(DistrictResponse::from)
                .collect(Collectors.toList());
    }
    
    // ==================== District Operations ====================
    
    /**
     * Get district by ID
     */
    public DistrictResponse getDistrictById(String id) {
        DistrictInfo district = districtRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("District not found: " + id));
        return DistrictResponse.from(district);
    }
    
    /**
     * Get district by code
     */
    public DistrictResponse getDistrictByCode(String stateCode, String districtCode) {
        DistrictInfo district = districtRepository
                .findByStateCodeAndDistrictCode(stateCode.toUpperCase(), districtCode.toUpperCase())
                .orElseThrow(() -> new RuntimeException("District not found: " + stateCode + "/" + districtCode));
        return DistrictResponse.from(district);
    }
    
    /**
     * Get featured districts
     */
    public List<DistrictResponse> getFeaturedDistricts() {
        return districtRepository.findByFeaturedTrueAndActiveTrueOrderByDisplayPriorityDesc()
                .stream()
                .map(DistrictResponse::from)
                .collect(Collectors.toList());
    }
    
    /**
     * Get districts with GI tagged products
     */
    public List<DistrictResponse> getGiTaggedDistricts() {
        return districtRepository.findDistrictsWithGiTags()
                .stream()
                .map(DistrictResponse::from)
                .collect(Collectors.toList());
    }
    
    /**
     * Get districts by craft
     */
    public List<DistrictResponse> getDistrictsByCraft(String craft) {
        return districtRepository.findByPrimaryCraft(craft)
                .stream()
                .map(DistrictResponse::from)
                .collect(Collectors.toList());
    }
    
    /**
     * Get districts by craft category
     */
    public List<DistrictResponse> getDistrictsByCraftCategory(String categoryId) {
        return districtRepository.findByCraftCategoryId(categoryId)
                .stream()
                .map(DistrictResponse::from)
                .collect(Collectors.toList());
    }
    
    /**
     * Search districts
     */
    public DistrictSearchResponse searchDistricts(String query) {
        List<DistrictInfo> results = districtRepository.searchDistricts(query);
        
        return DistrictSearchResponse.builder()
                .districts(results.stream()
                        .map(DistrictResponse::summary)
                        .collect(Collectors.toList()))
                .total(results.size())
                .query(query)
                .build();
    }
    
    /**
     * Get districts in bounding box (for map viewport)
     */
    public List<DistrictResponse> getDistrictsInBounds(
            double minLat, double maxLat, double minLng, double maxLng) {
        return districtRepository.findInBoundingBox(minLat, maxLat, minLng, maxLng)
                .stream()
                .map(DistrictResponse::summary)
                .collect(Collectors.toList());
    }
    
    // ==================== Admin Operations ====================
    
    /**
     * Create district
     */
    @Transactional
    public DistrictResponse createDistrict(CreateDistrictRequest request) {
        if (districtRepository.existsByDistrictCode(request.getDistrictCode())) {
            throw new RuntimeException("District code already exists: " + request.getDistrictCode());
        }
        
        DistrictInfo district = new DistrictInfo();
        mapRequestToDistrict(request, district);
        district.setCreatedAt(LocalDateTime.now());
        district.setUpdatedAt(LocalDateTime.now());
        
        DistrictInfo saved = districtRepository.save(district);
        log.info("Created district: {} ({})", saved.getName(), saved.getId());
        
        return DistrictResponse.from(saved);
    }
    
    /**
     * Update district
     */
    @Transactional
    public DistrictResponse updateDistrict(String id, CreateDistrictRequest request) {
        DistrictInfo district = districtRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("District not found: " + id));
        
        mapRequestToDistrict(request, district);
        district.setUpdatedAt(LocalDateTime.now());
        
        DistrictInfo saved = districtRepository.save(district);
        log.info("Updated district: {} ({})", saved.getName(), saved.getId());
        
        return DistrictResponse.from(saved);
    }
    
    /**
     * Delete district (soft delete)
     */
    @Transactional
    public void deleteDistrict(String id) {
        DistrictInfo district = districtRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("District not found: " + id));
        
        district.setActive(false);
        district.setUpdatedAt(LocalDateTime.now());
        districtRepository.save(district);
        
        log.info("Deleted district: {} ({})", district.getName(), id);
    }
    
    /**
     * Add ODOP product to district
     */
    @Transactional
    public DistrictResponse addOdopProduct(String districtId, DistrictInfo.OdopProduct product) {
        DistrictInfo district = districtRepository.findById(districtId)
                .orElseThrow(() -> new RuntimeException("District not found"));
        
        if (district.getOdopProducts() == null) {
            district.setOdopProducts(new ArrayList<>());
        }
        district.getOdopProducts().add(product);
        district.setUpdatedAt(LocalDateTime.now());
        
        return DistrictResponse.from(districtRepository.save(district));
    }
    
    /**
     * Update statistics for district
     */
    @Transactional
    public void updateDistrictStatistics(String districtId, long artisans, long vendors, long products) {
        DistrictInfo district = districtRepository.findById(districtId).orElse(null);
        if (district != null) {
            district.setRegisteredArtisans(artisans);
            district.setActiveVendors(vendors);
            district.setTotalProducts(products);
            district.setUpdatedAt(LocalDateTime.now());
            districtRepository.save(district);
        }
    }
    
    // ==================== Statistics ====================
    
    /**
     * Get map statistics
     */
    public MapStatistics getMapStatistics() {
        return buildMapStatistics();
    }
    
    // ==================== Helper Methods ====================
    
    private StateResponse buildStateResponse(String stateCode, DistrictInfo.StateInfo stateInfo, 
            List<DistrictInfo> districts) {
        
        // Get top crafts from districts
        List<String> topCrafts = districts.stream()
                .filter(d -> d.getPrimaryCraft() != null && !d.getPrimaryCraft().isEmpty())
                .map(DistrictInfo::getPrimaryCraft)
                .distinct()
                .limit(5)
                .collect(Collectors.toList());
        
        return StateResponse.builder()
                .stateCode(stateCode)
                .name(stateInfo != null ? stateInfo.getName() : stateCode)
                .nameHindi(stateInfo != null ? stateInfo.getNameHindi() : null)
                .region(stateInfo != null ? stateInfo.getRegion().name() : null)
                .regionDisplayName(stateInfo != null ? stateInfo.getRegion().getDisplayName() : null)
                .mapColor(stateInfo != null ? stateInfo.getMapColor() : "#CCCCCC")
                .districtCount(districts.size())
                .totalProducts(districts.stream().mapToLong(DistrictInfo::getTotalProducts).sum())
                .totalArtisans(districts.stream().mapToLong(DistrictInfo::getRegisteredArtisans).sum())
                .topCrafts(topCrafts)
                .build();
    }
    
    private MapStatistics buildMapStatistics() {
        List<DistrictInfoRepository.CraftAggregation> topCrafts;
        try {
            topCrafts = districtRepository.getTopCrafts();
        } catch (Exception e) {
            topCrafts = Collections.emptyList();
        }
        
        return MapStatistics.builder()
                .totalStates(stateCache.size())
                .totalDistricts((int) districtRepository.countByActiveTrue())
                .totalProducts(districtRepository.findByActiveTrueOrderByDisplayPriorityDesc()
                        .stream().mapToLong(DistrictInfo::getTotalProducts).sum())
                .totalArtisans(districtRepository.findByActiveTrueOrderByDisplayPriorityDesc()
                        .stream().mapToLong(DistrictInfo::getRegisteredArtisans).sum())
                .giTaggedProducts((int) districtRepository.countDistrictsWithGiTags())
                .topCrafts(topCrafts.stream()
                        .map(c -> TopCraftResponse.builder()
                                .craftName(c.getId())
                                .districtCount(c.getDistrictCount())
                                .productCount(c.getProductCount())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
    
    private MapMarker buildMapMarker(DistrictInfo district) {
        return MapMarker.builder()
                .id(district.getId())
                .name(district.getName())
                .latitude(district.getLatitude())
                .longitude(district.getLongitude())
                .primaryCraft(district.getPrimaryCraft())
                .thumbnailUrl(district.getThumbnailUrl())
                .color(district.getMapTileColor())
                .productCount(district.getTotalProducts())
                .type(MarkerType.DISTRICT)
                .build();
    }
    
    private void mapRequestToDistrict(CreateDistrictRequest request, DistrictInfo district) {
        district.setDistrictCode(request.getDistrictCode());
        district.setName(request.getName());
        district.setNameHindi(request.getNameHindi());
        district.setNameLocal(request.getNameLocal());
        district.setStateCode(request.getStateCode());
        district.setStateName(request.getStateName());
        district.setStateNameHindi(request.getStateNameHindi());
        district.setDivision(request.getDivision());
        district.setLatitude(request.getLatitude());
        district.setLongitude(request.getLongitude());
        district.setPrimaryCraft(request.getPrimaryCraft());
        district.setFamousFor(request.getFamousFor());
        district.setHistoricalSignificance(request.getHistoricalSignificance());
        district.setCraftHistory(request.getCraftHistory());
        district.setHeroImageUrl(request.getHeroImageUrl());
        district.setThumbnailUrl(request.getThumbnailUrl());
        district.setBannerImageUrl(request.getBannerImageUrl());
        district.setVideoUrl(request.getVideoUrl());
        district.setMapTileColor(request.getMapTileColor());
        district.setActive(request.isActive());
        district.setFeatured(request.isFeatured());
        district.setDisplayPriority(request.getDisplayPriority());
        district.setSeoTitle(request.getSeoTitle());
        district.setSeoDescription(request.getSeoDescription());
        district.setSeoKeywords(request.getSeoKeywords());
        
        if (request.getRegion() != null) {
            district.setRegion(DistrictInfo.Region.valueOf(request.getRegion().toUpperCase()));
        }
    }
    
    private boolean hasOdopProducts(String stateCode) {
        // Check if state has any ODOP products even without district data
        // This can be expanded based on official ODOP data
        return true; // All states have potential ODOP products
    }
}
