package com.odop.root.odopfeatures.districtmap.repository;

import com.odop.root.odopfeatures.districtmap.model.DistrictInfo;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for District Information
 */
@Repository
public interface DistrictInfoRepository extends MongoRepository<DistrictInfo, String> {
    
    // ==================== Basic Queries ====================
    
    Optional<DistrictInfo> findByDistrictCode(String districtCode);
    
    Optional<DistrictInfo> findByStateCodeAndDistrictCode(String stateCode, String districtCode);
    
    boolean existsByDistrictCode(String districtCode);
    
    // ==================== State Queries ====================
    
    List<DistrictInfo> findByStateCodeAndActiveTrueOrderByNameAsc(String stateCode);
    
    List<DistrictInfo> findByStateCodeOrderByDisplayPriorityDesc(String stateCode);
    
    @Query(value = "{ 'stateCode': ?0, 'active': true }", count = true)
    long countByState(String stateCode);
    
    // ==================== Region Queries ====================
    
    List<DistrictInfo> findByRegionAndActiveTrueOrderByDisplayPriorityDesc(DistrictInfo.Region region);
    
    @Query(value = "{ 'region': ?0, 'active': true }", count = true)
    long countByRegion(DistrictInfo.Region region);
    
    // ==================== Featured Queries ====================
    
    List<DistrictInfo> findByFeaturedTrueAndActiveTrueOrderByDisplayPriorityDesc();
    
    List<DistrictInfo> findByActiveTrueOrderByDisplayPriorityDesc();
    
    // ==================== Craft Queries ====================
    
    @Query("{ 'primaryCraft': { $regex: ?0, $options: 'i' }, 'active': true }")
    List<DistrictInfo> findByPrimaryCraft(String craft);
    
    @Query("{ 'craftCategoryIds': ?0, 'active': true }")
    List<DistrictInfo> findByCraftCategoryId(String categoryId);
    
    @Query("{ 'giTaggedProducts': { $exists: true, $ne: [] }, 'active': true }")
    List<DistrictInfo> findDistrictsWithGiTags();
    
    // ==================== Search Queries ====================
    
    @Query("{ $or: [ " +
            "{ 'name': { $regex: ?0, $options: 'i' } }, " +
            "{ 'nameHindi': { $regex: ?0, $options: 'i' } }, " +
            "{ 'stateName': { $regex: ?0, $options: 'i' } }, " +
            "{ 'primaryCraft': { $regex: ?0, $options: 'i' } }, " +
            "{ 'famousFor': { $regex: ?0, $options: 'i' } } " +
            "], 'active': true }")
    List<DistrictInfo> searchDistricts(String searchTerm);
    
    // ==================== Geographic Queries ====================
    
    @Query("{ 'latitude': { $gte: ?0, $lte: ?1 }, 'longitude': { $gte: ?2, $lte: ?3 }, 'active': true }")
    List<DistrictInfo> findInBoundingBox(double minLat, double maxLat, double minLng, double maxLng);
    
    // ==================== Statistics Queries ====================
    
    long countByActiveTrue();
    
    long countByFeaturedTrueAndActiveTrue();
    
    @Query(value = "{ 'giTaggedProducts': { $exists: true, $ne: [] }, 'active': true }", count = true)
    long countDistrictsWithGiTags();
    
    // ==================== Aggregation Queries ====================
    
    @Aggregation(pipeline = {
            "{ $match: { 'active': true } }",
            "{ $group: { _id: '$stateCode', count: { $sum: 1 }, " +
                    "totalProducts: { $sum: '$totalProducts' }, " +
                    "totalArtisans: { $sum: '$registeredArtisans' } } }",
            "{ $sort: { count: -1 } }"
    })
    List<StateAggregation> getDistrictCountByState();
    
    @Aggregation(pipeline = {
            "{ $match: { 'active': true } }",
            "{ $group: { _id: '$region', count: { $sum: 1 }, " +
                    "totalProducts: { $sum: '$totalProducts' } } }",
            "{ $sort: { count: -1 } }"
    })
    List<RegionAggregation> getDistrictCountByRegion();
    
    @Aggregation(pipeline = {
            "{ $match: { 'active': true, 'primaryCraft': { $exists: true, $ne: '' } } }",
            "{ $group: { _id: '$primaryCraft', districtCount: { $sum: 1 }, " +
                    "productCount: { $sum: '$totalProducts' } } }",
            "{ $sort: { districtCount: -1 } }",
            "{ $limit: 10 }"
    })
    List<CraftAggregation> getTopCrafts();
    
    // Aggregation result interfaces
    interface StateAggregation {
        String getId(); // stateCode
        int getCount();
        long getTotalProducts();
        long getTotalArtisans();
    }
    
    interface RegionAggregation {
        String getId(); // region
        int getCount();
        long getTotalProducts();
    }
    
    interface CraftAggregation {
        String getId(); // craftName
        int getDistrictCount();
        long getProductCount();
    }
}
