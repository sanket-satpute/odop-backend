package com.exhaustedpigeon.ODOP.shipping.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Shipment model for tracking order deliveries
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "shipments")
public class Shipment {
    
    @Id
    private String id;
    
    @Indexed(unique = true)
    private String trackingNumber;
    
    @Indexed
    private String orderId;
    
    @Indexed
    private String customerId;
    
    @Indexed
    private String vendorId;
    
    // Courier/Carrier Information
    private String courierName;         // Shiprocket, Delhivery, BlueDart, etc.
    private String courierTrackingId;   // External tracking ID from courier
    private String courierCode;         // Courier code for API integration
    
    // Shipment Details
    private ShipmentStatus status;
    private String statusDescription;
    
    // Addresses
    private ShippingAddress pickupAddress;
    private ShippingAddress deliveryAddress;
    
    // Package Details
    private Double weight;              // in kg
    private Double length;              // in cm
    private Double width;               // in cm
    private Double height;              // in cm
    private Integer numberOfItems;
    private String packageType;         // Box, Envelope, etc.
    
    // Shipping Cost & Mode
    private Double shippingCost;
    private String shippingMode;        // Standard, Express, Same-Day
    private String paymentMode;         // Prepaid, COD
    
    // Timeline
    private LocalDateTime createdAt;
    private LocalDateTime pickedUpAt;
    private LocalDateTime dispatchedAt;
    private LocalDateTime estimatedDeliveryDate;
    private LocalDateTime actualDeliveryDate;
    private LocalDateTime lastUpdatedAt;
    
    // Tracking History
    @Builder.Default
    private List<TrackingEvent> trackingHistory = new ArrayList<>();
    
    // Delivery Details
    private String deliveredTo;         // Name of person who received
    private String deliveryProofUrl;    // Image/signature proof
    private String deliveryNotes;
    
    // Return/Issues
    private Boolean isReturnShipment;
    private String returnReason;
    private String originalShipmentId;  // For returns
    
    // Notifications
    private Boolean smsNotificationEnabled;
    private Boolean emailNotificationEnabled;
    private Boolean whatsappNotificationEnabled;
    
    /**
     * Inner class for shipping address
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShippingAddress {
        private String name;
        private String phone;
        private String email;
        private String addressLine1;
        private String addressLine2;
        private String city;
        private String state;
        private String pincode;
        private String country;
        private String landmark;
        private Double latitude;
        private Double longitude;
    }
    
    /**
     * Inner class for tracking events
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrackingEvent {
        private LocalDateTime timestamp;
        private ShipmentStatus status;
        private String location;
        private String description;
        private String remarks;
        private String updatedBy;       // System/Courier/Admin
    }
    
    /**
     * Add a tracking event to history
     */
    public void addTrackingEvent(TrackingEvent event) {
        if (this.trackingHistory == null) {
            this.trackingHistory = new ArrayList<>();
        }
        this.trackingHistory.add(event);
        this.status = event.getStatus();
        this.statusDescription = event.getDescription();
        this.lastUpdatedAt = event.getTimestamp();
    }
    
    /**
     * Generate tracking number
     */
    public static String generateTrackingNumber(String prefix) {
        long timestamp = System.currentTimeMillis();
        int random = (int) (Math.random() * 10000);
        return String.format("%s%d%04d", prefix, timestamp % 100000000, random);
    }
}
