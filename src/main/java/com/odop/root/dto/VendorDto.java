package com.odop.root.dto;

import lombok.Data;

@Data
public class VendorDto {
    // Added for AuthController compatibility
    private String fullName;
    private String businessName;
    private String city;
    private String state;
    private String district;
    private Double deliveryCharge;
    private String address;
    private String vendorId;
    private String shoppeeName;
    private String shopkeeperName;
    private String emailAddress;
    private long contactNumber;
    private String shoppeeAddress;
    private String locationDistrict;
    private String locationState;
    private String pinCode;
    private String businessRegistryNumber;
    private String taxIdentificationNumber;
    private String businessLicenseNumber;
    private String completeAddress;

    private java.util.List<String> productIds;
    private java.util.List<String> productCategories;
    private Integer productCount;

    private String returnPolicy;
    private String termsAndServiceAgreement;
    private String businessDescription;
    private String profilePictureUrl;
    private String websiteUrl;
    private java.util.Map<String, String> socialMediaLinks;

    private String assignedAdminManagerId;
    private String status;
    private Boolean verified;

    private String kycStatus;
    private Boolean kycDocumentsUploaded;

    private String operatingHours;
    private Boolean deliveryAvailable;
    private Double deliveryRadiusInKm;
    private Double deliveryCharges;
    private Double freeDeliveryAbove;

    private Double storeCreditsOrWallet;
    private Double ratings;
    private Integer reviewCount;
    private java.util.List<String> tags;
    private String notes;

    // ODOP Shop Details
    private String vendorType;
    private String shopDescription;
    private java.util.List<String> shopImages;
    private java.util.List<String> specializations;

    // Location & Map
    private Double shopLatitude;
    private Double shopLongitude;
    private String googleMapsLink;

    // Physical Visit
    private Boolean isPhysicalVisitAllowed;
    private String shopTimings;
    private String shopClosedDays;

    // Delivery Options
    private java.util.List<String> deliveryOptions;
    private java.util.List<String> deliveryAreas;

    // Verification
    private Boolean giTagCertified;
    private Boolean isVerified;

    private java.util.List<String> roles;

    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime updatedAt;
}
