package com.odop.root.odopfeatures.crafts.service;

import com.odop.root.odopfeatures.crafts.dto.CraftCategoryDto.*;
import com.odop.root.odopfeatures.crafts.model.CraftCategory;
import com.odop.root.odopfeatures.crafts.repository.CraftCategoryRepository;
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
 * Service for Craft Categories management
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CraftCategoryService {
    
    private final CraftCategoryRepository categoryRepository;
    
    // ==================== Initialization ====================
    
    @EventListener(ApplicationReadyEvent.class)
    public void initializeDefaultCategories() {
        if (categoryRepository.count() == 0) {
            log.info("Initializing default craft categories...");
            List<CraftCategory> defaultCategories = CraftCategory.getDefaultCategories();
            categoryRepository.saveAll(defaultCategories);
            log.info("Created {} default craft categories", defaultCategories.size());
        }
    }
    
    // ==================== Read Operations ====================
    
    /**
     * Get all root categories (level 0)
     */
    public List<CraftCategoryResponse> getRootCategories() {
        return categoryRepository.findByLevelAndActiveTrueOrderByDisplayOrderAsc(0)
                .stream()
                .map(CraftCategoryResponse::from)
                .collect(Collectors.toList());
    }
    
    /**
     * Get category tree (hierarchical structure)
     */
    public CategoryTreeResponse getCategoryTree() {
        List<CraftCategory> allCategories = categoryRepository.findByActiveTrueOrderByDisplayOrderAsc();
        
        // Build tree structure
        Map<String, CraftCategoryResponse> categoryMap = new HashMap<>();
        List<CraftCategoryResponse> rootCategories = new ArrayList<>();
        
        // First pass: create all response objects
        for (CraftCategory category : allCategories) {
            CraftCategoryResponse response = CraftCategoryResponse.from(category);
            response.setChildren(new ArrayList<>());
            categoryMap.put(category.getId(), response);
        }
        
        // Second pass: build tree
        for (CraftCategory category : allCategories) {
            CraftCategoryResponse response = categoryMap.get(category.getId());
            if (category.getParentId() == null || category.getParentId().isEmpty()) {
                rootCategories.add(response);
            } else {
                CraftCategoryResponse parent = categoryMap.get(category.getParentId());
                if (parent != null) {
                    parent.getChildren().add(response);
                }
            }
        }
        
        // Sort children
        sortCategoriesRecursively(rootCategories);
        
        return CategoryTreeResponse.builder()
                .categories(rootCategories)
                .totalCategories(allCategories.size())
                .mainCategories((int) allCategories.stream().filter(c -> c.getLevel() == 0).count())
                .subCategories((int) allCategories.stream().filter(c -> c.getLevel() > 0).count())
                .build();
    }
    
    private void sortCategoriesRecursively(List<CraftCategoryResponse> categories) {
        categories.sort(Comparator.comparingInt(CraftCategoryResponse::getDisplayOrder));
        for (CraftCategoryResponse category : categories) {
            if (category.getChildren() != null && !category.getChildren().isEmpty()) {
                sortCategoriesRecursively(category.getChildren());
            }
        }
    }
    
    /**
     * Get category by ID
     */
    public CraftCategoryResponse getCategoryById(String id) {
        CraftCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found: " + id));
        return CraftCategoryResponse.from(category);
    }
    
    /**
     * Get category by slug
     */
    public CraftCategoryResponse getCategoryBySlug(String slug) {
        CraftCategory category = categoryRepository.findBySlugAndActiveTrue(slug)
                .orElseThrow(() -> new RuntimeException("Category not found: " + slug));
        return CraftCategoryResponse.from(category);
    }
    
    /**
     * Get subcategories of a category
     */
    public List<CraftCategoryResponse> getSubcategories(String parentId) {
        return categoryRepository.findByParentIdAndActiveTrueOrderByDisplayOrderAsc(parentId)
                .stream()
                .map(CraftCategoryResponse::from)
                .collect(Collectors.toList());
    }
    
    /**
     * Get featured categories
     */
    public List<CraftCategoryResponse> getFeaturedCategories() {
        return categoryRepository.findByFeaturedTrueAndActiveTrueOrderByDisplayOrderAsc()
                .stream()
                .map(CraftCategoryResponse::from)
                .collect(Collectors.toList());
    }
    
    /**
     * Get categories by state
     */
    public List<CraftCategoryResponse> getCategoriesByState(String state) {
        return categoryRepository.findByMajorState(state)
                .stream()
                .map(CraftCategoryResponse::from)
                .collect(Collectors.toList());
    }
    
    /**
     * Get categories by GI tag
     */
    public List<CraftCategoryResponse> getCategoriesByGiTag(String giTag) {
        return categoryRepository.findByGiTag(giTag)
                .stream()
                .map(CraftCategoryResponse::from)
                .collect(Collectors.toList());
    }
    
    /**
     * Get breadcrumb path for a category
     */
    public List<CraftCategoryResponse> getBreadcrumb(String categoryId) {
        List<CraftCategoryResponse> breadcrumb = new ArrayList<>();
        CraftCategory current = categoryRepository.findById(categoryId).orElse(null);
        
        while (current != null) {
            breadcrumb.add(0, CraftCategoryResponse.simpleFrom(current));
            if (current.getParentId() != null && !current.getParentId().isEmpty()) {
                current = categoryRepository.findById(current.getParentId()).orElse(null);
            } else {
                break;
            }
        }
        
        return breadcrumb;
    }
    
    /**
     * Search categories by name
     */
    public List<CraftCategoryResponse> searchCategories(String searchTerm) {
        return categoryRepository.searchByName(searchTerm)
                .stream()
                .map(CraftCategoryResponse::from)
                .collect(Collectors.toList());
    }
    
    /**
     * Get filter options for a category
     */
    public CategoryFilterResponse getCategoryFilters(String categoryId) {
        CraftCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        
        List<FilterOption> filters = new ArrayList<>();
        
        if (category.getAttributes() != null) {
            for (CraftCategory.CategoryAttribute attr : category.getAttributes()) {
                if (attr.isFilterable()) {
                    List<FilterValue> values = new ArrayList<>();
                    
                    if (attr.getOptions() != null) {
                        for (String option : attr.getOptions()) {
                            // In real implementation, count would come from product aggregation
                            values.add(FilterValue.builder()
                                    .value(option)
                                    .label(option)
                                    .count(0)
                                    .build());
                        }
                    }
                    
                    filters.add(FilterOption.builder()
                            .name(attr.getName())
                            .label(attr.getLabel())
                            .type(attr.getType().name())
                            .values(values)
                            .build());
                }
            }
        }
        
        // Add common filters
        filters.add(FilterOption.builder()
                .name("price")
                .label("Price Range")
                .type("RANGE")
                .values(Arrays.asList(
                        FilterValue.builder().value("0-500").label("Under ₹500").build(),
                        FilterValue.builder().value("500-1000").label("₹500 - ₹1000").build(),
                        FilterValue.builder().value("1000-2500").label("₹1000 - ₹2500").build(),
                        FilterValue.builder().value("2500-5000").label("₹2500 - ₹5000").build(),
                        FilterValue.builder().value("5000+").label("Above ₹5000").build()
                ))
                .build());
        
        filters.add(FilterOption.builder()
                .name("giTag")
                .label("GI Tagged")
                .type("BOOLEAN")
                .values(Arrays.asList(
                        FilterValue.builder().value("true").label("GI Tagged Products").build(),
                        FilterValue.builder().value("false").label("All Products").build()
                ))
                .build());
        
        return CategoryFilterResponse.builder()
                .categoryId(categoryId)
                .categoryName(category.getName())
                .filters(filters)
                .build();
    }
    
    // ==================== Write Operations ====================
    
    /**
     * Create a new category
     */
    @Transactional
    public CraftCategoryResponse createCategory(CreateCraftCategoryRequest request) {
        // Validate unique slug
        if (categoryRepository.existsBySlug(request.getSlug())) {
            throw new RuntimeException("Category with slug already exists: " + request.getSlug());
        }
        
        CraftCategory category = new CraftCategory();
        category.setSlug(request.getSlug());
        category.setName(request.getName());
        category.setNameHindi(request.getNameHindi());
        category.setDescription(request.getDescription());
        category.setDescriptionHindi(request.getDescriptionHindi());
        category.setImageUrl(request.getImageUrl());
        category.setIconName(request.getIconName());
        category.setBannerImageUrl(request.getBannerImageUrl());
        category.setThemeColor(request.getThemeColor());
        category.setRelatedGiTags(request.getRelatedGiTags());
        category.setMajorStates(request.getMajorStates());
        category.setFamousDistricts(request.getFamousDistricts());
        category.setActive(request.isActive());
        category.setFeatured(request.isFeatured());
        category.setDisplayOrder(request.getDisplayOrder());
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());
        
        // Handle parent relationship
        if (request.getParentId() != null && !request.getParentId().isEmpty()) {
            CraftCategory parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new RuntimeException("Parent category not found"));
            
            category.setParentId(parent.getId());
            category.setLevel(parent.getLevel() + 1);
            
            // Build ancestor path
            String ancestorPath = parent.getAncestorPath() != null ? 
                    parent.getAncestorPath() + "/" + parent.getId() : parent.getId();
            category.setAncestorPath(ancestorPath);
        } else {
            category.setLevel(0);
        }
        
        CraftCategory saved = categoryRepository.save(category);
        log.info("Created new category: {} ({})", saved.getName(), saved.getId());
        
        return CraftCategoryResponse.from(saved);
    }
    
    /**
     * Update category
     */
    @Transactional
    public CraftCategoryResponse updateCategory(String id, CreateCraftCategoryRequest request) {
        CraftCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found: " + id));
        
        // Check slug uniqueness if changed
        if (!category.getSlug().equals(request.getSlug()) && 
                categoryRepository.existsBySlug(request.getSlug())) {
            throw new RuntimeException("Category with slug already exists: " + request.getSlug());
        }
        
        category.setSlug(request.getSlug());
        category.setName(request.getName());
        category.setNameHindi(request.getNameHindi());
        category.setDescription(request.getDescription());
        category.setDescriptionHindi(request.getDescriptionHindi());
        category.setImageUrl(request.getImageUrl());
        category.setIconName(request.getIconName());
        category.setBannerImageUrl(request.getBannerImageUrl());
        category.setThemeColor(request.getThemeColor());
        category.setRelatedGiTags(request.getRelatedGiTags());
        category.setMajorStates(request.getMajorStates());
        category.setFamousDistricts(request.getFamousDistricts());
        category.setActive(request.isActive());
        category.setFeatured(request.isFeatured());
        category.setDisplayOrder(request.getDisplayOrder());
        category.setUpdatedAt(LocalDateTime.now());
        
        CraftCategory saved = categoryRepository.save(category);
        log.info("Updated category: {} ({})", saved.getName(), saved.getId());
        
        return CraftCategoryResponse.from(saved);
    }
    
    /**
     * Delete category (soft delete)
     */
    @Transactional
    public void deleteCategory(String id) {
        CraftCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found: " + id));
        
        // Check if has children
        long childCount = categoryRepository.countByParentId(id);
        if (childCount > 0) {
            throw new RuntimeException("Cannot delete category with subcategories. Delete children first.");
        }
        
        // Soft delete
        category.setActive(false);
        category.setUpdatedAt(LocalDateTime.now());
        categoryRepository.save(category);
        
        log.info("Deleted category: {} ({})", category.getName(), id);
    }
    
    /**
     * Update product count for category
     */
    @Transactional
    public void updateProductCount(String categoryId, long count) {
        CraftCategory category = categoryRepository.findById(categoryId).orElse(null);
        if (category != null) {
            category.setProductCount(count);
            category.setUpdatedAt(LocalDateTime.now());
            categoryRepository.save(category);
        }
    }
    
    /**
     * Update artisan count for category
     */
    @Transactional
    public void updateArtisanCount(String categoryId, long count) {
        CraftCategory category = categoryRepository.findById(categoryId).orElse(null);
        if (category != null) {
            category.setArtisanCount(count);
            category.setUpdatedAt(LocalDateTime.now());
            categoryRepository.save(category);
        }
    }
    
    // ==================== Statistics ====================
    
    /**
     * Get category statistics
     */
    public Map<String, Object> getCategoryStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("totalCategories", categoryRepository.countByActiveTrue());
        stats.put("rootCategories", categoryRepository.countRootCategories());
        stats.put("level1Categories", categoryRepository.countByLevel(1));
        stats.put("level2Categories", categoryRepository.countByLevel(2));
        
        // Category distribution by state
        try {
            stats.put("categoryByState", categoryRepository.getCategoryCountByState());
        } catch (Exception e) {
            log.warn("Could not get category by state aggregation", e);
        }
        
        return stats;
    }
}
