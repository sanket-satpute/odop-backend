package com.exhaustedpigeon.ODOP.shipping.dto;

import com.exhaustedpigeon.ODOP.shipping.model.Shipment;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a new shipment
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateShipmentRequest {
    
    @NotBlank(message = "Order ID is required")
    private String orderId;
    
    @NotBlank(message = "Customer ID is required")
    private String customerId;
    
    @NotBlank(message = "Vendor ID is required")
    private String vendorId;
    
    // Pickup Address (Vendor)
    @NotNull(message = "Pickup address is required")
    private AddressRequest pickupAddress;
    
    // Delivery Address (Customer)
    @NotNull(message = "Delivery address is required")
    private AddressRequest deliveryAddress;
    
    // Package Details
    private Double weight;
    private Double length;
    private Double width;
    private Double height;
    private Integer numberOfItems;
    private String packageType;
    
    // Shipping Options
    private String shippingMode;        // Standard, Express, Same-Day
    private String paymentMode;         // Prepaid, COD
    private String preferredCourier;    // Optional: specific courier preference
    
    // Notifications
    @Builder.Default
    private Boolean smsNotification = true;
    @Builder.Default
    private Boolean emailNotification = true;
    @Builder.Default
    private Boolean whatsappNotification = false;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddressRequest {
        @NotBlank(message = "Name is required")
        private String name;
        
        @NotBlank(message = "Phone is required")
        private String phone;
        
        private String email;
        
        @NotBlank(message = "Address line 1 is required")
        private String addressLine1;
        
        private String addressLine2;
        
        @NotBlank(message = "City is required")
        private String city;
        
        @NotBlank(message = "State is required")
        private String state;
        
        @NotBlank(message = "Pincode is required")
        private String pincode;
        
        @Builder.Default
        private String country = "India";
        
        private String landmark;
    }
    
    /**
     * Convert address request to shipping address model
     */
    public static Shipment.ShippingAddress toShippingAddress(AddressRequest request) {
        if (request == null) return null;
        return Shipment.ShippingAddress.builder()
                .name(request.getName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .addressLine1(request.getAddressLine1())
                .addressLine2(request.getAddressLine2())
                .city(request.getCity())
                .state(request.getState())
                .pincode(request.getPincode())
                .country(request.getCountry())
                .landmark(request.getLandmark())
                .build();
    }
}
