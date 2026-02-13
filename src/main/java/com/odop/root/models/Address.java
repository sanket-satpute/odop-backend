package com.odop.root.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

/**
 * Address entity for customer shipping/billing addresses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "addresses")
public class Address {

    @Id
    private String addressId;
    
    private String customerId;
    
    // Address type: home, work, other
    private String type;
    
    // Contact info for this address
    private String name;
    private String phone;
    
    // Address details
    private String addressLine1;
    private String addressLine2;
    private String landmark;
    private String city;
    private String district;
    private String state;
    private String pincode;
    private String country;
    
    // Flags
    private boolean isDefault;
    private boolean isActive;
    
    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * Set timestamps before save
     */
    public void preSave() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        this.updatedAt = LocalDateTime.now();
        if (this.country == null || this.country.isEmpty()) {
            this.country = "India";
        }
        this.isActive = true;
    }
}
