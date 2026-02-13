package com.odop.root.models;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Customer entity for ODOP e-commerce platform.
 * Uses Lombok for boilerplate code reduction.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "customers")
public class Customer {

    @Id
    private String customerId;
    private String fullName;
    private String emailAddress;
    private String password;
    private long contactNumber;
    private String address;
    private String district;        // Added for ODOP consistency (matching Vendor model)
    private String city;
    private String state;
    private String pinCode;
    
    // Profile
    private String profilePictureUrl;
    private String alternateContactNumber;

    private List<String> cartIds;           // reference to carts
    private List<String> orderIds;          // reference to orders
    private List<String> wishlistProductIds; // wishlist feature
    private List<String> roles;
    private String status;                  // active, inactive, banned
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * Convenience constructor for registration.
     */
    public Customer(String fullName, String emailAddress, String password, long contactNumber) {
        this.fullName = fullName;
        this.emailAddress = emailAddress;
        this.password = password;
        this.contactNumber = contactNumber;
    }
}
