package com.odop.root.returns.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Return request entity
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "return_requests")
public class ReturnRequest {
    
    @Id
    private String id;
    
    // Request ID (user-friendly)
    @Indexed(unique = true)
    private String returnId;
    
    // Order reference
    @Indexed
    private String orderId;
    
    private String orderItemId;
    
    // Product details
    private String productId;
    
    private String productName;
    
    private String productImage;
    
    private String variantId;
    
    private String variantInfo;
    
    // User info
    @Indexed
    private String customerId;
    
    private String customerName;
    
    private String customerEmail;
    
    private String customerPhone;
    
    // Vendor info
    @Indexed
    private String vendorId;
    
    private String vendorName;
    
    // Return details
    private ReturnType returnType;
    
    private ReturnReason reason;
    
    private String reasonDetails;
    
    private int quantity;
    
    private double itemPrice;
    
    private double returnAmount;
    
    // Images of the item (for damage proof)
    private List<String> images;
    
    // Status tracking
    private ReturnStatus status;
    
    private List<StatusHistory> statusHistory;
    
    // Pickup/shipping
    private PickupDetails pickupDetails;
    
    private ShippingDetails shippingDetails;
    
    // Refund tracking
    private RefundDetails refundDetails;
    
    // Admin/Vendor notes
    private String internalNotes;
    
    private String customerNotes;
    
    // Quality check
    private QualityCheckResult qualityCheck;
    
    // Timestamps
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    private LocalDateTime resolvedAt;
    
    // ==================== ENUMS ====================
    
    public enum ReturnType {
        RETURN,             // Return for refund
        EXCHANGE,           // Exchange for another product
        REPLACEMENT,        // Same product replacement
        REPAIR              // Product repair request
    }
    
    public enum ReturnReason {
        DEFECTIVE,          // Product is defective/not working
        DAMAGED,            // Product damaged during shipping
        WRONG_ITEM,         // Wrong item received
        NOT_AS_DESCRIBED,   // Product doesn't match description
        SIZE_FIT,           // Size/fit issue
        QUALITY_ISSUE,      // Quality not as expected
        CHANGED_MIND,       // Customer changed mind
        BETTER_PRICE,       // Found better price elsewhere
        LATE_DELIVERY,      // Arrived too late
        MISSING_PARTS,      // Parts/accessories missing
        OTHER               // Other reason
    }
    
    public enum ReturnStatus {
        REQUESTED,          // Return request submitted
        PENDING_APPROVAL,   // Awaiting vendor/admin approval
        APPROVED,           // Return approved
        REJECTED,           // Return rejected
        PICKUP_SCHEDULED,   // Pickup scheduled
        PICKUP_COMPLETED,   // Item picked up
        IN_TRANSIT,         // Item in transit to warehouse
        RECEIVED,           // Item received at warehouse
        QUALITY_CHECK,      // Under quality inspection
        QC_PASSED,          // Quality check passed
        QC_FAILED,          // Quality check failed
        REFUND_INITIATED,   // Refund process started
        REFUND_COMPLETED,   // Refund completed
        EXCHANGE_SHIPPED,   // Exchange item shipped
        COMPLETED,          // Return process completed
        CANCELLED           // Return cancelled
    }
    
    // ==================== INNER CLASSES ====================
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StatusHistory {
        private ReturnStatus status;
        private String comment;
        private String updatedBy;
        private LocalDateTime timestamp;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PickupDetails {
        private String addressLine1;
        private String addressLine2;
        private String city;
        private String state;
        private String pincode;
        private String landmark;
        private String contactPhone;
        private LocalDateTime preferredDate;
        private String preferredTimeSlot;
        private LocalDateTime scheduledDate;
        private String pickupAgentName;
        private String pickupAgentPhone;
        private String trackingId;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ShippingDetails {
        private String shippingMethod;
        private String trackingNumber;
        private String carrier;
        private LocalDateTime shippedDate;
        private LocalDateTime deliveredDate;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RefundDetails {
        private String refundId;
        private RefundMethod refundMethod;
        private RefundStatus refundStatus;
        private double refundAmount;
        private double deductions;
        private String deductionReason;
        private String transactionId;
        private LocalDateTime initiatedAt;
        private LocalDateTime completedAt;
        private String failureReason;
    }
    
    public enum RefundMethod {
        ORIGINAL_PAYMENT,   // Refund to original payment method
        BANK_TRANSFER,      // Bank account transfer
        WALLET,             // Platform wallet
        STORE_CREDIT        // Store credit
    }
    
    public enum RefundStatus {
        PENDING,
        PROCESSING,
        COMPLETED,
        FAILED,
        CANCELLED
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QualityCheckResult {
        private boolean passed;
        private String inspectorId;
        private String inspectorName;
        private LocalDateTime inspectionDate;
        private String condition;
        private String notes;
        private List<String> defectImages;
        private boolean eligibleForRestock;
    }
    
    // ==================== CONVENIENCE METHODS ====================
    
    public void addStatusHistory(ReturnStatus newStatus, String comment, String updatedBy) {
        if (statusHistory == null) {
            statusHistory = new ArrayList<>();
        }
        
        statusHistory.add(StatusHistory.builder()
                .status(newStatus)
                .comment(comment)
                .updatedBy(updatedBy)
                .timestamp(LocalDateTime.now())
                .build());
        
        this.status = newStatus;
        this.updatedAt = LocalDateTime.now();
    }
    
    public static String generateReturnId() {
        return "RET" + System.currentTimeMillis() + 
               String.format("%04d", new Random().nextInt(10000));
    }
}
