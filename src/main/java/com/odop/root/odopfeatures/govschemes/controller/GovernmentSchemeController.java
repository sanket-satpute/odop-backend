package com.odop.root.odopfeatures.govschemes.controller;

import com.odop.root.odopfeatures.govschemes.dto.GovernmentSchemeDto.*;
import com.odop.root.odopfeatures.govschemes.model.GovernmentScheme.*;
import com.odop.root.odopfeatures.govschemes.service.GovernmentSchemeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for Government Schemes
 * 
 * Provides endpoints for:
 * - Public: Browse schemes, search, filter, scheme finder
 * - Admin: CRUD operations, add success stories, add FAQs
 */
@RestController
@RequestMapping("/odop/schemes")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class GovernmentSchemeController {
    
    private final GovernmentSchemeService schemeService;
    
    // ==================== Public Endpoints ====================
    
    /**
     * Get all active government schemes
     */
    @GetMapping
    public ResponseEntity<List<SchemeListItem>> getAllSchemes() {
        return ResponseEntity.ok(schemeService.getAllSchemes());
    }
    
    /**
     * Get featured schemes for homepage
     */
    @GetMapping("/featured")
    public ResponseEntity<List<SchemeListItem>> getFeaturedSchemes() {
        return ResponseEntity.ok(schemeService.getFeaturedSchemes());
    }
    
    /**
     * Get schemes overview with statistics
     */
    @GetMapping("/overview")
    public ResponseEntity<SchemesOverview> getOverview() {
        return ResponseEntity.ok(schemeService.getOverview());
    }
    
    /**
     * Get scheme by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<SchemeResponse> getSchemeById(@PathVariable String id) {
        return ResponseEntity.ok(schemeService.getSchemeById(id));
    }
    
    /**
     * Get scheme by slug
     */
    @GetMapping("/slug/{slug}")
    public ResponseEntity<SchemeResponse> getSchemeBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(schemeService.getSchemeBySlug(slug));
    }
    
    // ==================== Filter Endpoints ====================
    
    /**
     * Get available filters
     */
    @GetMapping("/filters")
    public ResponseEntity<SchemeFilters> getFilters() {
        return ResponseEntity.ok(schemeService.getFilters());
    }
    
    /**
     * Get schemes by type
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<List<SchemeListItem>> getSchemesByType(@PathVariable String type) {
        SchemeType schemeType = SchemeType.valueOf(type.toUpperCase());
        return ResponseEntity.ok(schemeService.getSchemesByType(schemeType));
    }
    
    /**
     * Get schemes by category
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<List<SchemeListItem>> getSchemesByCategory(@PathVariable String category) {
        SchemeCategory schemeCategory = SchemeCategory.valueOf(category.toUpperCase());
        return ResponseEntity.ok(schemeService.getSchemesByCategory(schemeCategory));
    }
    
    /**
     * Get schemes by target beneficiary
     */
    @GetMapping("/beneficiary/{beneficiary}")
    public ResponseEntity<List<SchemeListItem>> getSchemesByBeneficiary(@PathVariable String beneficiary) {
        TargetBeneficiary target = TargetBeneficiary.valueOf(beneficiary.toUpperCase());
        return ResponseEntity.ok(schemeService.getSchemesByBeneficiary(target));
    }
    
    /**
     * Get schemes by government level (CENTRAL/STATE/BOTH)
     */
    @GetMapping("/level/{level}")
    public ResponseEntity<List<SchemeListItem>> getSchemesByLevel(@PathVariable String level) {
        GovernmentLevel govLevel = GovernmentLevel.valueOf(level.toUpperCase());
        return ResponseEntity.ok(schemeService.getSchemesByLevel(govLevel));
    }
    
    /**
     * Get schemes applicable for a specific state
     */
    @GetMapping("/state/{stateCode}")
    public ResponseEntity<List<SchemeListItem>> getSchemesForState(@PathVariable String stateCode) {
        return ResponseEntity.ok(schemeService.getSchemesForState(stateCode));
    }
    
    // ==================== Special Filter Endpoints ====================
    
    /**
     * Get schemes currently open for applications
     */
    @GetMapping("/open")
    public ResponseEntity<List<SchemeListItem>> getOpenSchemes() {
        return ResponseEntity.ok(schemeService.getOpenSchemes());
    }
    
    /**
     * Get schemes with online application facility
     */
    @GetMapping("/online")
    public ResponseEntity<List<SchemeListItem>> getOnlineSchemes() {
        return ResponseEntity.ok(schemeService.getOnlineSchemes());
    }
    
    /**
     * Get collateral-free loan schemes
     */
    @GetMapping("/collateral-free")
    public ResponseEntity<List<SchemeListItem>> getCollateralFreeSchemes() {
        return ResponseEntity.ok(schemeService.getCollateralFreeSchemes());
    }
    
    /**
     * Get loan schemes
     */
    @GetMapping("/loans")
    public ResponseEntity<List<SchemeListItem>> getLoanSchemes() {
        return ResponseEntity.ok(schemeService.getLoanSchemes());
    }
    
    /**
     * Get grant and subsidy schemes
     */
    @GetMapping("/grants")
    public ResponseEntity<List<SchemeListItem>> getGrantSchemes() {
        return ResponseEntity.ok(schemeService.getGrantSchemes());
    }
    
    // ==================== Search & Finder ====================
    
    /**
     * Search schemes by text query
     */
    @GetMapping("/search")
    public ResponseEntity<List<SchemeListItem>> searchSchemes(@RequestParam String q) {
        return ResponseEntity.ok(schemeService.searchSchemes(q));
    }
    
    /**
     * Scheme finder - recommends schemes based on user criteria
     */
    @PostMapping("/finder")
    public ResponseEntity<SchemeFinderResponse> findSchemes(@RequestBody SchemeFinderRequest request) {
        return ResponseEntity.ok(schemeService.findSchemes(request));
    }
    
    // ==================== Analytics ====================
    
    /**
     * Track when user clicks apply button
     */
    @PostMapping("/{id}/track-apply")
    public ResponseEntity<Map<String, String>> trackApplicationClick(@PathVariable String id) {
        schemeService.trackApplicationClick(id);
        return ResponseEntity.ok(Map.of("message", "Tracked"));
    }
    
    // ==================== Admin Endpoints ====================
    
    /**
     * Create a new scheme (Admin only)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SchemeResponse> createScheme(@RequestBody CreateSchemeRequest request) {
        SchemeResponse response = schemeService.createScheme(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Update an existing scheme (Admin only)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SchemeResponse> updateScheme(
            @PathVariable String id,
            @RequestBody UpdateSchemeRequest request) {
        return ResponseEntity.ok(schemeService.updateScheme(id, request));
    }
    
    /**
     * Delete a scheme (Admin only)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteScheme(@PathVariable String id) {
        schemeService.deleteScheme(id);
        return ResponseEntity.ok(Map.of("message", "Scheme deleted successfully"));
    }
    
    /**
     * Toggle scheme active status (Admin only)
     */
    @PatchMapping("/{id}/toggle-active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SchemeResponse> toggleActive(@PathVariable String id) {
        return ResponseEntity.ok(schemeService.toggleSchemeActive(id));
    }
    
    /**
     * Add success story to scheme (Admin only)
     */
    @PostMapping("/{id}/success-stories")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SchemeResponse> addSuccessStory(
            @PathVariable String id,
            @RequestBody AddSuccessStoryRequest request) {
        return ResponseEntity.ok(schemeService.addSuccessStory(id, request));
    }
    
    /**
     * Add FAQ to scheme (Admin only)
     */
    @PostMapping("/{id}/faqs")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SchemeResponse> addFaq(
            @PathVariable String id,
            @RequestBody AddFaqRequest request) {
        return ResponseEntity.ok(schemeService.addFaq(id, request));
    }
}
