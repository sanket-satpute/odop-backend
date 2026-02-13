package com.exhaustedpigeon.ODOP.shipping.model;

/**
 * Enum representing various shipment statuses in the delivery lifecycle
 */
public enum ShipmentStatus {
    // Order Processing
    ORDER_PLACED("Order Placed", "Your order has been successfully placed"),
    ORDER_CONFIRMED("Order Confirmed", "Your order has been confirmed by the seller"),
    PROCESSING("Processing", "Your order is being processed"),
    
    // Pickup & Warehouse
    READY_FOR_PICKUP("Ready for Pickup", "Package is ready for courier pickup"),
    PICKED_UP("Picked Up", "Package has been picked up by courier"),
    IN_TRANSIT_TO_HUB("In Transit to Hub", "Package is on the way to the distribution hub"),
    REACHED_HUB("Reached Hub", "Package has arrived at the distribution hub"),
    
    // In Transit
    IN_TRANSIT("In Transit", "Package is on the way to your location"),
    OUT_FOR_DELIVERY("Out for Delivery", "Package is out for delivery"),
    
    // Delivery Attempts
    DELIVERY_ATTEMPTED("Delivery Attempted", "Delivery was attempted but unsuccessful"),
    RESCHEDULED("Rescheduled", "Delivery has been rescheduled"),
    
    // Final Statuses
    DELIVERED("Delivered", "Package has been delivered successfully"),
    RETURNED("Returned", "Package has been returned to seller"),
    CANCELLED("Cancelled", "Shipment has been cancelled"),
    LOST("Lost", "Package is lost in transit"),
    DAMAGED("Damaged", "Package was damaged during transit");

    private final String displayName;
    private final String description;

    ShipmentStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
