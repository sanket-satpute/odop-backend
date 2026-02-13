package com.odop.root.odopfeatures.festivals.controller;

import com.odop.root.odopfeatures.festivals.dto.FestivalDto.*;
import com.odop.root.odopfeatures.festivals.model.FestivalCollection;
import com.odop.root.odopfeatures.festivals.service.FestivalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for Festival Collections
 */
@RestController
@RequestMapping("/odop/festivals")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class FestivalController {
    
    private final FestivalService festivalService;
    
    // ==================== Public Endpoints ====================
    
    /**
     * Get all active festivals
     */
    @GetMapping
    public ResponseEntity<FestivalListResponse> getAllFestivals() {
        return ResponseEntity.ok(festivalService.getAllFestivals());
    }
    
    /**
     * Get featured festivals for homepage
     */
    @GetMapping("/featured")
    public ResponseEntity<List<FestivalCollectionResponse>> getFeaturedFestivals() {
        return ResponseEntity.ok(festivalService.getFeaturedFestivals());
    }
    
    /**
     * Get live festivals (currently active)
     */
    @GetMapping("/live")
    public ResponseEntity<List<FestivalCollectionResponse>> getLiveFestivals() {
        return ResponseEntity.ok(festivalService.getLiveFestivals());
    }
    
    /**
     * Get upcoming festivals
     */
    @GetMapping("/upcoming")
    public ResponseEntity<List<FestivalCollectionResponse>> getUpcomingFestivals() {
        return ResponseEntity.ok(festivalService.getUpcomingFestivals());
    }
    
    /**
     * Get festival by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<FestivalCollectionResponse> getFestivalById(@PathVariable String id) {
        return ResponseEntity.ok(festivalService.getFestivalById(id));
    }
    
    /**
     * Get festival by slug
     */
    @GetMapping("/slug/{slug}")
    public ResponseEntity<FestivalCollectionResponse> getFestivalBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(festivalService.getFestivalBySlug(slug));
    }
    
    /**
     * Get festivals by type
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<List<FestivalCollectionResponse>> getFestivalsByType(@PathVariable String type) {
        return ResponseEntity.ok(festivalService.getFestivalsByType(type));
    }
    
    /**
     * Get festivals by category
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<List<FestivalCollectionResponse>> getFestivalsByCategory(@PathVariable String category) {
        return ResponseEntity.ok(festivalService.getFestivalsByCategory(category));
    }
    
    /**
     * Get festivals by state
     */
    @GetMapping("/state/{state}")
    public ResponseEntity<List<FestivalCollectionResponse>> getFestivalsByState(@PathVariable String state) {
        return ResponseEntity.ok(festivalService.getFestivalsByState(state));
    }
    
    /**
     * Get festivals by year
     */
    @GetMapping("/year/{year}")
    public ResponseEntity<List<FestivalCollectionResponse>> getFestivalsByYear(@PathVariable int year) {
        return ResponseEntity.ok(festivalService.getFestivalsByYear(year));
    }
    
    /**
     * Search festivals
     */
    @GetMapping("/search")
    public ResponseEntity<List<FestivalCollectionResponse>> searchFestivals(@RequestParam String q) {
        return ResponseEntity.ok(festivalService.searchFestivals(q));
    }
    
    /**
     * Get festival calendar
     */
    @GetMapping("/calendar")
    public ResponseEntity<Map<String, List<FestivalCollectionResponse>>> getFestivalCalendar() {
        return ResponseEntity.ok(festivalService.getFestivalCalendar());
    }
    
    /**
     * Get gift guide for a festival
     */
    @GetMapping("/{festivalId}/gift-guide")
    public ResponseEntity<List<GiftSuggestionResponse>> getGiftGuide(@PathVariable String festivalId) {
        return ResponseEntity.ok(festivalService.getGiftGuide(festivalId));
    }
    
    /**
     * Get sections for a festival
     */
    @GetMapping("/{festivalId}/sections")
    public ResponseEntity<List<CollectionSectionResponse>> getFestivalSections(@PathVariable String festivalId) {
        return ResponseEntity.ok(festivalService.getFestivalSections(festivalId));
    }
    
    /**
     * Get festival statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getFestivalStatistics() {
        return ResponseEntity.ok(festivalService.getFestivalStatistics());
    }
    
    // ==================== Admin Endpoints ====================
    
    /**
     * Create new festival (Admin only)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FestivalCollectionResponse> createFestival(
            @RequestBody CreateFestivalRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(festivalService.createFestival(request, userDetails.getUsername()));
    }
    
    /**
     * Update festival (Admin only)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FestivalCollectionResponse> updateFestival(
            @PathVariable String id,
            @RequestBody CreateFestivalRequest request) {
        return ResponseEntity.ok(festivalService.updateFestival(id, request));
    }
    
    /**
     * Delete festival (Admin only)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteFestival(@PathVariable String id) {
        festivalService.deleteFestival(id);
        return ResponseEntity.ok().build();
    }
    
    /**
     * Update featured products (Admin only)
     */
    @PatchMapping("/{id}/products")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FestivalCollectionResponse> updateFeaturedProducts(
            @PathVariable String id,
            @RequestBody List<String> productIds) {
        return ResponseEntity.ok(festivalService.updateFeaturedProducts(id, productIds));
    }
    
    /**
     * Add section to festival (Admin only)
     */
    @PostMapping("/{id}/sections")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FestivalCollectionResponse> addSection(
            @PathVariable String id,
            @RequestBody FestivalCollection.CollectionSection section) {
        return ResponseEntity.ok(festivalService.addSection(id, section));
    }
    
    /**
     * Update discount info (Admin only)
     */
    @PatchMapping("/{id}/discount")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FestivalCollectionResponse> updateDiscount(
            @PathVariable String id,
            @RequestBody FestivalCollection.DiscountInfo discount) {
        return ResponseEntity.ok(festivalService.updateDiscount(id, discount));
    }
}
