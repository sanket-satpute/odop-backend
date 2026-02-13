package com.exhaustedpigeon.ODOP.shipping.service;

import com.exhaustedpigeon.ODOP.shipping.dto.CreateShipmentRequest;
import com.exhaustedpigeon.ODOP.shipping.dto.ShipmentTrackingDto;
import com.exhaustedpigeon.ODOP.shipping.dto.UpdateShipmentStatusRequest;
import com.exhaustedpigeon.ODOP.shipping.model.Shipment;
import com.exhaustedpigeon.ODOP.shipping.model.ShipmentStatus;
import com.exhaustedpigeon.ODOP.shipping.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for managing shipments and tracking
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ShippingService {
    
    private final ShipmentRepository shipmentRepository;
    
    // Tracking number prefix for ODOP
    private static final String TRACKING_PREFIX = "ODOP";
    
    /**
     * Create a new shipment for an order
     */
    @Transactional
    public Shipment createShipment(CreateShipmentRequest request) {
        // Check if shipment already exists for this order
        if (shipmentRepository.existsByOrderId(request.getOrderId())) {
            throw new IllegalStateException("Shipment already exists for order: " + request.getOrderId());
        }
        
        // Generate tracking number
        String trackingNumber = Shipment.generateTrackingNumber(TRACKING_PREFIX);
        
        // Calculate estimated delivery (3-7 days based on mode)
        LocalDateTime estimatedDelivery = calculateEstimatedDelivery(request.getShippingMode());
        
        // Create shipment
        Shipment shipment = Shipment.builder()
                .trackingNumber(trackingNumber)
                .orderId(request.getOrderId())
                .customerId(request.getCustomerId())
                .vendorId(request.getVendorId())
                .pickupAddress(CreateShipmentRequest.toShippingAddress(request.getPickupAddress()))
                .deliveryAddress(CreateShipmentRequest.toShippingAddress(request.getDeliveryAddress()))
                .weight(request.getWeight())
                .length(request.getLength())
                .width(request.getWidth())
                .height(request.getHeight())
                .numberOfItems(request.getNumberOfItems())
                .packageType(request.getPackageType())
                .shippingMode(request.getShippingMode() != null ? request.getShippingMode() : "Standard")
                .paymentMode(request.getPaymentMode() != null ? request.getPaymentMode() : "Prepaid")
                .status(ShipmentStatus.ORDER_PLACED)
                .statusDescription(ShipmentStatus.ORDER_PLACED.getDescription())
                .createdAt(LocalDateTime.now())
                .lastUpdatedAt(LocalDateTime.now())
                .estimatedDeliveryDate(estimatedDelivery)
                .smsNotificationEnabled(request.getSmsNotification())
                .emailNotificationEnabled(request.getEmailNotification())
                .whatsappNotificationEnabled(request.getWhatsappNotification())
                .isReturnShipment(false)
                .build();
        
        // Add initial tracking event
        shipment.addTrackingEvent(Shipment.TrackingEvent.builder()
                .timestamp(LocalDateTime.now())
                .status(ShipmentStatus.ORDER_PLACED)
                .location("Online")
                .description("Order placed successfully")
                .updatedBy("System")
                .build());
        
        Shipment saved = shipmentRepository.save(shipment);
        log.info("Created shipment {} for order {}", trackingNumber, request.getOrderId());
        
        return saved;
    }
    
    /**
     * Track shipment by tracking number
     */
    public ShipmentTrackingDto trackByTrackingNumber(String trackingNumber) {
        Shipment shipment = shipmentRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new RuntimeException("Shipment not found: " + trackingNumber));
        
        return ShipmentTrackingDto.fromShipment(shipment);
    }
    
    /**
     * Track shipment by order ID
     */
    public ShipmentTrackingDto trackByOrderId(String orderId) {
        Shipment shipment = shipmentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("No shipment found for order: " + orderId));
        
        return ShipmentTrackingDto.fromShipment(shipment);
    }
    
    /**
     * Update shipment status
     */
    @Transactional
    public Shipment updateShipmentStatus(String trackingNumber, UpdateShipmentStatusRequest request) {
        Shipment shipment = shipmentRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new RuntimeException("Shipment not found: " + trackingNumber));
        
        // Validate status transition
        validateStatusTransition(shipment.getStatus(), request.getStatus());
        
        // Add tracking event
        shipment.addTrackingEvent(Shipment.TrackingEvent.builder()
                .timestamp(LocalDateTime.now())
                .status(request.getStatus())
                .location(request.getLocation())
                .description(request.getDescription() != null ? 
                        request.getDescription() : request.getStatus().getDescription())
                .remarks(request.getRemarks())
                .updatedBy("Admin")
                .build());
        
        // Handle special statuses
        switch (request.getStatus()) {
            case PICKED_UP -> shipment.setPickedUpAt(LocalDateTime.now());
            case IN_TRANSIT -> shipment.setDispatchedAt(LocalDateTime.now());
            case DELIVERED -> {
                shipment.setActualDeliveryDate(LocalDateTime.now());
                shipment.setDeliveredTo(request.getDeliveredTo());
                shipment.setDeliveryProofUrl(request.getDeliveryProofUrl());
            }
            case RETURNED -> shipment.setReturnReason(request.getReturnReason());
            default -> {}
        }
        
        Shipment saved = shipmentRepository.save(shipment);
        log.info("Updated shipment {} status to {}", trackingNumber, request.getStatus());
        
        // Send notification
        sendStatusNotification(shipment);
        
        return saved;
    }
    
    /**
     * Get all shipments for a customer
     */
    public List<ShipmentTrackingDto> getCustomerShipments(String customerId) {
        return shipmentRepository.findByCustomerIdOrderByCreatedAtDesc(customerId)
                .stream()
                .map(ShipmentTrackingDto::fromShipment)
                .toList();
    }
    
    /**
     * Get active shipments for a customer (not delivered/cancelled)
     */
    public List<ShipmentTrackingDto> getActiveShipments(String customerId) {
        return shipmentRepository.findActiveShipmentsByCustomerId(customerId)
                .stream()
                .map(ShipmentTrackingDto::fromShipment)
                .toList();
    }
    
    /**
     * Get all shipments for a vendor
     */
    public List<ShipmentTrackingDto> getVendorShipments(String vendorId) {
        return shipmentRepository.findByVendorIdOrderByCreatedAtDesc(vendorId)
                .stream()
                .map(ShipmentTrackingDto::fromShipment)
                .toList();
    }
    
    /**
     * Assign courier to shipment
     */
    @Transactional
    public Shipment assignCourier(String trackingNumber, String courierName, 
                                   String courierCode, String courierTrackingId) {
        Shipment shipment = shipmentRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new RuntimeException("Shipment not found: " + trackingNumber));
        
        shipment.setCourierName(courierName);
        shipment.setCourierCode(courierCode);
        shipment.setCourierTrackingId(courierTrackingId);
        
        // Update status to ready for pickup
        if (shipment.getStatus() == ShipmentStatus.ORDER_CONFIRMED || 
            shipment.getStatus() == ShipmentStatus.PROCESSING) {
            shipment.addTrackingEvent(Shipment.TrackingEvent.builder()
                    .timestamp(LocalDateTime.now())
                    .status(ShipmentStatus.READY_FOR_PICKUP)
                    .location(shipment.getPickupAddress().getCity())
                    .description("Courier assigned: " + courierName)
                    .updatedBy("System")
                    .build());
        }
        
        return shipmentRepository.save(shipment);
    }
    
    /**
     * Create return shipment
     */
    @Transactional
    public Shipment createReturnShipment(String originalTrackingNumber, String returnReason) {
        Shipment original = shipmentRepository.findByTrackingNumber(originalTrackingNumber)
                .orElseThrow(() -> new RuntimeException("Original shipment not found"));
        
        if (original.getStatus() != ShipmentStatus.DELIVERED) {
            throw new IllegalStateException("Can only create return for delivered shipments");
        }
        
        // Swap pickup and delivery addresses
        Shipment returnShipment = Shipment.builder()
                .trackingNumber(Shipment.generateTrackingNumber(TRACKING_PREFIX + "R"))
                .orderId(original.getOrderId() + "-RETURN")
                .customerId(original.getCustomerId())
                .vendorId(original.getVendorId())
                .pickupAddress(original.getDeliveryAddress())   // Customer's address
                .deliveryAddress(original.getPickupAddress())   // Vendor's address
                .shippingMode("Standard")
                .paymentMode("Prepaid")
                .status(ShipmentStatus.ORDER_PLACED)
                .statusDescription("Return shipment created")
                .createdAt(LocalDateTime.now())
                .lastUpdatedAt(LocalDateTime.now())
                .estimatedDeliveryDate(LocalDateTime.now().plusDays(7))
                .isReturnShipment(true)
                .returnReason(returnReason)
                .originalShipmentId(original.getId())
                .build();
        
        returnShipment.addTrackingEvent(Shipment.TrackingEvent.builder()
                .timestamp(LocalDateTime.now())
                .status(ShipmentStatus.ORDER_PLACED)
                .location("Online")
                .description("Return shipment initiated: " + returnReason)
                .updatedBy("System")
                .build());
        
        return shipmentRepository.save(returnShipment);
    }
    
    /**
     * Get shipment statistics for vendor dashboard
     */
    public VendorShipmentStats getVendorStats(String vendorId) {
        List<ShipmentStatus> activeStatuses = List.of(
                ShipmentStatus.ORDER_PLACED, ShipmentStatus.ORDER_CONFIRMED,
                ShipmentStatus.PROCESSING, ShipmentStatus.READY_FOR_PICKUP,
                ShipmentStatus.PICKED_UP, ShipmentStatus.IN_TRANSIT_TO_HUB,
                ShipmentStatus.REACHED_HUB, ShipmentStatus.IN_TRANSIT,
                ShipmentStatus.OUT_FOR_DELIVERY
        );
        
        long pending = shipmentRepository.countByVendorIdAndStatusIn(vendorId, 
                List.of(ShipmentStatus.ORDER_PLACED, ShipmentStatus.ORDER_CONFIRMED, ShipmentStatus.PROCESSING));
        long inTransit = shipmentRepository.countByVendorIdAndStatusIn(vendorId,
                List.of(ShipmentStatus.PICKED_UP, ShipmentStatus.IN_TRANSIT_TO_HUB, 
                        ShipmentStatus.REACHED_HUB, ShipmentStatus.IN_TRANSIT));
        long outForDelivery = shipmentRepository.countByVendorIdAndStatusIn(vendorId,
                List.of(ShipmentStatus.OUT_FOR_DELIVERY));
        long delivered = shipmentRepository.countByVendorIdAndStatusIn(vendorId,
                List.of(ShipmentStatus.DELIVERED));
        
        return new VendorShipmentStats(pending, inTransit, outForDelivery, delivered);
    }
    
    /**
     * Calculate estimated delivery based on shipping mode
     */
    private LocalDateTime calculateEstimatedDelivery(String shippingMode) {
        int daysToAdd = switch (shippingMode != null ? shippingMode.toLowerCase() : "standard") {
            case "same-day" -> 0;
            case "express" -> 2;
            case "standard" -> 5;
            default -> 7;
        };
        return LocalDateTime.now().plusDays(daysToAdd);
    }
    
    /**
     * Validate status transitions
     */
    private void validateStatusTransition(ShipmentStatus current, ShipmentStatus next) {
        // Terminal statuses cannot be changed
        if (current == ShipmentStatus.DELIVERED || 
            current == ShipmentStatus.CANCELLED ||
            current == ShipmentStatus.LOST) {
            throw new IllegalStateException("Cannot change status from terminal state: " + current);
        }
        
        // Add more specific transition rules as needed
    }
    
    /**
     * Send notification for status update
     */
    private void sendStatusNotification(Shipment shipment) {
        // TODO: Integrate with notification service
        log.info("Notification sent for shipment {} - Status: {}", 
                shipment.getTrackingNumber(), shipment.getStatus());
    }
    
    /**
     * Stats record for vendor dashboard
     */
    public record VendorShipmentStats(
            long pending,
            long inTransit,
            long outForDelivery,
            long delivered
    ) {}
}
