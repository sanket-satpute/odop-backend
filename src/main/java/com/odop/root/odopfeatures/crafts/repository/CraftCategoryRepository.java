package com.odop.root.odopfeatures.crafts.repository;

import com.odop.root.odopfeatures.crafts.model.CraftCategory;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Craft Categories with MongoDB
 */
@Repository
public interface CraftCategoryRepository extends MongoRepository<CraftCategory, String> {
    
    // ==================== Basic Queries ====================
    
    Optional<CraftCategory> findBySlug(String slug);
    
    Optional<CraftCategory> findBySlugAndActiveTrue(String slug);
    
    boolean existsBySlug(String slug);
    
    // ==================== Hierarchy Queries ====================
    
    // Find root categories (level 0)
    List<CraftCategory> findByLevelAndActiveTrueOrderByDisplayOrderAsc(int level);
    
    // Find by parent
    List<CraftCategory> findByParentIdAndActiveTrueOrderByDisplayOrderAsc(String parentId);
    
    // Find all children (direct and indirect) using ancestor path
    @Query("{ 'ancestorPath': { $regex: ?0 }, 'active': true }")
    List<CraftCategory> findAllDescendants(String ancestorPathPattern);
    
    // Find by level
    List<CraftCategory> findByLevelOrderByDisplayOrderAsc(int level);
    
    // ==================== Feature Queries ====================
    
    List<CraftCategory> findByFeaturedTrueAndActiveTrueOrderByDisplayOrderAsc();
    
    List<CraftCategory> findByActiveTrue();
    
    List<CraftCategory> findByActiveTrueOrderByDisplayOrderAsc();
    
    // ==================== State/District Queries ====================
    
    @Query("{ 'majorStates': ?0, 'active': true }")
    List<CraftCategory> findByMajorState(String state);
    
    @Query("{ 'famousDistricts': ?0, 'active': true }")
    List<CraftCategory> findByDistrict(String district);
    
    // ==================== GI Tag Queries ====================
    
    @Query("{ 'relatedGiTags': ?0, 'active': true }")
    List<CraftCategory> findByGiTag(String giTag);
    
    @Query("{ 'relatedGiTags': { $in: ?0 }, 'active': true }")
    List<CraftCategory> findByGiTagsIn(List<String> giTags);
    
    // ==================== Count Queries ====================
    
    long countByParentId(String parentId);
    
    long countByLevel(int level);
    
    long countByActiveTrue();
    
    @Query(value = "{ 'level': 0, 'active': true }", count = true)
    long countRootCategories();
    
    // ==================== Search Queries ====================
    
    @Query("{ $or: [ { 'name': { $regex: ?0, $options: 'i' } }, { 'nameHindi': { $regex: ?0, $options: 'i' } } ], 'active': true }")
    List<CraftCategory> searchByName(String searchTerm);
    
    // ==================== Aggregation Queries ====================
    
    // Get category with product count grouped by state
    @Aggregation(pipeline = {
        "{ $match: { 'active': true } }",
        "{ $unwind: '$majorStates' }",
        "{ $group: { _id: '$majorStates', count: { $sum: 1 }, categories: { $push: '$name' } } }",
        "{ $sort: { count: -1 } }"
    })
    List<StateCategoryCount> getCategoryCountByState();
    
    // Get popular categories by product count
    @Query(value = "{ 'active': true }", sort = "{ 'productCount': -1 }")
    List<CraftCategory> findTopByProductCount(int limit);
    
    // Interface for aggregation result
    interface StateCategoryCount {
        String getId(); // state name
        int getCount();
        List<String> getCategories();
    }
}
