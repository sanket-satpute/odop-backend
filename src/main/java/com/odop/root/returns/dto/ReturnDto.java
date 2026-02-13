package com.odop.root.returns.dto;

import com.odop.root.returns.model.ReturnRequest;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTOs for return/refund operations
 */
public class ReturnDto {
    
    // ==================== REQUEST DTOs ====================
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateReturnRequest {
        private String orderId;
        private String orderItemId;
        private String productId;
        private String variantId;
        private String returnType;
        private String reason;
        private String reasonDetails;
        private int quantity;
        private List<String> images;
        private PickupAddressDto pickupAddress;
        private String preferredRefundMethod;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PickupAddressDto {
        private String addressLine1;
        private String addressLine2;
        private String city;
        private String state;
        private String pincode;
        private String landmark;
        private String contactPhone;
        private String preferredDate;
        private String preferredTimeSlot;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateReturnStatusRequest {
        private String returnId;
        private String status;
        private String comment;
        private String internalNotes;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SchedulePickupRequest {
        private String returnId;
        private String scheduledDate;
        private String timeSlot;
        private String agentName;
        private String agentPhone;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProcessRefundRequest {
        private String returnId;
        private String refundMethod;
        private double refundAmount;
        private double deductions;
        private String deductionReason;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QualityCheckRequest {
        private String returnId;
        private boolean passed;
        private String condition;
        private String notes;
        private List<String> defectImages;
        private boolean eligibleForRestock;
    }
    
    // ==================== RESPONSE DTOs ====================
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReturnResponse {
        private String id;
        private String returnId;
        private String orderId;
        private String productId;
        private String productName;
        private String productImage;
        private String variantInfo;
        private String customerId;
        private String customerName;
        private String vendorId;
        private String vendorName;
        private String returnType;
        private String reason;
        private String reasonDetails;
        private int quantity;
        private double itemPrice;
        private double returnAmount;
        private List<String> images;
        private String status;
        private List<StatusHistoryDto> statusHistory;
        private PickupDetailsDto pickupDetails;
        private RefundDetailsDto refundDetails;
        private String customerNotes;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private LocalDateTime resolvedAt;
        
        public static ReturnResponse from(ReturnRequest request) {
            ReturnResponseBuilder builder = ReturnResponse.builder()
                    .id(request.getId())
                    .returnId(request.getReturnId())
                    .orderId(request.getOrderId())
                    .productId(request.getProductId())
                    .productName(request.getProductName())
                    .productImage(request.getProductImage())
                    .variantInfo(request.getVariantInfo())
                    .customerId(request.getCustomerId())
                    .customerName(request.getCustomerName())
                    .vendorId(request.getVendorId())
                    .vendorName(request.getVendorName())
                    .returnType(request.getReturnType() != null ? request.getReturnType().name() : null)
                    .reason(request.getReason() != null ? request.getReason().name() : null)
                    .reasonDetails(request.getReasonDetails())
                    .quantity(request.getQuantity())
                    .itemPrice(request.getItemPrice())
                    .returnAmount(request.getReturnAmount())
                    .images(request.getImages())
                    .status(request.getStatus() != null ? request.getStatus().name() : null)
                    .customerNotes(request.getCustomerNotes())
                    .createdAt(request.getCreatedAt())
                    .updatedAt(request.getUpdatedAt())
                    .resolvedAt(request.getResolvedAt());
            
            // Map status history
            if (request.getStatusHistory() != null) {
                builder.statusHistory(request.getStatusHistory().stream()
                        .map(StatusHistoryDto::from)
                        .toList());
            }
            
            // Map pickup details
            if (request.getPickupDetails() != null) {
                builder.pickupDetails(PickupDetailsDto.from(request.getPickupDetails()));
            }
            
            // Map refund details
            if (request.getRefundDetails() != null) {
                builder.refundDetails(RefundDetailsDto.from(request.getRefundDetails()));
            }
            
            return builder.build();
        }
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StatusHistoryDto {
        private String status;
        private String comment;
        private String updatedBy;
        private LocalDateTime timestamp;
        
        public static StatusHistoryDto from(ReturnRequest.StatusHistory history) {
            return StatusHistoryDto.builder()
                    .status(history.getStatus().name())
                    .comment(history.getComment())
                    .updatedBy(history.getUpdatedBy())
                    .timestamp(history.getTimestamp())
                    .build();
        }
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PickupDetailsDto {
        private String addressLine1;
        private String addressLine2;
        private String city;
        private String state;
        private String pincode;
        private String contactPhone;
        private LocalDateTime scheduledDate;
        private String pickupAgentName;
        private String pickupAgentPhone;
        private String trackingId;
        
        public static PickupDetailsDto from(ReturnRequest.PickupDetails details) {
            return PickupDetailsDto.builder()
                    .addressLine1(details.getAddressLine1())
                    .addressLine2(details.getAddressLine2())
                    .city(details.getCity())
                    .state(details.getState())
                    .pincode(details.getPincode())
                    .contactPhone(details.getContactPhone())
                    .scheduledDate(details.getScheduledDate())
                    .pickupAgentName(details.getPickupAgentName())
                    .pickupAgentPhone(details.getPickupAgentPhone())
                    .trackingId(details.getTrackingId())
                    .build();
        }
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RefundDetailsDto {
        private String refundId;
        private String refundMethod;
        private String refundStatus;
        private double refundAmount;
        private double deductions;
        private String deductionReason;
        private String transactionId;
        private LocalDateTime initiatedAt;
        private LocalDateTime completedAt;
        
        public static RefundDetailsDto from(ReturnRequest.RefundDetails details) {
            return RefundDetailsDto.builder()
                    .refundId(details.getRefundId())
                    .refundMethod(details.getRefundMethod() != null ? details.getRefundMethod().name() : null)
                    .refundStatus(details.getRefundStatus() != null ? details.getRefundStatus().name() : null)
                    .refundAmount(details.getRefundAmount())
                    .deductions(details.getDeductions())
                    .deductionReason(details.getDeductionReason())
                    .transactionId(details.getTransactionId())
                    .initiatedAt(details.getInitiatedAt())
                    .completedAt(details.getCompletedAt())
                    .build();
        }
    }
    
    // ==================== SUMMARY DTOs ====================
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReturnSummary {
        private long totalReturns;
        private long pendingReturns;
        private long approvedReturns;
        private long completedReturns;
        private long rejectedReturns;
        private double totalRefundAmount;
        private double pendingRefundAmount;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReturnPolicyInfo {
        private int returnWindowDays;
        private List<String> eligibleReasons;
        private List<String> nonReturnableCategories;
        private String refundProcessingTime;
        private String pickupInfo;
        private List<String> requiredDocuments;
    }
}
