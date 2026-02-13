package com.odop.root.returns.controller;

import com.odop.root.returns.dto.ReturnDto.*;
import com.odop.root.returns.model.ReturnRequest;
import com.odop.root.returns.service.ReturnService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * REST Controller for returns and refunds
 */
@RestController
@RequestMapping("/odop/returns")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ReturnController {
    
    private final ReturnService returnService;
    
    // ==================== CUSTOMER ENDPOINTS ====================
    
    /**
     * Create a return request
     */
    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> createReturn(
            @RequestBody CreateReturnRequest request,
            Authentication auth) {
        
        try {
            String customerId = auth.getName();
            String customerName = getUserName(auth);
            String customerEmail = getUserEmail(auth);
            
            ReturnRequest returnRequest = returnService.createReturn(
                    customerId, customerName, customerEmail, request);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Return request created successfully",
                    "returnRequest", ReturnResponse.from(returnRequest)
            ));
            
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Error creating return request", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Failed to create return request"
            ));
        }
    }
    
    /**
     * Get customer's return requests
     */
    @GetMapping("/my-returns")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> getMyReturns(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication auth) {
        
        String customerId = auth.getName();
        List<ReturnResponse> returns = returnService.getCustomerReturns(customerId, page, size);
        
        return ResponseEntity.ok(Map.of(
                "success", true,
                "returns", returns,
                "page", page,
                "size", size
        ));
    }
    
    /**
     * Get single return details
     */
    @GetMapping("/{returnId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getReturn(@PathVariable String returnId, Authentication auth) {
        ReturnResponse response = returnService.getReturnResponse(returnId);
        
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        
        // Verify access
        String userId = auth.getName();
        String role = getUserRole(auth);
        
        if (!"ADMIN".equals(role) && 
            !response.getCustomerId().equals(userId) && 
            !response.getVendorId().equals(userId)) {
            return ResponseEntity.status(403).body(Map.of(
                    "success", false,
                    "message", "Access denied"
            ));
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Cancel return request
     */
    @PostMapping("/{returnId}/cancel")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> cancelReturn(
            @PathVariable String returnId,
            @RequestBody(required = false) Map<String, String> body,
            Authentication auth) {
        
        try {
            String customerId = auth.getName();
            String reason = body != null ? body.get("reason") : null;
            
            ReturnRequest returnRequest = returnService.cancelReturn(returnId, customerId, reason);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Return request cancelled",
                    "returnRequest", ReturnResponse.from(returnRequest)
            ));
            
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Get return policy
     */
    @GetMapping("/policy")
    public ResponseEntity<?> getReturnPolicy() {
        ReturnPolicyInfo policy = returnService.getReturnPolicy();
        
        return ResponseEntity.ok(Map.of(
                "success", true,
                "policy", policy
        ));
    }
    
    // ==================== VENDOR ENDPOINTS ====================
    
    /**
     * Get vendor's return requests
     */
    @GetMapping("/vendor")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<?> getVendorReturns(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication auth) {
        
        String vendorId = auth.getName();
        List<ReturnResponse> returns = returnService.getVendorReturns(vendorId, page, size);
        
        return ResponseEntity.ok(Map.of(
                "success", true,
                "returns", returns,
                "page", page,
                "size", size
        ));
    }
    
    /**
     * Get vendor's pending returns
     */
    @GetMapping("/vendor/pending")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<?> getVendorPendingReturns(Authentication auth) {
        String vendorId = auth.getName();
        List<ReturnResponse> returns = returnService.getVendorPendingReturns(vendorId);
        
        return ResponseEntity.ok(Map.of(
                "success", true,
                "returns", returns
        ));
    }
    
    /**
     * Approve return request
     */
    @PostMapping("/{returnId}/approve")
    @PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")
    public ResponseEntity<?> approveReturn(
            @PathVariable String returnId,
            @RequestBody(required = false) Map<String, String> body,
            Authentication auth) {
        
        try {
            String approvedBy = auth.getName();
            String comment = body != null ? body.get("comment") : null;
            
            ReturnRequest returnRequest = returnService.approveReturn(returnId, approvedBy, comment);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Return request approved",
                    "returnRequest", ReturnResponse.from(returnRequest)
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Reject return request
     */
    @PostMapping("/{returnId}/reject")
    @PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")
    public ResponseEntity<?> rejectReturn(
            @PathVariable String returnId,
            @RequestBody Map<String, String> body,
            Authentication auth) {
        
        try {
            String rejectedBy = auth.getName();
            String reason = body.get("reason");
            
            if (reason == null || reason.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Rejection reason is required"
                ));
            }
            
            ReturnRequest returnRequest = returnService.rejectReturn(returnId, rejectedBy, reason);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Return request rejected",
                    "returnRequest", ReturnResponse.from(returnRequest)
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Get vendor's return summary
     */
    @GetMapping("/vendor/summary")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<?> getVendorReturnSummary(Authentication auth) {
        String vendorId = auth.getName();
        ReturnSummary summary = returnService.getVendorReturnSummary(vendorId);
        
        return ResponseEntity.ok(Map.of(
                "success", true,
                "summary", summary
        ));
    }
    
    // ==================== ADMIN ENDPOINTS ====================
    
    /**
     * Get all returns (admin)
     */
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllReturns(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        List<ReturnResponse> returns;
        
        if (status != null && !status.isEmpty()) {
            returns = returnService.getReturnsByStatus(status, page, size);
        } else {
            // Get all (using any status query with pagination)
            returns = returnService.getReturnsByStatus("REQUESTED", page, size);
        }
        
        return ResponseEntity.ok(Map.of(
                "success", true,
                "returns", returns,
                "page", page,
                "size", size
        ));
    }
    
    /**
     * Update return status (admin)
     */
    @PostMapping("/{returnId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateStatus(
            @PathVariable String returnId,
            @RequestBody UpdateReturnStatusRequest request,
            Authentication auth) {
        
        try {
            String updatedBy = auth.getName();
            
            ReturnRequest returnRequest = returnService.updateStatus(
                    returnId, request.getStatus(), request.getComment(), updatedBy);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Status updated",
                    "returnRequest", ReturnResponse.from(returnRequest)
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Schedule pickup (admin)
     */
    @PostMapping("/schedule-pickup")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> schedulePickup(
            @RequestBody SchedulePickupRequest request,
            Authentication auth) {
        
        try {
            String scheduledBy = auth.getName();
            
            ReturnRequest returnRequest = returnService.schedulePickup(request, scheduledBy);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Pickup scheduled",
                    "returnRequest", ReturnResponse.from(returnRequest)
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Record quality check (admin)
     */
    @PostMapping("/quality-check")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> recordQualityCheck(
            @RequestBody QualityCheckRequest request,
            Authentication auth) {
        
        try {
            String inspectorId = auth.getName();
            
            ReturnRequest returnRequest = returnService.recordQualityCheck(request, inspectorId);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Quality check recorded",
                    "returnRequest", ReturnResponse.from(returnRequest)
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Initiate refund (admin)
     */
    @PostMapping("/initiate-refund")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> initiateRefund(
            @RequestBody ProcessRefundRequest request,
            Authentication auth) {
        
        try {
            String initiatedBy = auth.getName();
            
            ReturnRequest returnRequest = returnService.initiateRefund(request, initiatedBy);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Refund initiated",
                    "returnRequest", ReturnResponse.from(returnRequest)
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Complete refund (admin/webhook)
     */
    @PostMapping("/{returnId}/complete-refund")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> completeRefund(
            @PathVariable String returnId,
            @RequestBody Map<String, String> body) {
        
        try {
            String transactionId = body.get("transactionId");
            
            if (transactionId == null || transactionId.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Transaction ID is required"
                ));
            }
            
            ReturnRequest returnRequest = returnService.completeRefund(returnId, transactionId);
            
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Refund completed",
                    "returnRequest", ReturnResponse.from(returnRequest)
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Get return summary (admin)
     */
    @GetMapping("/admin/summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getReturnSummary() {
        ReturnSummary summary = returnService.getReturnSummary();
        
        return ResponseEntity.ok(Map.of(
                "success", true,
                "summary", summary
        ));
    }
    
    // ==================== ORDER RETURNS ====================
    
    /**
     * Get returns for an order
     */
    @GetMapping("/order/{orderId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getOrderReturns(@PathVariable String orderId) {
        List<ReturnResponse> returns = returnService.getOrderReturns(orderId);
        
        return ResponseEntity.ok(Map.of(
                "success", true,
                "returns", returns
        ));
    }
    
    // ==================== HEALTH CHECK ====================
    
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "Returns & Refunds"
        ));
    }
    
    // ==================== HELPER METHODS ====================
    
    private String getUserRole(Authentication auth) {
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith("ROLE_"))
                .map(a -> a.substring(5))
                .findFirst()
                .orElse("USER");
    }
    
    private String getUserName(Authentication auth) {
        return auth.getName();
    }
    
    private String getUserEmail(Authentication auth) {
        return auth.getName() + "@example.com";
    }
}
