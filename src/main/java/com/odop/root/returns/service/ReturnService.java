package com.odop.root.returns.service;

import com.odop.root.returns.dto.ReturnDto.*;
import com.odop.root.returns.model.ReturnRequest;
import com.odop.root.returns.model.ReturnRequest.*;
import com.odop.root.returns.repository.ReturnRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for return/refund operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReturnService {
    
    private final ReturnRequestRepository returnRepository;
    
    // Return policy configuration
    private static final int RETURN_WINDOW_DAYS = 7;
    private static final List<String> NON_RETURNABLE_CATEGORIES = List.of(
            "PERISHABLE", "CUSTOMIZED", "INTIMATE_APPAREL", "DIGITAL"
    );
    
    // ==================== CREATE RETURN ====================
    
    /**
     * Create a new return request
     */
    @Transactional
    public ReturnRequest createReturn(String customerId, String customerName, 
                                       String customerEmail, CreateReturnRequest request) {
        
        // Validate - check if return already exists for this item
        List<ReturnStatus> activeStatuses = List.of(
                ReturnStatus.CANCELLED, ReturnStatus.REJECTED
        );
        
        if (returnRepository.existsByOrderIdAndOrderItemIdAndStatusNotIn(
                request.getOrderId(), request.getOrderItemId(), activeStatuses)) {
            throw new IllegalStateException("A return request already exists for this item");
        }
        
        // Create return request
        ReturnRequest returnRequest = ReturnRequest.builder()
                .returnId(ReturnRequest.generateReturnId())
                .orderId(request.getOrderId())
                .orderItemId(request.getOrderItemId())
                .productId(request.getProductId())
                .variantId(request.getVariantId())
                .customerId(customerId)
                .customerName(customerName)
                .customerEmail(customerEmail)
                .returnType(ReturnType.valueOf(request.getReturnType()))
                .reason(ReturnReason.valueOf(request.getReason()))
                .reasonDetails(request.getReasonDetails())
                .quantity(request.getQuantity())
                .images(request.getImages())
                .status(ReturnStatus.REQUESTED)
                .statusHistory(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        // Set pickup address if provided
        if (request.getPickupAddress() != null) {
            returnRequest.setPickupDetails(mapPickupAddress(request.getPickupAddress()));
        }
        
        // Add initial status history
        returnRequest.addStatusHistory(ReturnStatus.REQUESTED, 
                "Return request submitted by customer", customerId);
        
        // TODO: Fetch product/vendor details from order/product service
        // For now, set placeholders
        returnRequest.setProductName("Product " + request.getProductId());
        
        returnRequest = returnRepository.save(returnRequest);
        
        log.info("Return request created: {} for order: {}", 
                returnRequest.getReturnId(), request.getOrderId());
        
        // TODO: Send notification to vendor
        // TODO: Send confirmation email to customer
        
        return returnRequest;
    }
    
    // ==================== GET RETURNS ====================
    
    /**
     * Get return by ID
     */
    public ReturnRequest getReturn(String returnId) {
        return returnRepository.findByReturnId(returnId)
                .or(() -> returnRepository.findById(returnId))
                .orElse(null);
    }
    
    /**
     * Get return response by ID
     */
    public ReturnResponse getReturnResponse(String returnId) {
        ReturnRequest request = getReturn(returnId);
        return request != null ? ReturnResponse.from(request) : null;
    }
    
    /**
     * Get customer's returns
     */
    public List<ReturnResponse> getCustomerReturns(String customerId, int page, int size) {
        Page<ReturnRequest> returns = returnRepository
                .findByCustomerIdOrderByCreatedAtDesc(customerId, PageRequest.of(page, size));
        
        return returns.getContent().stream()
                .map(ReturnResponse::from)
                .collect(Collectors.toList());
    }
    
    /**
     * Get vendor's returns
     */
    public List<ReturnResponse> getVendorReturns(String vendorId, int page, int size) {
        Page<ReturnRequest> returns = returnRepository
                .findByVendorIdOrderByCreatedAtDesc(vendorId, PageRequest.of(page, size));
        
        return returns.getContent().stream()
                .map(ReturnResponse::from)
                .collect(Collectors.toList());
    }
    
    /**
     * Get vendor's pending returns
     */
    public List<ReturnResponse> getVendorPendingReturns(String vendorId) {
        return returnRepository.findPendingReturnsByVendor(vendorId).stream()
                .map(ReturnResponse::from)
                .collect(Collectors.toList());
    }
    
    /**
     * Get returns by status
     */
    public List<ReturnResponse> getReturnsByStatus(String status, int page, int size) {
        ReturnStatus returnStatus = ReturnStatus.valueOf(status);
        Page<ReturnRequest> returns = returnRepository
                .findByStatusOrderByCreatedAtDesc(returnStatus, PageRequest.of(page, size));
        
        return returns.getContent().stream()
                .map(ReturnResponse::from)
                .collect(Collectors.toList());
    }
    
    /**
     * Get returns for an order
     */
    public List<ReturnResponse> getOrderReturns(String orderId) {
        return returnRepository.findByOrderIdOrderByCreatedAtDesc(orderId).stream()
                .map(ReturnResponse::from)
                .collect(Collectors.toList());
    }
    
    // ==================== UPDATE STATUS ====================
    
    /**
     * Update return status
     */
    @Transactional
    public ReturnRequest updateStatus(String returnId, String newStatus, 
                                       String comment, String updatedBy) {
        
        ReturnRequest returnRequest = getReturn(returnId);
        if (returnRequest == null) {
            throw new IllegalArgumentException("Return request not found");
        }
        
        ReturnStatus status = ReturnStatus.valueOf(newStatus);
        
        // Validate status transition
        validateStatusTransition(returnRequest.getStatus(), status);
        
        returnRequest.addStatusHistory(status, comment, updatedBy);
        
        // Handle special status actions
        handleStatusAction(returnRequest, status);
        
        returnRequest = returnRepository.save(returnRequest);
        
        log.info("Return {} status updated to {}", returnId, newStatus);
        
        // TODO: Send notifications based on status
        
        return returnRequest;
    }
    
    /**
     * Approve return request
     */
    @Transactional
    public ReturnRequest approveReturn(String returnId, String approvedBy, String comment) {
        ReturnRequest returnRequest = getReturn(returnId);
        if (returnRequest == null) {
            throw new IllegalArgumentException("Return request not found");
        }
        
        returnRequest.addStatusHistory(ReturnStatus.APPROVED, 
                comment != null ? comment : "Return request approved", approvedBy);
        
        returnRequest = returnRepository.save(returnRequest);
        
        log.info("Return {} approved by {}", returnId, approvedBy);
        
        // TODO: Initiate pickup scheduling
        // TODO: Send approval notification to customer
        
        return returnRequest;
    }
    
    /**
     * Reject return request
     */
    @Transactional
    public ReturnRequest rejectReturn(String returnId, String rejectedBy, String reason) {
        ReturnRequest returnRequest = getReturn(returnId);
        if (returnRequest == null) {
            throw new IllegalArgumentException("Return request not found");
        }
        
        returnRequest.addStatusHistory(ReturnStatus.REJECTED, 
                reason != null ? reason : "Return request rejected", rejectedBy);
        returnRequest.setResolvedAt(LocalDateTime.now());
        
        returnRequest = returnRepository.save(returnRequest);
        
        log.info("Return {} rejected by {}: {}", returnId, rejectedBy, reason);
        
        // TODO: Send rejection notification to customer
        
        return returnRequest;
    }
    
    /**
     * Cancel return request (by customer)
     */
    @Transactional
    public ReturnRequest cancelReturn(String returnId, String customerId, String reason) {
        ReturnRequest returnRequest = getReturn(returnId);
        if (returnRequest == null) {
            throw new IllegalArgumentException("Return request not found");
        }
        
        // Verify ownership
        if (!returnRequest.getCustomerId().equals(customerId)) {
            throw new IllegalArgumentException("Unauthorized to cancel this return");
        }
        
        // Can only cancel in early stages
        if (!canCancel(returnRequest.getStatus())) {
            throw new IllegalStateException("Cannot cancel return in current status");
        }
        
        returnRequest.addStatusHistory(ReturnStatus.CANCELLED, 
                reason != null ? reason : "Cancelled by customer", customerId);
        returnRequest.setResolvedAt(LocalDateTime.now());
        
        returnRequest = returnRepository.save(returnRequest);
        
        log.info("Return {} cancelled by customer", returnId);
        
        return returnRequest;
    }
    
    // ==================== PICKUP MANAGEMENT ====================
    
    /**
     * Schedule pickup
     */
    @Transactional
    public ReturnRequest schedulePickup(SchedulePickupRequest request, String scheduledBy) {
        ReturnRequest returnRequest = getReturn(request.getReturnId());
        if (returnRequest == null) {
            throw new IllegalArgumentException("Return request not found");
        }
        
        if (returnRequest.getPickupDetails() == null) {
            returnRequest.setPickupDetails(new PickupDetails());
        }
        
        PickupDetails pickup = returnRequest.getPickupDetails();
        pickup.setScheduledDate(LocalDateTime.parse(request.getScheduledDate(), 
                DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        pickup.setPreferredTimeSlot(request.getTimeSlot());
        pickup.setPickupAgentName(request.getAgentName());
        pickup.setPickupAgentPhone(request.getAgentPhone());
        pickup.setTrackingId("PKP" + System.currentTimeMillis());
        
        returnRequest.addStatusHistory(ReturnStatus.PICKUP_SCHEDULED, 
                "Pickup scheduled for " + request.getScheduledDate(), scheduledBy);
        
        returnRequest = returnRepository.save(returnRequest);
        
        log.info("Pickup scheduled for return {}", request.getReturnId());
        
        // TODO: Send pickup notification to customer
        
        return returnRequest;
    }
    
    /**
     * Mark pickup as completed
     */
    @Transactional
    public ReturnRequest completePickup(String returnId, String completedBy) {
        return updateStatus(returnId, "PICKUP_COMPLETED", 
                "Item picked up from customer", completedBy);
    }
    
    // ==================== QUALITY CHECK ====================
    
    /**
     * Record quality check result
     */
    @Transactional
    public ReturnRequest recordQualityCheck(QualityCheckRequest request, String inspectorId) {
        ReturnRequest returnRequest = getReturn(request.getReturnId());
        if (returnRequest == null) {
            throw new IllegalArgumentException("Return request not found");
        }
        
        QualityCheckResult qcResult = QualityCheckResult.builder()
                .passed(request.isPassed())
                .inspectorId(inspectorId)
                .inspectionDate(LocalDateTime.now())
                .condition(request.getCondition())
                .notes(request.getNotes())
                .defectImages(request.getDefectImages())
                .eligibleForRestock(request.isEligibleForRestock())
                .build();
        
        returnRequest.setQualityCheck(qcResult);
        
        ReturnStatus newStatus = request.isPassed() ? 
                ReturnStatus.QC_PASSED : ReturnStatus.QC_FAILED;
        
        returnRequest.addStatusHistory(newStatus, 
                "Quality check " + (request.isPassed() ? "passed" : "failed") + 
                ": " + request.getNotes(), inspectorId);
        
        returnRequest = returnRepository.save(returnRequest);
        
        log.info("Quality check recorded for return {}: {}", 
                request.getReturnId(), request.isPassed() ? "PASSED" : "FAILED");
        
        return returnRequest;
    }
    
    // ==================== REFUND PROCESSING ====================
    
    /**
     * Initiate refund
     */
    @Transactional
    public ReturnRequest initiateRefund(ProcessRefundRequest request, String initiatedBy) {
        ReturnRequest returnRequest = getReturn(request.getReturnId());
        if (returnRequest == null) {
            throw new IllegalArgumentException("Return request not found");
        }
        
        RefundDetails refund = RefundDetails.builder()
                .refundId("REF" + System.currentTimeMillis())
                .refundMethod(RefundMethod.valueOf(request.getRefundMethod()))
                .refundStatus(RefundStatus.PENDING)
                .refundAmount(request.getRefundAmount())
                .deductions(request.getDeductions())
                .deductionReason(request.getDeductionReason())
                .initiatedAt(LocalDateTime.now())
                .build();
        
        returnRequest.setRefundDetails(refund);
        returnRequest.setReturnAmount(request.getRefundAmount());
        
        returnRequest.addStatusHistory(ReturnStatus.REFUND_INITIATED, 
                "Refund of ₹" + request.getRefundAmount() + " initiated", initiatedBy);
        
        returnRequest = returnRepository.save(returnRequest);
        
        log.info("Refund initiated for return {}: ₹{}", 
                request.getReturnId(), request.getRefundAmount());
        
        // TODO: Integrate with payment gateway for actual refund
        // TODO: Send refund initiated notification
        
        return returnRequest;
    }
    
    /**
     * Complete refund (called after payment gateway confirmation)
     */
    @Transactional
    public ReturnRequest completeRefund(String returnId, String transactionId) {
        ReturnRequest returnRequest = getReturn(returnId);
        if (returnRequest == null) {
            throw new IllegalArgumentException("Return request not found");
        }
        
        if (returnRequest.getRefundDetails() == null) {
            throw new IllegalStateException("No refund details found");
        }
        
        RefundDetails refund = returnRequest.getRefundDetails();
        refund.setRefundStatus(RefundStatus.COMPLETED);
        refund.setTransactionId(transactionId);
        refund.setCompletedAt(LocalDateTime.now());
        
        returnRequest.addStatusHistory(ReturnStatus.REFUND_COMPLETED, 
                "Refund completed. Transaction ID: " + transactionId, "SYSTEM");
        returnRequest.addStatusHistory(ReturnStatus.COMPLETED, 
                "Return process completed", "SYSTEM");
        returnRequest.setResolvedAt(LocalDateTime.now());
        
        returnRequest = returnRepository.save(returnRequest);
        
        log.info("Refund completed for return {}: {}", returnId, transactionId);
        
        // TODO: Send refund completion notification
        
        return returnRequest;
    }
    
    // ==================== STATISTICS ====================
    
    /**
     * Get return summary statistics
     */
    public ReturnSummary getReturnSummary() {
        long pending = returnRepository.countPendingReturns();
        long approved = returnRepository.countByStatus(ReturnStatus.APPROVED) +
                       returnRepository.countByStatus(ReturnStatus.PICKUP_SCHEDULED);
        long completed = returnRepository.countByStatus(ReturnStatus.COMPLETED);
        long rejected = returnRepository.countByStatus(ReturnStatus.REJECTED);
        
        // Calculate refund amounts
        double totalRefund = calculateTotalRefundAmount();
        double pendingRefund = calculatePendingRefundAmount();
        
        return ReturnSummary.builder()
                .totalReturns(returnRepository.count())
                .pendingReturns(pending)
                .approvedReturns(approved)
                .completedReturns(completed)
                .rejectedReturns(rejected)
                .totalRefundAmount(totalRefund)
                .pendingRefundAmount(pendingRefund)
                .build();
    }
    
    /**
     * Get vendor-specific summary
     */
    public ReturnSummary getVendorReturnSummary(String vendorId) {
        long pending = returnRepository.countVendorPendingReturns(vendorId);
        long approved = returnRepository.countByVendorIdAndStatus(vendorId, ReturnStatus.APPROVED);
        long completed = returnRepository.countByVendorIdAndStatus(vendorId, ReturnStatus.COMPLETED);
        long rejected = returnRepository.countByVendorIdAndStatus(vendorId, ReturnStatus.REJECTED);
        
        return ReturnSummary.builder()
                .pendingReturns(pending)
                .approvedReturns(approved)
                .completedReturns(completed)
                .rejectedReturns(rejected)
                .build();
    }
    
    /**
     * Get return policy info
     */
    public ReturnPolicyInfo getReturnPolicy() {
        return ReturnPolicyInfo.builder()
                .returnWindowDays(RETURN_WINDOW_DAYS)
                .eligibleReasons(Arrays.stream(ReturnReason.values())
                        .map(Enum::name).toList())
                .nonReturnableCategories(NON_RETURNABLE_CATEGORIES)
                .refundProcessingTime("5-7 business days")
                .pickupInfo("Free pickup available in select areas")
                .requiredDocuments(List.of(
                        "Original invoice",
                        "Product images showing issue",
                        "Original packaging (if available)"
                ))
                .build();
    }
    
    // ==================== HELPER METHODS ====================
    
    private PickupDetails mapPickupAddress(PickupAddressDto dto) {
        return PickupDetails.builder()
                .addressLine1(dto.getAddressLine1())
                .addressLine2(dto.getAddressLine2())
                .city(dto.getCity())
                .state(dto.getState())
                .pincode(dto.getPincode())
                .landmark(dto.getLandmark())
                .contactPhone(dto.getContactPhone())
                .preferredDate(dto.getPreferredDate() != null ? 
                        LocalDateTime.parse(dto.getPreferredDate()) : null)
                .preferredTimeSlot(dto.getPreferredTimeSlot())
                .build();
    }
    
    private void validateStatusTransition(ReturnStatus current, ReturnStatus newStatus) {
        // Define valid transitions
        Map<ReturnStatus, Set<ReturnStatus>> validTransitions = Map.ofEntries(
                Map.entry(ReturnStatus.REQUESTED, Set.of(ReturnStatus.PENDING_APPROVAL, ReturnStatus.APPROVED, ReturnStatus.REJECTED, ReturnStatus.CANCELLED)),
                Map.entry(ReturnStatus.PENDING_APPROVAL, Set.of(ReturnStatus.APPROVED, ReturnStatus.REJECTED, ReturnStatus.CANCELLED)),
                Map.entry(ReturnStatus.APPROVED, Set.of(ReturnStatus.PICKUP_SCHEDULED, ReturnStatus.CANCELLED)),
                Map.entry(ReturnStatus.PICKUP_SCHEDULED, Set.of(ReturnStatus.PICKUP_COMPLETED)),
                Map.entry(ReturnStatus.PICKUP_COMPLETED, Set.of(ReturnStatus.IN_TRANSIT)),
                Map.entry(ReturnStatus.IN_TRANSIT, Set.of(ReturnStatus.RECEIVED)),
                Map.entry(ReturnStatus.RECEIVED, Set.of(ReturnStatus.QUALITY_CHECK)),
                Map.entry(ReturnStatus.QUALITY_CHECK, Set.of(ReturnStatus.QC_PASSED, ReturnStatus.QC_FAILED)),
                Map.entry(ReturnStatus.QC_PASSED, Set.of(ReturnStatus.REFUND_INITIATED, ReturnStatus.EXCHANGE_SHIPPED)),
                Map.entry(ReturnStatus.REFUND_INITIATED, Set.of(ReturnStatus.REFUND_COMPLETED)),
                Map.entry(ReturnStatus.REFUND_COMPLETED, Set.of(ReturnStatus.COMPLETED))
        );
        
        Set<ReturnStatus> allowed = validTransitions.getOrDefault(current, Set.of());
        if (!allowed.contains(newStatus)) {
            throw new IllegalStateException(
                    "Invalid status transition from " + current + " to " + newStatus);
        }
    }
    
    private void handleStatusAction(ReturnRequest request, ReturnStatus status) {
        switch (status) {
            case COMPLETED:
                request.setResolvedAt(LocalDateTime.now());
                break;
            case REJECTED:
            case CANCELLED:
                request.setResolvedAt(LocalDateTime.now());
                break;
            default:
                break;
        }
    }
    
    private boolean canCancel(ReturnStatus status) {
        return status == ReturnStatus.REQUESTED || 
               status == ReturnStatus.PENDING_APPROVAL ||
               status == ReturnStatus.APPROVED;
    }
    
    private double calculateTotalRefundAmount() {
        List<ReturnRequest> completed = returnRepository.findByRefundStatus("COMPLETED");
        return completed.stream()
                .filter(r -> r.getRefundDetails() != null)
                .mapToDouble(r -> r.getRefundDetails().getRefundAmount())
                .sum();
    }
    
    private double calculatePendingRefundAmount() {
        List<ReturnRequest> pending = returnRepository.findPendingRefunds();
        return pending.stream()
                .filter(r -> r.getRefundDetails() != null)
                .mapToDouble(r -> r.getRefundDetails().getRefundAmount())
                .sum();
    }
}
