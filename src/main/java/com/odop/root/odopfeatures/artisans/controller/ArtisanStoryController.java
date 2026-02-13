package com.odop.root.odopfeatures.artisans.controller;

import com.odop.root.odopfeatures.artisans.dto.ArtisanStoryDto.*;
import com.odop.root.odopfeatures.artisans.service.ArtisanStoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for Artisan Stories
 * 
 * Provides endpoints for:
 * - Public: Browse artisan stories, search, filter
 * - Admin: CRUD operations, publish, verify
 */
@RestController
@RequestMapping("/odop/artisans")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ArtisanStoryController {
    
    private final ArtisanStoryService artisanService;
    
    // ==================== Public Endpoints ====================
    
    /**
     * Get all published artisan stories
     */
    @GetMapping
    public ResponseEntity<List<ArtisanListItem>> getAllStories() {
        return ResponseEntity.ok(artisanService.getAllPublishedStories());
    }
    
    /**
     * Get featured artisan stories
     */
    @GetMapping("/featured")
    public ResponseEntity<List<ArtisanListItem>> getFeaturedStories() {
        return ResponseEntity.ok(artisanService.getFeaturedStories());
    }
    
    /**
     * Get artisan stories overview with statistics
     */
    @GetMapping("/overview")
    public ResponseEntity<ArtisanStoriesOverview> getOverview() {
        return ResponseEntity.ok(artisanService.getOverview());
    }
    
    /**
     * Get artisan story by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ArtisanStoryResponse> getStoryById(@PathVariable String id) {
        return ResponseEntity.ok(artisanService.getStoryById(id));
    }
    
    /**
     * Get artisan story by slug
     */
    @GetMapping("/slug/{slug}")
    public ResponseEntity<ArtisanStoryResponse> getStoryBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(artisanService.getStoryBySlug(slug));
    }
    
    // ==================== Filter Endpoints ====================
    
    /**
     * Get available filters
     */
    @GetMapping("/filters")
    public ResponseEntity<ArtisanFilters> getFilters() {
        return ResponseEntity.ok(artisanService.getFilters());
    }
    
    /**
     * Get artisans by state
     */
    @GetMapping("/state/{stateCode}")
    public ResponseEntity<List<ArtisanListItem>> getStoriesByState(@PathVariable String stateCode) {
        return ResponseEntity.ok(artisanService.getStoriesByState(stateCode));
    }
    
    /**
     * Get artisans by craft
     */
    @GetMapping("/craft/{craft}")
    public ResponseEntity<List<ArtisanListItem>> getStoriesByCraft(@PathVariable String craft) {
        return ResponseEntity.ok(artisanService.getStoriesByCraft(craft));
    }
    
    /**
     * Get national award winners
     */
    @GetMapping("/national-awardees")
    public ResponseEntity<List<ArtisanListItem>> getNationalAwardees() {
        return ResponseEntity.ok(artisanService.getNationalAwardees());
    }
    
    /**
     * Get state award winners
     */
    @GetMapping("/state-awardees")
    public ResponseEntity<List<ArtisanListItem>> getStateAwardees() {
        return ResponseEntity.ok(artisanService.getStateAwardees());
    }
    
    /**
     * Get GI tag holders
     */
    @GetMapping("/gi-tag-holders")
    public ResponseEntity<List<ArtisanListItem>> getGiTagHolders() {
        return ResponseEntity.ok(artisanService.getGiTagHolders());
    }
    
    /**
     * Get artisans available for workshops
     */
    @GetMapping("/workshops-available")
    public ResponseEntity<List<ArtisanListItem>> getAvailableForWorkshops() {
        return ResponseEntity.ok(artisanService.getAvailableForWorkshops());
    }
    
    /**
     * Get artisans available for commissions
     */
    @GetMapping("/commissions-available")
    public ResponseEntity<List<ArtisanListItem>> getAvailableForCommissions() {
        return ResponseEntity.ok(artisanService.getAvailableForCommissions());
    }
    
    /**
     * Get experienced artisans (min years)
     */
    @GetMapping("/experienced")
    public ResponseEntity<List<ArtisanListItem>> getExperiencedArtisans(
            @RequestParam(defaultValue = "20") int minYears) {
        return ResponseEntity.ok(artisanService.getExperiencedArtisans(minYears));
    }
    
    /**
     * Get multi-generation artisans
     */
    @GetMapping("/multi-generation")
    public ResponseEntity<List<ArtisanListItem>> getMultiGenerationArtisans(
            @RequestParam(defaultValue = "3") int minGenerations) {
        return ResponseEntity.ok(artisanService.getMultiGenerationArtisans(minGenerations));
    }
    
    // ==================== Search ====================
    
    /**
     * Search artisan stories
     */
    @GetMapping("/search")
    public ResponseEntity<List<ArtisanListItem>> searchStories(@RequestParam String q) {
        return ResponseEntity.ok(artisanService.searchStories(q));
    }
    
    // ==================== Map Data ====================
    
    /**
     * Get map markers for all artisans
     */
    @GetMapping("/map/markers")
    public ResponseEntity<List<ArtisanMapMarker>> getMapMarkers() {
        return ResponseEntity.ok(artisanService.getMapMarkers());
    }
    
    /**
     * Get map markers within bounds
     */
    @GetMapping("/map/markers/bounds")
    public ResponseEntity<List<ArtisanMapMarker>> getMapMarkersInBounds(
            @RequestParam Double minLat,
            @RequestParam Double minLng,
            @RequestParam Double maxLat,
            @RequestParam Double maxLng) {
        return ResponseEntity.ok(artisanService.getMapMarkersInBounds(minLat, minLng, maxLat, maxLng));
    }
    
    // ==================== Share Tracking ====================
    
    /**
     * Track story share
     */
    @PostMapping("/{id}/share")
    public ResponseEntity<Map<String, String>> trackShare(@PathVariable String id) {
        artisanService.trackShare(id);
        return ResponseEntity.ok(Map.of("message", "Share tracked"));
    }
    
    // ==================== Admin Endpoints ====================
    
    /**
     * Get draft stories (Admin only)
     */
    @GetMapping("/admin/drafts")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ArtisanListItem>> getDraftStories() {
        return ResponseEntity.ok(artisanService.getDraftStories());
    }
    
    /**
     * Get pending review stories (Admin only)
     */
    @GetMapping("/admin/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ArtisanListItem>> getPendingReviewStories() {
        return ResponseEntity.ok(artisanService.getPendingReviewStories());
    }
    
    /**
     * Create a new artisan story (Admin only)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ArtisanStoryResponse> createStory(@RequestBody CreateArtisanStoryRequest request) {
        ArtisanStoryResponse response = artisanService.createStory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Update an artisan story (Admin only)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ArtisanStoryResponse> updateStory(
            @PathVariable String id,
            @RequestBody UpdateArtisanStoryRequest request) {
        return ResponseEntity.ok(artisanService.updateStory(id, request));
    }
    
    /**
     * Publish an artisan story (Admin only)
     */
    @PatchMapping("/{id}/publish")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ArtisanStoryResponse> publishStory(@PathVariable String id) {
        return ResponseEntity.ok(artisanService.publishStory(id));
    }
    
    /**
     * Archive an artisan story (Admin only)
     */
    @PatchMapping("/{id}/archive")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ArtisanStoryResponse> archiveStory(@PathVariable String id) {
        return ResponseEntity.ok(artisanService.archiveStory(id));
    }
    
    /**
     * Toggle featured status (Admin only)
     */
    @PatchMapping("/{id}/toggle-featured")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ArtisanStoryResponse> toggleFeatured(@PathVariable String id) {
        return ResponseEntity.ok(artisanService.toggleFeatured(id));
    }
    
    /**
     * Verify an artisan story (Admin only)
     */
    @PatchMapping("/{id}/verify")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ArtisanStoryResponse> verifyStory(@PathVariable String id) {
        return ResponseEntity.ok(artisanService.verifyStory(id));
    }
    
    /**
     * Delete an artisan story (Admin only)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteStory(@PathVariable String id) {
        artisanService.deleteStory(id);
        return ResponseEntity.ok(Map.of("message", "Story deleted successfully"));
    }
    
    /**
     * Add media to story gallery (Admin only)
     */
    @PostMapping("/{id}/media")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ArtisanStoryResponse> addMedia(
            @PathVariable String id,
            @RequestBody AddMediaRequest request) {
        return ResponseEntity.ok(artisanService.addMedia(id, request));
    }
    
    /**
     * Add video to story (Admin only)
     */
    @PostMapping("/{id}/videos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ArtisanStoryResponse> addVideo(
            @PathVariable String id,
            @RequestBody AddVideoRequest request) {
        return ResponseEntity.ok(artisanService.addVideo(id, request));
    }
    
    /**
     * Add award to story (Admin only)
     */
    @PostMapping("/{id}/awards")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ArtisanStoryResponse> addAward(
            @PathVariable String id,
            @RequestBody AddAwardRequest request) {
        return ResponseEntity.ok(artisanService.addAward(id, request));
    }
}
