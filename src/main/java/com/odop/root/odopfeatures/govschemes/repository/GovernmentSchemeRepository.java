package com.odop.root.odopfeatures.govschemes.repository;

import com.odop.root.odopfeatures.govschemes.model.GovernmentScheme;
import com.odop.root.odopfeatures.govschemes.model.GovernmentScheme.*;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Government Scheme data access
 */
@Repository
public interface GovernmentSchemeRepository extends MongoRepository<GovernmentScheme, String> {
    
    // ==================== Basic Finders ====================
    
    Optional<GovernmentScheme> findBySlug(String slug);
    
    Optional<GovernmentScheme> findByShortName(String shortName);
    
    List<GovernmentScheme> findByActiveTrue();
    
    List<GovernmentScheme> findByActiveTrueOrderByDisplayOrderAsc();
    
    // ==================== Featured & Popular ====================
    
    List<GovernmentScheme> findByActiveTrueAndFeaturedTrueOrderByDisplayOrderAsc();
    
    @Query("{ 'active': true }")
    List<GovernmentScheme> findTopByOrderByPopularityScoreDesc();
    
    @Query("{ 'active': true }")
    List<GovernmentScheme> findTopByOrderByViewCountDesc();
    
    // ==================== By Type ====================
    
    List<GovernmentScheme> findByActiveTrueAndType(SchemeType type);
    
    List<GovernmentScheme> findByActiveTrueAndTypeOrderByDisplayOrderAsc(SchemeType type);
    
    List<GovernmentScheme> findByActiveTrueAndTypeIn(List<SchemeType> types);
    
    // ==================== By Category ====================
    
    List<GovernmentScheme> findByActiveTrueAndCategory(SchemeCategory category);
    
    List<GovernmentScheme> findByActiveTrueAndCategoryOrderByDisplayOrderAsc(SchemeCategory category);
    
    List<GovernmentScheme> findByActiveTrueAndCategoryIn(List<SchemeCategory> categories);
    
    // ==================== By Government Level ====================
    
    List<GovernmentScheme> findByActiveTrueAndLevel(GovernmentLevel level);
    
    List<GovernmentScheme> findByActiveTrueAndLevelOrderByDisplayOrderAsc(GovernmentLevel level);
    
    // ==================== By Target Beneficiary ====================
    
    @Query("{ 'active': true, 'targetBeneficiaries': ?0 }")
    List<GovernmentScheme> findByTargetBeneficiary(TargetBeneficiary beneficiary);
    
    @Query("{ 'active': true, 'targetBeneficiaries': { $in: ?0 } }")
    List<GovernmentScheme> findByTargetBeneficiariesIn(List<TargetBeneficiary> beneficiaries);
    
    // ==================== By State ====================
    
    @Query("{ 'active': true, $or: [ { 'panIndiaScheme': true }, { 'applicableStates': ?0 } ] }")
    List<GovernmentScheme> findSchemesForState(String stateCode);
    
    List<GovernmentScheme> findByActiveTrueAndPanIndiaSchemeTrueOrderByDisplayOrderAsc();
    
    @Query("{ 'active': true, 'applicableStates': ?0 }")
    List<GovernmentScheme> findByApplicableState(String stateCode);
    
    // ==================== Application Status ====================
    
    List<GovernmentScheme> findByActiveTrueAndOpenForApplicationsTrue();
    
    List<GovernmentScheme> findByActiveTrueAndOnlineApplicationAvailableTrue();
    
    @Query("{ 'active': true, 'lastDateToApply': { $gte: ?0 }, 'openForApplications': true }")
    List<GovernmentScheme> findOpenSchemesWithDeadlineAfter(LocalDate date);
    
    @Query("{ 'active': true, 'lastDateToApply': { $gte: ?0, $lte: ?1 }, 'openForApplications': true }")
    List<GovernmentScheme> findSchemesWithDeadlineBetween(LocalDate start, LocalDate end);
    
    // ==================== By Ministry ====================
    
    List<GovernmentScheme> findByActiveTrueAndMinistry(String ministry);
    
    @Query(value = "{ 'active': true }", fields = "{ 'ministry': 1 }")
    List<GovernmentScheme> findDistinctMinistries();
    
    // ==================== Search ====================
    
    @Query("{ 'active': true, $or: [ " +
           "{ 'name': { $regex: ?0, $options: 'i' } }, " +
           "{ 'nameHindi': { $regex: ?0, $options: 'i' } }, " +
           "{ 'shortName': { $regex: ?0, $options: 'i' } }, " +
           "{ 'description': { $regex: ?0, $options: 'i' } }, " +
           "{ 'tags': { $regex: ?0, $options: 'i' } } " +
           "] }")
    List<GovernmentScheme> searchSchemes(String searchText);
    
    @Query("{ 'active': true, 'tags': { $in: ?0 } }")
    List<GovernmentScheme> findByTags(List<String> tags);
    
    // ==================== By Craft Category ====================
    
    @Query("{ 'active': true, 'craftCategoryIds': ?0 }")
    List<GovernmentScheme> findByCraftCategory(String craftCategoryId);
    
    @Query("{ 'active': true, 'craftCategoryIds': { $in: ?0 } }")
    List<GovernmentScheme> findByCraftCategories(List<String> craftCategoryIds);
    
    // ==================== Financial Filters ====================
    
    @Query("{ 'active': true, 'collateralRequirement': 'No collateral required' }")
    List<GovernmentScheme> findCollateralFreeSchemes();
    
    @Query("{ 'active': true, 'type': 'LOAN' }")
    List<GovernmentScheme> findLoanSchemes();
    
    @Query("{ 'active': true, 'type': { $in: ['GRANT', 'SUBSIDY'] } }")
    List<GovernmentScheme> findGrantAndSubsidySchemes();
    
    // ==================== Related Schemes ====================
    
    @Query("{ 'active': true, '_id': { $in: ?0 } }")
    List<GovernmentScheme> findRelatedSchemes(List<String> schemeIds);
    
    // ==================== Aggregations ====================
    
    @Aggregation(pipeline = {
            "{ $match: { 'active': true } }",
            "{ $group: { _id: '$type', count: { $sum: 1 } } }",
            "{ $sort: { count: -1 } }"
    })
    List<TypeCountResult> getSchemeCountByType();
    
    @Aggregation(pipeline = {
            "{ $match: { 'active': true } }",
            "{ $group: { _id: '$category', count: { $sum: 1 } } }",
            "{ $sort: { count: -1 } }"
    })
    List<CategoryCountResult> getSchemeCountByCategory();
    
    @Aggregation(pipeline = {
            "{ $match: { 'active': true } }",
            "{ $group: { _id: '$level', count: { $sum: 1 } } }",
            "{ $sort: { count: -1 } }"
    })
    List<LevelCountResult> getSchemeCountByLevel();
    
    @Aggregation(pipeline = {
            "{ $match: { 'active': true } }",
            "{ $unwind: '$targetBeneficiaries' }",
            "{ $group: { _id: '$targetBeneficiaries', count: { $sum: 1 } } }",
            "{ $sort: { count: -1 } }"
    })
    List<BeneficiaryCountResult> getSchemeCountByBeneficiary();
    
    // ==================== Count Queries ====================
    
    long countByActiveTrue();
    
    long countByActiveTrueAndType(SchemeType type);
    
    long countByActiveTrueAndCategory(SchemeCategory category);
    
    long countByActiveTrueAndLevel(GovernmentLevel level);
    
    long countByActiveTrueAndOpenForApplicationsTrue();
    
    // ==================== Result Classes for Aggregations ====================
    
    interface TypeCountResult {
        SchemeType get_id();
        Long getCount();
    }
    
    interface CategoryCountResult {
        SchemeCategory get_id();
        Long getCount();
    }
    
    interface LevelCountResult {
        GovernmentLevel get_id();
        Long getCount();
    }
    
    interface BeneficiaryCountResult {
        TargetBeneficiary get_id();
        Long getCount();
    }
}
