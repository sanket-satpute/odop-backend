package com.odop.root.odopfeatures.festivals.repository;

import com.odop.root.odopfeatures.festivals.model.FestivalCollection;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Festival Collections
 */
@Repository
public interface FestivalCollectionRepository extends MongoRepository<FestivalCollection, String> {
    
    // ==================== Basic Queries ====================
    
    Optional<FestivalCollection> findBySlug(String slug);
    
    Optional<FestivalCollection> findBySlugAndActiveTrue(String slug);
    
    boolean existsBySlug(String slug);
    
    // ==================== Active/Featured Queries ====================
    
    List<FestivalCollection> findByActiveTrueOrderByDisplayOrderAsc();
    
    List<FestivalCollection> findByFeaturedTrueAndActiveTrueOrderByDisplayOrderAsc();
    
    // ==================== Date-Based Queries ====================
    
    // Find live festivals (current date is between start and end)
    @Query("{ 'active': true, 'startDate': { $lte: ?0 }, 'endDate': { $gte: ?0 } }")
    List<FestivalCollection> findLiveFestivals(LocalDate currentDate);
    
    // Find upcoming festivals (festival date is in the future)
    @Query("{ 'active': true, 'festivalDate': { $gte: ?0 } }")
    List<FestivalCollection> findUpcomingFestivals(LocalDate currentDate);
    
    // Find festivals by year
    List<FestivalCollection> findByYearAndActiveTrueOrderByFestivalDateAsc(int year);
    
    // Find festivals within date range
    @Query("{ 'active': true, 'festivalDate': { $gte: ?0, $lte: ?1 } }")
    List<FestivalCollection> findFestivalsBetweenDates(LocalDate start, LocalDate end);
    
    // ==================== Type/Category Queries ====================
    
    List<FestivalCollection> findByTypeAndActiveTrueOrderByDisplayOrderAsc(
            FestivalCollection.FestivalType type);
    
    List<FestivalCollection> findByCategoryAndActiveTrueOrderByDisplayOrderAsc(
            FestivalCollection.FestivalCategory category);
    
    Optional<FestivalCollection> findByCategoryAndYearAndActiveTrue(
            FestivalCollection.FestivalCategory category, int year);
    
    // ==================== Regional Queries ====================
    
    @Query("{ 'primaryStates': ?0, 'active': true }")
    List<FestivalCollection> findByPrimaryState(String state);
    
    List<FestivalCollection> findByRegionalRelevanceAndActiveTrueOrderByDisplayOrderAsc(
            FestivalCollection.RegionalRelevance relevance);
    
    // ==================== Search Queries ====================
    
    @Query("{ $or: [ { 'name': { $regex: ?0, $options: 'i' } }, { 'nameHindi': { $regex: ?0, $options: 'i' } }, { 'description': { $regex: ?0, $options: 'i' } } ], 'active': true }")
    List<FestivalCollection> searchByText(String searchTerm);
    
    // ==================== Count Queries ====================
    
    long countByActiveTrue();
    
    long countByFeaturedTrueAndActiveTrue();
    
    @Query(value = "{ 'active': true, 'festivalDate': { $gte: ?0 } }", count = true)
    long countUpcomingFestivals(LocalDate currentDate);
    
    // ==================== Statistics Queries ====================
    
    @Query(value = "{ 'active': true }", sort = "{ 'viewCount': -1 }")
    List<FestivalCollection> findMostViewedFestivals();
    
    @Query(value = "{ 'active': true }", sort = "{ 'orderCount': -1 }")
    List<FestivalCollection> findMostOrderedFestivals();
}
