package com.odop.root.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class VendorRegistrationDto {
    @NotBlank(message = "Shopkeeper name is required")
    @Size(min = 2, max = 100, message = "Shopkeeper name must be between 2 and 100 characters")
    private String shopkeeperName;

    @NotBlank(message = "Shoppee name is required")
    private String shoppeeName;

    @NotBlank(message = "Email address is required")
    @Email(message = "Email address must be valid")
    private String emailAddress;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @Positive(message = "Contact number must be positive")
    private long contactNumber;

    private String shoppeeAddress;
    private String locationDistrict;
    private String locationState;
    @Pattern(regexp = "^[0-9]{6}$", message = "Pin code must be 6 digits")
    private String pinCode;

    private String businessRegistryNumber;
    private String taxIdentificationNumber;
    private String businessLicenseNumber;
    private String completeAddress;

    private String returnPolicy;
    private String termsAndServiceAgreement;
    private String businessDescription;
    private String profilePictureUrl;
    private String websiteUrl;

    private String operatingHours;
    private Boolean deliveryAvailable;
    private Double deliveryRadiusInKm;
    private Double deliveryCharges;
    private Double freeDeliveryAbove;

    private String vendorType;
    private String shopDescription;
    private java.util.List<String> shopImages;
    private java.util.List<String> specializations;

    private Double shopLatitude;
    private Double shopLongitude;
    private String googleMapsLink;

    private Boolean isPhysicalVisitAllowed;
    private String shopTimings;
    private String shopClosedDays;

    private java.util.List<String> deliveryOptions;
    private java.util.List<String> deliveryAreas;

    private Boolean giTagCertified;
}
