package com.odop.root.report.repository;

import com.odop.root.report.model.Report;
import com.odop.root.report.model.Report.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for reports
 */
@Repository
public interface ReportRepository extends MongoRepository<Report, String> {
    
    // Find by user
    List<Report> findByUserIdOrderByCreatedAtDesc(String userId);
    
    Page<Report> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);
    
    // Find by user and type
    List<Report> findByUserIdAndReportType(String userId, ReportType reportType);
    
    // Find by status
    List<Report> findByStatus(ReportStatus status);
    
    List<Report> findByUserIdAndStatus(String userId, ReportStatus status);
    
    // Find pending/generating reports
    @Query("{ 'status': { $in: ['PENDING', 'GENERATING'] } }")
    List<Report> findPendingReports();
    
    // Find expired reports
    @Query("{ 'expiresAt': { $lt: ?0 }, 'status': 'COMPLETED' }")
    List<Report> findExpiredReports(LocalDateTime now);
    
    // Delete expired
    void deleteByExpiresAtBeforeAndStatus(LocalDateTime before, ReportStatus status);
    
    // Count by user
    long countByUserId(String userId);
    
    long countByUserIdAndStatus(String userId, ReportStatus status);
    
    // Recent reports
    List<Report> findTop10ByUserIdOrderByCreatedAtDesc(String userId);
}
