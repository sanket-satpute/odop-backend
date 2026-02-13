package com.odop.root.odopfeatures.artisans.repository;

import com.odop.root.odopfeatures.artisans.model.ArtisanStory;
import com.odop.root.odopfeatures.artisans.model.ArtisanStory.StoryStatus;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Artisan Story data access
 */
@Repository
public interface ArtisanStoryRepository extends MongoRepository<ArtisanStory, String> {
    
    // ==================== Basic Finders ====================
    
    Optional<ArtisanStory> findBySlug(String slug);
    
    List<ArtisanStory> findByStatus(StoryStatus status);
    
    List<ArtisanStory> findByStatusOrderByDisplayOrderAsc(StoryStatus status);
    
    // ==================== Featured & Published ====================
    
    @Query("{ 'status': 'PUBLISHED' }")
    List<ArtisanStory> findAllPublished();
    
    @Query("{ 'status': 'PUBLISHED', 'featured': true }")
    List<ArtisanStory> findFeaturedStories();
    
    List<ArtisanStory> findByStatusAndFeaturedTrueOrderByDisplayOrderAsc(StoryStatus status);
    
    // ==================== By Location ====================
    
    List<ArtisanStory> findByStatusAndState(StoryStatus status, String state);
    
    List<ArtisanStory> findByStatusAndStateCode(StoryStatus status, String stateCode);
    
    List<ArtisanStory> findByStatusAndDistrict(StoryStatus status, String district);
    
    @Query("{ 'status': 'PUBLISHED', 'stateCode': ?0 }")
    List<ArtisanStory> findPublishedByStateCode(String stateCode);
    
    // ==================== By Craft ====================
    
    List<ArtisanStory> findByStatusAndPrimaryCraft(StoryStatus status, String craft);
    
    @Query("{ 'status': 'PUBLISHED', 'primaryCraft': { $regex: ?0, $options: 'i' } }")
    List<ArtisanStory> findPublishedByCraft(String craft);
    
    List<ArtisanStory> findByStatusAndCraftCategoryId(StoryStatus status, String craftCategoryId);
    
    // ==================== By Recognition ====================
    
    @Query("{ 'status': 'PUBLISHED', 'nationalAwardee': true }")
    List<ArtisanStory> findNationalAwardees();
    
    @Query("{ 'status': 'PUBLISHED', 'stateAwardee': true }")
    List<ArtisanStory> findStateAwardees();
    
    @Query("{ 'status': 'PUBLISHED', 'giTagHolder': true }")
    List<ArtisanStory> findGiTagHolders();
    
    @Query("{ 'status': 'PUBLISHED', $or: [ { 'nationalAwardee': true }, { 'stateAwardee': true } ] }")
    List<ArtisanStory> findAwardees();
    
    // ==================== By Availability ====================
    
    @Query("{ 'status': 'PUBLISHED', 'availableForWorkshops': true }")
    List<ArtisanStory> findAvailableForWorkshops();
    
    @Query("{ 'status': 'PUBLISHED', 'availableForCommissions': true }")
    List<ArtisanStory> findAvailableForCommissions();
    
    // ==================== By Vendor ====================
    
    Optional<ArtisanStory> findByVendorId(String vendorId);
    
    // ==================== By Experience ====================
    
    @Query("{ 'status': 'PUBLISHED', 'yearsOfExperience': { $gte: ?0 } }")
    List<ArtisanStory> findWithMinExperience(int years);
    
    @Query("{ 'status': 'PUBLISHED', 'generationsInCraft': { $gte: ?0 } }")
    List<ArtisanStory> findWithMinGenerations(int generations);
    
    // ==================== Search ====================
    
    @Query("{ 'status': 'PUBLISHED', $or: [ " +
           "{ 'artisanName': { $regex: ?0, $options: 'i' } }, " +
           "{ 'artisanNameHindi': { $regex: ?0, $options: 'i' } }, " +
           "{ 'primaryCraft': { $regex: ?0, $options: 'i' } }, " +
           "{ 'primaryCraftHindi': { $regex: ?0, $options: 'i' } }, " +
           "{ 'village': { $regex: ?0, $options: 'i' } }, " +
           "{ 'district': { $regex: ?0, $options: 'i' } }, " +
           "{ 'state': { $regex: ?0, $options: 'i' } }, " +
           "{ 'tags': { $regex: ?0, $options: 'i' } } " +
           "] }")
    List<ArtisanStory> searchStories(String searchText);
    
    @Query("{ 'status': 'PUBLISHED', 'tags': { $in: ?0 } }")
    List<ArtisanStory> findByTags(List<String> tags);
    
    // ==================== Geographic Queries ====================
    
    @Query("{ 'status': 'PUBLISHED', " +
           "'latitude': { $gte: ?0, $lte: ?2 }, " +
           "'longitude': { $gte: ?1, $lte: ?3 } }")
    List<ArtisanStory> findInBoundingBox(Double minLat, Double minLng, Double maxLat, Double maxLng);
    
    // ==================== Aggregations ====================
    
    @Aggregation(pipeline = {
            "{ $match: { 'status': 'PUBLISHED' } }",
            "{ $group: { _id: '$primaryCraft', count: { $sum: 1 } } }",
            "{ $sort: { count: -1 } }",
            "{ $limit: 20 }"
    })
    List<CraftCountResult> getArtisanCountByCraft();
    
    @Aggregation(pipeline = {
            "{ $match: { 'status': 'PUBLISHED' } }",
            "{ $group: { _id: { state: '$state', stateCode: '$stateCode' }, count: { $sum: 1 } } }",
            "{ $sort: { count: -1 } }"
    })
    List<StateCountResult> getArtisanCountByState();
    
    @Aggregation(pipeline = {
            "{ $match: { 'status': 'PUBLISHED' } }",
            "{ $group: { _id: '$district', state: { $first: '$state' }, count: { $sum: 1 } } }",
            "{ $sort: { count: -1 } }",
            "{ $limit: 20 }"
    })
    List<DistrictCountResult> getArtisanCountByDistrict();
    
    // ==================== Count Queries ====================
    
    long countByStatus(StoryStatus status);
    
    long countByStatusAndFeaturedTrue(StoryStatus status);
    
    long countByStatusAndNationalAwardeeTrue(StoryStatus status);
    
    long countByStatusAndGiTagHolderTrue(StoryStatus status);
    
    long countByStatusAndStateCode(StoryStatus status, String stateCode);
    
    long countByStatusAndPrimaryCraft(StoryStatus status, String craft);
    
    // ==================== Result Interfaces ====================
    
    interface CraftCountResult {
        String get_id();
        Long getCount();
    }
    
    interface StateCountResult {
        StateInfo get_id();
        Long getCount();
        
        interface StateInfo {
            String getState();
            String getStateCode();
        }
    }
    
    interface DistrictCountResult {
        String get_id();
        String getState();
        Long getCount();
    }
}
