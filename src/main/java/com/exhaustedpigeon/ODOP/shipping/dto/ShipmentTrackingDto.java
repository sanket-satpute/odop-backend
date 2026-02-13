package com.exhaustedpigeon.ODOP.shipping.dto;

import com.exhaustedpigeon.ODOP.shipping.model.Shipment;
import com.exhaustedpigeon.ODOP.shipping.model.ShipmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for shipment tracking response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentTrackingDto {
    
    private String trackingNumber;
    private String orderId;
    
    // Current Status
    private ShipmentStatus currentStatus;
    private String statusDisplayName;
    private String statusDescription;
    
    // Courier Info
    private String courierName;
    private String courierTrackingId;
    private String courierTrackingUrl;  // Link to courier's tracking page
    
    // Shipping Mode
    private String shippingMode;
    private String paymentMode;
    
    // Addresses
    private AddressDto pickupAddress;
    private AddressDto deliveryAddress;
    
    // Timeline
    private LocalDateTime orderDate;
    private LocalDateTime dispatchedAt;
    private LocalDateTime estimatedDelivery;
    private LocalDateTime actualDelivery;
    
    // Progress
    private Integer progressPercentage;
    private String nextExpectedStatus;
    
    // Tracking History
    private List<TrackingEventDto> trackingHistory;
    
    // Delivery Details (if delivered)
    private String deliveredTo;
    private String deliveryProofUrl;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddressDto {
        private String name;
        private String phone;
        private String addressLine1;
        private String addressLine2;
        private String city;
        private String state;
        private String pincode;
        private String landmark;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrackingEventDto {
        private LocalDateTime timestamp;
        private String status;
        private String statusDisplayName;
        private String location;
        private String description;
        private String icon;            // For frontend display
        private Boolean isCompleted;
    }
    
    /**
     * Convert from Shipment entity
     */
    public static ShipmentTrackingDto fromShipment(Shipment shipment) {
        return ShipmentTrackingDto.builder()
                .trackingNumber(shipment.getTrackingNumber())
                .orderId(shipment.getOrderId())
                .currentStatus(shipment.getStatus())
                .statusDisplayName(shipment.getStatus().getDisplayName())
                .statusDescription(shipment.getStatusDescription())
                .courierName(shipment.getCourierName())
                .courierTrackingId(shipment.getCourierTrackingId())
                .courierTrackingUrl(generateCourierTrackingUrl(shipment))
                .shippingMode(shipment.getShippingMode())
                .paymentMode(shipment.getPaymentMode())
                .pickupAddress(convertAddress(shipment.getPickupAddress()))
                .deliveryAddress(convertAddress(shipment.getDeliveryAddress()))
                .orderDate(shipment.getCreatedAt())
                .dispatchedAt(shipment.getDispatchedAt())
                .estimatedDelivery(shipment.getEstimatedDeliveryDate())
                .actualDelivery(shipment.getActualDeliveryDate())
                .progressPercentage(calculateProgress(shipment.getStatus()))
                .nextExpectedStatus(getNextExpectedStatus(shipment.getStatus()))
                .trackingHistory(convertTrackingHistory(shipment.getTrackingHistory()))
                .deliveredTo(shipment.getDeliveredTo())
                .deliveryProofUrl(shipment.getDeliveryProofUrl())
                .build();
    }
    
    private static AddressDto convertAddress(Shipment.ShippingAddress address) {
        if (address == null) return null;
        return AddressDto.builder()
                .name(address.getName())
                .phone(address.getPhone())
                .addressLine1(address.getAddressLine1())
                .addressLine2(address.getAddressLine2())
                .city(address.getCity())
                .state(address.getState())
                .pincode(address.getPincode())
                .landmark(address.getLandmark())
                .build();
    }
    
    private static List<TrackingEventDto> convertTrackingHistory(List<Shipment.TrackingEvent> events) {
        if (events == null) return List.of();
        return events.stream()
                .map(e -> TrackingEventDto.builder()
                        .timestamp(e.getTimestamp())
                        .status(e.getStatus().name())
                        .statusDisplayName(e.getStatus().getDisplayName())
                        .location(e.getLocation())
                        .description(e.getDescription())
                        .icon(getStatusIcon(e.getStatus()))
                        .isCompleted(true)
                        .build())
                .toList();
    }
    
    private static String generateCourierTrackingUrl(Shipment shipment) {
        if (shipment.getCourierCode() == null || shipment.getCourierTrackingId() == null) {
            return null;
        }
        
        return switch (shipment.getCourierCode().toLowerCase()) {
            case "delhivery" -> "https://www.delhivery.com/track/package/" + shipment.getCourierTrackingId();
            case "bluedart" -> "https://www.bluedart.com/tracking/" + shipment.getCourierTrackingId();
            case "shiprocket" -> "https://shiprocket.co/tracking/" + shipment.getCourierTrackingId();
            case "dtdc" -> "https://www.dtdc.in/tracking.asp?strCnno=" + shipment.getCourierTrackingId();
            case "fedex" -> "https://www.fedex.com/fedextrack/?trknbr=" + shipment.getCourierTrackingId();
            default -> null;
        };
    }
    
    private static Integer calculateProgress(ShipmentStatus status) {
        return switch (status) {
            case ORDER_PLACED -> 5;
            case ORDER_CONFIRMED -> 10;
            case PROCESSING -> 20;
            case READY_FOR_PICKUP -> 30;
            case PICKED_UP -> 40;
            case IN_TRANSIT_TO_HUB -> 50;
            case REACHED_HUB -> 60;
            case IN_TRANSIT -> 70;
            case OUT_FOR_DELIVERY -> 90;
            case DELIVERED -> 100;
            case DELIVERY_ATTEMPTED, RESCHEDULED -> 85;
            case RETURNED, CANCELLED, LOST, DAMAGED -> 0;
        };
    }
    
    private static String getNextExpectedStatus(ShipmentStatus status) {
        return switch (status) {
            case ORDER_PLACED -> "Order Confirmation";
            case ORDER_CONFIRMED -> "Processing";
            case PROCESSING -> "Ready for Pickup";
            case READY_FOR_PICKUP -> "Pickup by Courier";
            case PICKED_UP -> "In Transit to Hub";
            case IN_TRANSIT_TO_HUB -> "Reached Distribution Hub";
            case REACHED_HUB -> "In Transit to Your City";
            case IN_TRANSIT -> "Out for Delivery";
            case OUT_FOR_DELIVERY -> "Delivery";
            case DELIVERY_ATTEMPTED -> "Rescheduled Delivery";
            case RESCHEDULED -> "Out for Delivery";
            case DELIVERED, RETURNED, CANCELLED, LOST, DAMAGED -> null;
        };
    }
    
    private static String getStatusIcon(ShipmentStatus status) {
        return switch (status) {
            case ORDER_PLACED, ORDER_CONFIRMED -> "shopping_cart";
            case PROCESSING -> "inventory";
            case READY_FOR_PICKUP, PICKED_UP -> "local_shipping";
            case IN_TRANSIT_TO_HUB, REACHED_HUB -> "warehouse";
            case IN_TRANSIT -> "flight";
            case OUT_FOR_DELIVERY -> "delivery_dining";
            case DELIVERED -> "check_circle";
            case DELIVERY_ATTEMPTED -> "error";
            case RESCHEDULED -> "schedule";
            case RETURNED -> "undo";
            case CANCELLED -> "cancel";
            case LOST, DAMAGED -> "warning";
        };
    }
}
