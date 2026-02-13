package com.odop.root.returns.repository;

import com.odop.root.returns.model.ReturnRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for return requests
 */
@Repository
public interface ReturnRequestRepository extends MongoRepository<ReturnRequest, String> {
    
    /**
     * Find by return ID
     */
    Optional<ReturnRequest> findByReturnId(String returnId);
    
    /**
     * Find by order ID
     */
    List<ReturnRequest> findByOrderIdOrderByCreatedAtDesc(String orderId);
    
    /**
     * Find by customer ID
     */
    List<ReturnRequest> findByCustomerIdOrderByCreatedAtDesc(String customerId);
    
    /**
     * Find by customer with pagination
     */
    Page<ReturnRequest> findByCustomerIdOrderByCreatedAtDesc(String customerId, Pageable pageable);
    
    /**
     * Find by vendor ID
     */
    List<ReturnRequest> findByVendorIdOrderByCreatedAtDesc(String vendorId);
    
    /**
     * Find by vendor with pagination
     */
    Page<ReturnRequest> findByVendorIdOrderByCreatedAtDesc(String vendorId, Pageable pageable);
    
    /**
     * Find by status
     */
    List<ReturnRequest> findByStatusOrderByCreatedAtDesc(ReturnRequest.ReturnStatus status);
    
    /**
     * Find by status with pagination
     */
    Page<ReturnRequest> findByStatusOrderByCreatedAtDesc(ReturnRequest.ReturnStatus status, Pageable pageable);
    
    /**
     * Find by multiple statuses
     */
    List<ReturnRequest> findByStatusInOrderByCreatedAtDesc(List<ReturnRequest.ReturnStatus> statuses);
    
    /**
     * Find by vendor and status
     */
    List<ReturnRequest> findByVendorIdAndStatusInOrderByCreatedAtDesc(
            String vendorId, List<ReturnRequest.ReturnStatus> statuses);
    
    /**
     * Find pending returns for vendor
     */
    @Query("{ 'vendorId': ?0, 'status': { $in: ['REQUESTED', 'PENDING_APPROVAL'] } }")
    List<ReturnRequest> findPendingReturnsByVendor(String vendorId);
    
    /**
     * Find returns awaiting pickup
     */
    List<ReturnRequest> findByStatusAndPickupDetails_ScheduledDateBetween(
            ReturnRequest.ReturnStatus status, LocalDateTime start, LocalDateTime end);
    
    /**
     * Find by date range
     */
    List<ReturnRequest> findByCreatedAtBetweenOrderByCreatedAtDesc(
            LocalDateTime start, LocalDateTime end);
    
    /**
     * Count by status
     */
    long countByStatus(ReturnRequest.ReturnStatus status);
    
    /**
     * Count by vendor and status
     */
    long countByVendorIdAndStatus(String vendorId, ReturnRequest.ReturnStatus status);
    
    /**
     * Count pending returns
     */
    @Query(value = "{ 'status': { $in: ['REQUESTED', 'PENDING_APPROVAL'] } }", count = true)
    long countPendingReturns();
    
    /**
     * Count vendor's pending returns
     */
    @Query(value = "{ 'vendorId': ?0, 'status': { $in: ['REQUESTED', 'PENDING_APPROVAL'] } }", count = true)
    long countVendorPendingReturns(String vendorId);
    
    /**
     * Find returns with pending refunds
     */
    @Query("{ 'refundDetails.refundStatus': 'PENDING' }")
    List<ReturnRequest> findPendingRefunds();
    
    /**
     * Sum refund amounts by status
     */
    @Query(value = "{ 'refundDetails.refundStatus': ?0 }", fields = "{ 'refundDetails.refundAmount': 1 }")
    List<ReturnRequest> findByRefundStatus(String status);
    
    /**
     * Check if return exists for order item
     */
    boolean existsByOrderIdAndOrderItemIdAndStatusNotIn(
            String orderId, String orderItemId, List<ReturnRequest.ReturnStatus> excludeStatuses);
}
