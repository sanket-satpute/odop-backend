package com.odop.root.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Document(collection = "vendors")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vendor {

    @Id
    private String vendorId;
    private String shoppeeName;
    private String shopkeeperName;
    private String emailAddress;
    private String password;
    private long contactNumber;
    private String shoppeeAddress;
    private String locationDistrict;
    private String locationState;
    private String pinCode;
    private String businessRegistryNumber;
    private String taxIdentificationNumber;
    private String businessLicenseNumber;
    private String completeAddress;

    private List<String> productIds; // references to products
    private List<String> productCategories; // references to categories
    private Integer productCount;

    private String returnPolicy;
    private String termsAndServiceAgreement;
    private String businessDescription;
    private String profilePictureUrl;
    private String websiteUrl;
    private Map<String, String> socialMediaLinks;

    private String assignedAdminManagerId;
    private String status; // verified, pending, rejected
    private Boolean verified;

    private String kycStatus;
    private Boolean kycDocumentsUploaded;

    private String operatingHours;
    private Boolean deliveryAvailable;
    private Double deliveryRadiusInKm;
    private Double deliveryCharges;

    private Double storeCreditsOrWallet;
    private Double ratings;
    private Integer reviewCount;
    private List<String> tags;
    private String notes;

    // ========== ODOP Shop Details ==========
    private String vendorType;           // small (hat-gaadi), medium, large
    private String shopDescription;      // About the shop
    private List<String> shopImages;     // Shop photo URLs
    private List<String> specializations;// What products they specialize in

    // ========== Location & Map ==========
    private Double shopLatitude;         // For map display
    private Double shopLongitude;        // For map display
    private String googleMapsLink;       // Direct Google Maps link

    // ========== Physical Visit ==========
    private Boolean isPhysicalVisitAllowed;  // Can customer visit shop?
    private String shopTimings;              // e.g., "9 AM - 8 PM"
    private String shopClosedDays;           // e.g., "Sunday"

    // ========== Delivery Options ==========
    private List<String> deliveryOptions;    // post, courier, local, pickup
    private List<String> deliveryAreas;      // Districts/states they deliver to
    private Double freeDeliveryAbove;        // Free delivery above this amount

    // ========== Verification ==========
    private Boolean giTagCertified;         // Has GI tag certification
    private Boolean isVerified;             // Admin verified vendor

    private List<String> roles;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
