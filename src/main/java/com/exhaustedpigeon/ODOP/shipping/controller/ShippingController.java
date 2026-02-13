package com.exhaustedpigeon.ODOP.shipping.controller;

import com.exhaustedpigeon.ODOP.shipping.dto.CreateShipmentRequest;
import com.exhaustedpigeon.ODOP.shipping.dto.ShipmentTrackingDto;
import com.exhaustedpigeon.ODOP.shipping.dto.UpdateShipmentStatusRequest;
import com.exhaustedpigeon.ODOP.shipping.model.Shipment;
import com.exhaustedpigeon.ODOP.shipping.service.ShippingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for Shipping and Tracking operations
 */
@RestController
@RequestMapping("/odop/shipping")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ShippingController {
    
    private final ShippingService shippingService;
    
    // ============================================
    // PUBLIC TRACKING ENDPOINTS (No Auth Required)
    // ============================================
    
    /**
     * Track shipment by tracking number (public)
     * GET /odop/shipping/track/{trackingNumber}
     */
    @GetMapping("/track/{trackingNumber}")
    public ResponseEntity<ShipmentTrackingDto> trackByTrackingNumber(
            @PathVariable String trackingNumber) {
        log.info("Tracking request for: {}", trackingNumber);
        
        ShipmentTrackingDto tracking = shippingService.trackByTrackingNumber(trackingNumber);
        return ResponseEntity.ok(tracking);
    }
    
    /**
     * Track shipment by order ID
     * GET /odop/shipping/track/order/{orderId}
     */
    @GetMapping("/track/order/{orderId}")
    public ResponseEntity<ShipmentTrackingDto> trackByOrderId(
            @PathVariable String orderId) {
        log.info("Tracking request for order: {}", orderId);
        
        ShipmentTrackingDto tracking = shippingService.trackByOrderId(orderId);
        return ResponseEntity.ok(tracking);
    }
    
    // ============================================
    // CUSTOMER ENDPOINTS
    // ============================================
    
    /**
     * Get all shipments for a customer
     * GET /odop/shipping/customer/{customerId}
     */
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<ShipmentTrackingDto>> getCustomerShipments(
            @PathVariable String customerId) {
        log.info("Getting shipments for customer: {}", customerId);
        
        List<ShipmentTrackingDto> shipments = shippingService.getCustomerShipments(customerId);
        return ResponseEntity.ok(shipments);
    }
    
    /**
     * Get active shipments for a customer (not delivered/cancelled)
     * GET /odop/shipping/customer/{customerId}/active
     */
    @GetMapping("/customer/{customerId}/active")
    public ResponseEntity<List<ShipmentTrackingDto>> getActiveShipments(
            @PathVariable String customerId) {
        log.info("Getting active shipments for customer: {}", customerId);
        
        List<ShipmentTrackingDto> shipments = shippingService.getActiveShipments(customerId);
        return ResponseEntity.ok(shipments);
    }
    
    // ============================================
    // VENDOR ENDPOINTS
    // ============================================
    
    /**
     * Get all shipments for a vendor
     * GET /odop/shipping/vendor/{vendorId}
     */
    @GetMapping("/vendor/{vendorId}")
    public ResponseEntity<List<ShipmentTrackingDto>> getVendorShipments(
            @PathVariable String vendorId) {
        log.info("Getting shipments for vendor: {}", vendorId);
        
        List<ShipmentTrackingDto> shipments = shippingService.getVendorShipments(vendorId);
        return ResponseEntity.ok(shipments);
    }
    
    /**
     * Get vendor shipment statistics
     * GET /odop/shipping/vendor/{vendorId}/stats
     */
    @GetMapping("/vendor/{vendorId}/stats")
    public ResponseEntity<ShippingService.VendorShipmentStats> getVendorStats(
            @PathVariable String vendorId) {
        log.info("Getting shipment stats for vendor: {}", vendorId);
        
        ShippingService.VendorShipmentStats stats = shippingService.getVendorStats(vendorId);
        return ResponseEntity.ok(stats);
    }
    
    // ============================================
    // ADMIN/INTERNAL ENDPOINTS
    // ============================================
    
    /**
     * Create a new shipment
     * POST /odop/shipping/create
     */
    @PostMapping("/create")
    public ResponseEntity<ShipmentTrackingDto> createShipment(
            @Valid @RequestBody CreateShipmentRequest request) {
        log.info("Creating shipment for order: {}", request.getOrderId());
        
        Shipment shipment = shippingService.createShipment(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ShipmentTrackingDto.fromShipment(shipment));
    }
    
    /**
     * Update shipment status
     * PUT /odop/shipping/{trackingNumber}/status
     */
    @PutMapping("/{trackingNumber}/status")
    public ResponseEntity<ShipmentTrackingDto> updateStatus(
            @PathVariable String trackingNumber,
            @Valid @RequestBody UpdateShipmentStatusRequest request) {
        log.info("Updating status for shipment: {} to {}", trackingNumber, request.getStatus());
        
        Shipment shipment = shippingService.updateShipmentStatus(trackingNumber, request);
        return ResponseEntity.ok(ShipmentTrackingDto.fromShipment(shipment));
    }
    
    /**
     * Assign courier to shipment
     * POST /odop/shipping/{trackingNumber}/assign-courier
     */
    @PostMapping("/{trackingNumber}/assign-courier")
    public ResponseEntity<ShipmentTrackingDto> assignCourier(
            @PathVariable String trackingNumber,
            @RequestBody Map<String, String> courierDetails) {
        log.info("Assigning courier to shipment: {}", trackingNumber);
        
        Shipment shipment = shippingService.assignCourier(
                trackingNumber,
                courierDetails.get("courierName"),
                courierDetails.get("courierCode"),
                courierDetails.get("courierTrackingId")
        );
        return ResponseEntity.ok(ShipmentTrackingDto.fromShipment(shipment));
    }
    
    /**
     * Create return shipment
     * POST /odop/shipping/{trackingNumber}/return
     */
    @PostMapping("/{trackingNumber}/return")
    public ResponseEntity<ShipmentTrackingDto> createReturn(
            @PathVariable String trackingNumber,
            @RequestBody Map<String, String> returnDetails) {
        log.info("Creating return shipment for: {}", trackingNumber);
        
        Shipment returnShipment = shippingService.createReturnShipment(
                trackingNumber,
                returnDetails.get("reason")
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ShipmentTrackingDto.fromShipment(returnShipment));
    }
    
    // ============================================
    // EXCEPTION HANDLERS
    // ============================================
    
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
        log.error("Shipping error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage()));
    }
    
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleIllegalStateException(IllegalStateException ex) {
        log.error("Shipping state error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage()));
    }
}
