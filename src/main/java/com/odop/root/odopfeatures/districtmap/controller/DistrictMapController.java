package com.odop.root.odopfeatures.districtmap.controller;

import com.odop.root.odopfeatures.districtmap.dto.DistrictMapDto.*;
import com.odop.root.odopfeatures.districtmap.model.DistrictInfo;
import com.odop.root.odopfeatures.districtmap.service.DistrictMapService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for District Map Browse Feature
 */
@RestController
@RequestMapping("/odop/district-map")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DistrictMapController {
    
    private final DistrictMapService districtMapService;
    
    // ==================== Map Data Endpoints ====================
    
    /**
     * Get complete map data including states and featured districts
     */
    @GetMapping("/data")
    public ResponseEntity<MapDataResponse> getMapData() {
        return ResponseEntity.ok(districtMapService.getMapData());
    }
    
    /**
     * Get map markers for visualization
     */
    @GetMapping("/markers")
    public ResponseEntity<List<MapMarker>> getMapMarkers() {
        return ResponseEntity.ok(districtMapService.getMapMarkers());
    }
    
    /**
     * Get map statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<MapStatistics> getMapStatistics() {
        return ResponseEntity.ok(districtMapService.getMapStatistics());
    }
    
    // ==================== State Endpoints ====================
    
    /**
     * Get all states with summary
     */
    @GetMapping("/states")
    public ResponseEntity<List<StateResponse>> getAllStates() {
        return ResponseEntity.ok(districtMapService.getAllStates());
    }
    
    /**
     * Get state details by code
     */
    @GetMapping("/states/{stateCode}")
    public ResponseEntity<StateResponse> getStateByCode(@PathVariable String stateCode) {
        return ResponseEntity.ok(districtMapService.getStateByCode(stateCode));
    }
    
    /**
     * Get districts of a state
     */
    @GetMapping("/states/{stateCode}/districts")
    public ResponseEntity<List<DistrictResponse>> getDistrictsByState(@PathVariable String stateCode) {
        return ResponseEntity.ok(districtMapService.getDistrictsByState(stateCode));
    }
    
    // ==================== Region Endpoints ====================
    
    /**
     * Get all regions with summary
     */
    @GetMapping("/regions")
    public ResponseEntity<List<RegionResponse>> getAllRegions() {
        return ResponseEntity.ok(districtMapService.getAllRegions());
    }
    
    /**
     * Get districts by region
     */
    @GetMapping("/regions/{region}/districts")
    public ResponseEntity<List<DistrictResponse>> getDistrictsByRegion(@PathVariable String region) {
        return ResponseEntity.ok(districtMapService.getDistrictsByRegion(region));
    }
    
    // ==================== District Endpoints ====================
    
    /**
     * Get district by ID
     */
    @GetMapping("/districts/{id}")
    public ResponseEntity<DistrictResponse> getDistrictById(@PathVariable String id) {
        return ResponseEntity.ok(districtMapService.getDistrictById(id));
    }
    
    /**
     * Get district by state and district code
     */
    @GetMapping("/districts/{stateCode}/{districtCode}")
    public ResponseEntity<DistrictResponse> getDistrictByCode(
            @PathVariable String stateCode,
            @PathVariable String districtCode) {
        return ResponseEntity.ok(districtMapService.getDistrictByCode(stateCode, districtCode));
    }
    
    /**
     * Get featured districts
     */
    @GetMapping("/districts/featured")
    public ResponseEntity<List<DistrictResponse>> getFeaturedDistricts() {
        return ResponseEntity.ok(districtMapService.getFeaturedDistricts());
    }
    
    /**
     * Get GI tagged districts
     */
    @GetMapping("/districts/gi-tagged")
    public ResponseEntity<List<DistrictResponse>> getGiTaggedDistricts() {
        return ResponseEntity.ok(districtMapService.getGiTaggedDistricts());
    }
    
    /**
     * Get districts by craft
     */
    @GetMapping("/districts/craft/{craft}")
    public ResponseEntity<List<DistrictResponse>> getDistrictsByCraft(@PathVariable String craft) {
        return ResponseEntity.ok(districtMapService.getDistrictsByCraft(craft));
    }
    
    /**
     * Get districts by craft category
     */
    @GetMapping("/districts/category/{categoryId}")
    public ResponseEntity<List<DistrictResponse>> getDistrictsByCraftCategory(@PathVariable String categoryId) {
        return ResponseEntity.ok(districtMapService.getDistrictsByCraftCategory(categoryId));
    }
    
    /**
     * Search districts
     */
    @GetMapping("/districts/search")
    public ResponseEntity<DistrictSearchResponse> searchDistricts(@RequestParam String q) {
        return ResponseEntity.ok(districtMapService.searchDistricts(q));
    }
    
    /**
     * Get districts in bounding box (for map viewport)
     */
    @GetMapping("/districts/bounds")
    public ResponseEntity<List<DistrictResponse>> getDistrictsInBounds(
            @RequestParam double minLat,
            @RequestParam double maxLat,
            @RequestParam double minLng,
            @RequestParam double maxLng) {
        return ResponseEntity.ok(districtMapService.getDistrictsInBounds(minLat, maxLat, minLng, maxLng));
    }
    
    // ==================== Admin Endpoints ====================
    
    /**
     * Create district (Admin only)
     */
    @PostMapping("/districts")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DistrictResponse> createDistrict(@RequestBody CreateDistrictRequest request) {
        return ResponseEntity.ok(districtMapService.createDistrict(request));
    }
    
    /**
     * Update district (Admin only)
     */
    @PutMapping("/districts/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DistrictResponse> updateDistrict(
            @PathVariable String id,
            @RequestBody CreateDistrictRequest request) {
        return ResponseEntity.ok(districtMapService.updateDistrict(id, request));
    }
    
    /**
     * Delete district (Admin only)
     */
    @DeleteMapping("/districts/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteDistrict(@PathVariable String id) {
        districtMapService.deleteDistrict(id);
        return ResponseEntity.ok().build();
    }
    
    /**
     * Add ODOP product to district (Admin only)
     */
    @PostMapping("/districts/{id}/odop-products")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DistrictResponse> addOdopProduct(
            @PathVariable String id,
            @RequestBody DistrictInfo.OdopProduct product) {
        return ResponseEntity.ok(districtMapService.addOdopProduct(id, product));
    }
    
    /**
     * Update district statistics (Admin only)
     */
    @PatchMapping("/districts/{id}/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateStatistics(
            @PathVariable String id,
            @RequestParam long artisans,
            @RequestParam long vendors,
            @RequestParam long products) {
        districtMapService.updateDistrictStatistics(id, artisans, vendors, products);
        return ResponseEntity.ok().build();
    }
}
