package com.odop.root.odopfeatures.crafts.controller;

import com.odop.root.odopfeatures.crafts.dto.CraftCategoryDto.*;
import com.odop.root.odopfeatures.crafts.service.CraftCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for Craft Categories
 */
@RestController
@RequestMapping("/odop/craft-categories")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CraftCategoryController {
    
    private final CraftCategoryService categoryService;
    
    // ==================== Public Endpoints ====================
    
    /**
     * Get all root categories
     */
    @GetMapping("/roots")
    public ResponseEntity<List<CraftCategoryResponse>> getRootCategories() {
        return ResponseEntity.ok(categoryService.getRootCategories());
    }
    
    /**
     * Get category tree (hierarchical)
     */
    @GetMapping("/tree")
    public ResponseEntity<CategoryTreeResponse> getCategoryTree() {
        return ResponseEntity.ok(categoryService.getCategoryTree());
    }
    
    /**
     * Get category by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<CraftCategoryResponse> getCategoryById(@PathVariable String id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }
    
    /**
     * Get category by slug
     */
    @GetMapping("/slug/{slug}")
    public ResponseEntity<CraftCategoryResponse> getCategoryBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(categoryService.getCategoryBySlug(slug));
    }
    
    /**
     * Get subcategories of a category
     */
    @GetMapping("/{parentId}/children")
    public ResponseEntity<List<CraftCategoryResponse>> getSubcategories(@PathVariable String parentId) {
        return ResponseEntity.ok(categoryService.getSubcategories(parentId));
    }
    
    /**
     * Get featured categories
     */
    @GetMapping("/featured")
    public ResponseEntity<List<CraftCategoryResponse>> getFeaturedCategories() {
        return ResponseEntity.ok(categoryService.getFeaturedCategories());
    }
    
    /**
     * Get categories by state
     */
    @GetMapping("/state/{state}")
    public ResponseEntity<List<CraftCategoryResponse>> getCategoriesByState(@PathVariable String state) {
        return ResponseEntity.ok(categoryService.getCategoriesByState(state));
    }
    
    /**
     * Get categories by GI tag
     */
    @GetMapping("/gi-tag/{giTag}")
    public ResponseEntity<List<CraftCategoryResponse>> getCategoriesByGiTag(@PathVariable String giTag) {
        return ResponseEntity.ok(categoryService.getCategoriesByGiTag(giTag));
    }
    
    /**
     * Get breadcrumb for category
     */
    @GetMapping("/{categoryId}/breadcrumb")
    public ResponseEntity<List<CraftCategoryResponse>> getBreadcrumb(@PathVariable String categoryId) {
        return ResponseEntity.ok(categoryService.getBreadcrumb(categoryId));
    }
    
    /**
     * Search categories
     */
    @GetMapping("/search")
    public ResponseEntity<List<CraftCategoryResponse>> searchCategories(
            @RequestParam String q) {
        return ResponseEntity.ok(categoryService.searchCategories(q));
    }
    
    /**
     * Get filter options for category
     */
    @GetMapping("/{categoryId}/filters")
    public ResponseEntity<CategoryFilterResponse> getCategoryFilters(@PathVariable String categoryId) {
        return ResponseEntity.ok(categoryService.getCategoryFilters(categoryId));
    }
    
    /**
     * Get category statistics (public summary)
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getCategoryStatistics() {
        return ResponseEntity.ok(categoryService.getCategoryStatistics());
    }
    
    // ==================== Admin Endpoints ====================
    
    /**
     * Create new category (Admin only)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CraftCategoryResponse> createCategory(
            @RequestBody CreateCraftCategoryRequest request) {
        return ResponseEntity.ok(categoryService.createCategory(request));
    }
    
    /**
     * Update category (Admin only)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CraftCategoryResponse> updateCategory(
            @PathVariable String id,
            @RequestBody CreateCraftCategoryRequest request) {
        return ResponseEntity.ok(categoryService.updateCategory(id, request));
    }
    
    /**
     * Delete category (Admin only)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCategory(@PathVariable String id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok().build();
    }
    
    /**
     * Update product count for category (Admin/System)
     */
    @PatchMapping("/{categoryId}/product-count")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateProductCount(
            @PathVariable String categoryId,
            @RequestParam long count) {
        categoryService.updateProductCount(categoryId, count);
        return ResponseEntity.ok().build();
    }
    
    /**
     * Update artisan count for category (Admin/System)
     */
    @PatchMapping("/{categoryId}/artisan-count")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateArtisanCount(
            @PathVariable String categoryId,
            @RequestParam long count) {
        categoryService.updateArtisanCount(categoryId, count);
        return ResponseEntity.ok().build();
    }
}
