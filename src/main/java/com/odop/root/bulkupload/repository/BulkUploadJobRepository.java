package com.odop.root.bulkupload.repository;

import com.odop.root.bulkupload.model.BulkUploadJob;
import com.odop.root.bulkupload.model.BulkUploadJob.UploadStatus;
import com.odop.root.bulkupload.model.BulkUploadJob.UploadType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for bulk upload jobs
 */
@Repository
public interface BulkUploadJobRepository extends MongoRepository<BulkUploadJob, String> {
    
    // Find by vendor
    List<BulkUploadJob> findByVendorIdOrderByCreatedAtDesc(String vendorId);
    
    Page<BulkUploadJob> findByVendorIdOrderByCreatedAtDesc(String vendorId, Pageable pageable);
    
    // Find by status
    List<BulkUploadJob> findByStatus(UploadStatus status);
    
    List<BulkUploadJob> findByVendorIdAndStatus(String vendorId, UploadStatus status);
    
    // Find by upload type
    List<BulkUploadJob> findByVendorIdAndUploadType(String vendorId, UploadType uploadType);
    
    // Find running jobs
    @Query("{ 'status': { $in: ['PENDING', 'VALIDATING', 'PROCESSING'] } }")
    List<BulkUploadJob> findRunningJobs();
    
    @Query("{ 'vendorId': ?0, 'status': { $in: ['PENDING', 'VALIDATING', 'PROCESSING'] } }")
    List<BulkUploadJob> findRunningJobsByVendor(String vendorId);
    
    // Count running jobs for vendor
    @Query(value = "{ 'vendorId': ?0, 'status': { $in: ['PENDING', 'VALIDATING', 'PROCESSING'] } }", count = true)
    long countRunningJobsByVendor(String vendorId);
    
    // Find recent jobs
    List<BulkUploadJob> findByVendorIdAndCreatedAtAfterOrderByCreatedAtDesc(
            String vendorId, LocalDateTime after);
    
    // Find completed jobs
    List<BulkUploadJob> findByVendorIdAndStatusIn(String vendorId, List<UploadStatus> statuses);
    
    // Find stuck jobs (processing for too long)
    @Query("{ 'status': { $in: ['PROCESSING', 'VALIDATING'] }, 'startedAt': { $lt: ?0 } }")
    List<BulkUploadJob> findStuckJobs(LocalDateTime threshold);
    
    // Get latest job for vendor
    Optional<BulkUploadJob> findFirstByVendorIdOrderByCreatedAtDesc(String vendorId);
    
    // Delete old completed jobs
    void deleteByStatusAndCompletedAtBefore(UploadStatus status, LocalDateTime before);
    
    // Statistics
    @Query(value = "{ 'vendorId': ?0, 'status': 'COMPLETED' }", count = true)
    long countCompletedByVendor(String vendorId);
    
    @Query(value = "{ 'vendorId': ?0, 'status': 'FAILED' }", count = true)
    long countFailedByVendor(String vendorId);
}
