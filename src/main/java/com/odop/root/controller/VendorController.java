package com.odop.root.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.odop.root.dto.VendorDto;
import com.odop.root.dto.VendorRegistrationDto;
import com.odop.root.models.Vendor;
import com.odop.root.services.VendorService;

@RestController
@RequestMapping("odop/vendor")
@CrossOrigin
public class VendorController {

    @Autowired
    private VendorService vendorService;
    private static final Logger logger = LogManager.getLogger(VendorController.class);

    @PostMapping("/create_account")
    public ResponseEntity<VendorDto> createVendorAccount(@RequestBody VendorRegistrationDto registrationDto) {
        Vendor vendor = toEntity(registrationDto);
        Vendor savedVendor = vendorService.saveVendor(vendor);
        return ResponseEntity.ok(toDto(savedVendor));
    }

    @GetMapping("/get_all_vendors")
    public ResponseEntity<List<VendorDto>> getAllVendors() {
        List<Vendor> vendors = this.vendorService.getAllVendors();
        List<VendorDto> vendorDtos = vendors.stream().map(this::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(vendorDtos);
    }

    @GetMapping("/get_vendor_id/{id}")
    public ResponseEntity<VendorDto> getVendorById(@PathVariable("id") String id) {
        Vendor vendor = this.vendorService.getVendorById(id);
        return ResponseEntity.ok(toDto(vendor));
    }

    @PostMapping("/login")
    public ResponseEntity<VendorDto> getVendorByEmailAndPassword(@RequestBody Map<String, String> credentials) {
        String email = credentials.get("emailAddress");
        String password = credentials.get("password");
        Vendor vendor = this.vendorService.getVendorByEmailAndPassword(email, password);
        return ResponseEntity.ok(toDto(vendor));
    }

    @DeleteMapping("/delete_by_id/{id}")
    public ResponseEntity<Boolean> deleteVendorById(@PathVariable("id") String id) {
        return ResponseEntity.ok(this.vendorService.deleteById(id));
    }

    @PutMapping("/update_vendor_by_id/{id}")
    public ResponseEntity<VendorDto> updateVendorById(
            @PathVariable("id") String id,
            @RequestBody VendorDto vendorDto) {
        // First, get existing vendor to preserve roles and password
        Vendor existingVendor = vendorService.getVendorById(id);
        Vendor vendor = toEntity(vendorDto);
        
        // Preserve roles from existing vendor
        if (existingVendor != null && existingVendor.getRoles() != null) {
            vendor.setRoles(existingVendor.getRoles());
        }
        // Preserve password if not being updated
        if (existingVendor != null && (vendor.getPassword() == null || vendor.getPassword().isEmpty())) {
            vendor.setPassword(existingVendor.getPassword());
        }
        
        Vendor updatedVendor = vendorService.saveVendor(vendor);
        return ResponseEntity.ok(toDto(updatedVendor));
    }

    @PatchMapping("/update_status/{vendorId}")
    public ResponseEntity<VendorDto> updateVendorStatus(
            @PathVariable String vendorId,
            @RequestBody Map<String, String> body) {
        String status = body.get("status");
        Vendor updatedVendor = this.vendorService.updateVendorStatus(vendorId, status);
        return ResponseEntity.ok(toDto(updatedVendor));
    }

    @GetMapping("/search_by_location")
    public ResponseEntity<List<VendorDto>> getVendorsByLocation(
            @RequestParam String district,
            @RequestParam String state) {
        List<Vendor> vendors = vendorService.getVendorsByLocation(district, state);
        List<VendorDto> vendorDtos = vendors.stream().map(this::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(vendorDtos);
    }


    @GetMapping("/search_with_delivery")
    public ResponseEntity<List<VendorDto>> getVendorsWithDelivery(
            @RequestParam String district,
            @RequestParam String state) {
        List<Vendor> vendors = vendorService.getVendorsWithDelivery(district, state);
        List<VendorDto> vendorDtos = vendors.stream().map(this::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(vendorDtos);
    }

    @GetMapping("/search_by_state")
    public ResponseEntity<List<VendorDto>> getVendorsByState(@RequestParam String state) {
        List<Vendor> vendors = vendorService.getVendorsByState(state);
        List<VendorDto> vendorDtos = vendors.stream().map(this::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(vendorDtos);
    }

    private VendorDto toDto(Vendor vendor) {
        VendorDto dto = new VendorDto();
        dto.setVendorId(vendor.getVendorId());
        dto.setShoppeeName(vendor.getShoppeeName());
        dto.setShopkeeperName(vendor.getShopkeeperName());
        dto.setEmailAddress(vendor.getEmailAddress());
        dto.setContactNumber(vendor.getContactNumber());
        dto.setShoppeeAddress(vendor.getShoppeeAddress());
        dto.setLocationDistrict(vendor.getLocationDistrict());
        dto.setLocationState(vendor.getLocationState());
        dto.setPinCode(vendor.getPinCode());
        dto.setBusinessRegistryNumber(vendor.getBusinessRegistryNumber());
        dto.setStatus(vendor.getStatus());
        // Also set the alias fields for compatibility
        dto.setFullName(vendor.getShopkeeperName());
        dto.setBusinessName(vendor.getShoppeeName());
        dto.setAddress(vendor.getShoppeeAddress());
        dto.setCity(vendor.getLocationDistrict());
        dto.setState(vendor.getLocationState());
        return dto;
    }

    private Vendor toEntity(VendorRegistrationDto dto) {
        Vendor vendor = new Vendor();
        vendor.setShopkeeperName(dto.getShopkeeperName());
        vendor.setEmailAddress(dto.getEmailAddress());
        vendor.setPassword(dto.getPassword());
        vendor.setContactNumber(dto.getContactNumber());
        vendor.setShoppeeAddress(dto.getShoppeeAddress());
        vendor.setLocationDistrict(dto.getLocationDistrict());
        vendor.setLocationState(dto.getLocationState());
        vendor.setPinCode(dto.getPinCode());
        vendor.setShoppeeName(dto.getShoppeeName());
        vendor.setBusinessRegistryNumber(dto.getBusinessRegistryNumber());
        // Additional fields from registration form
        vendor.setTaxIdentificationNumber(dto.getTaxIdentificationNumber());
        vendor.setBusinessLicenseNumber(dto.getBusinessLicenseNumber());
        vendor.setCompleteAddress(dto.getCompleteAddress());
        vendor.setReturnPolicy(dto.getReturnPolicy());
        vendor.setTermsAndServiceAgreement(dto.getTermsAndServiceAgreement());
        vendor.setBusinessDescription(dto.getBusinessDescription());
        vendor.setProfilePictureUrl(dto.getProfilePictureUrl());
        vendor.setWebsiteUrl(dto.getWebsiteUrl());
        vendor.setOperatingHours(dto.getOperatingHours());
        vendor.setDeliveryAvailable(dto.getDeliveryAvailable());
        vendor.setDeliveryRadiusInKm(dto.getDeliveryRadiusInKm());
        vendor.setDeliveryCharges(dto.getDeliveryCharges());
        vendor.setFreeDeliveryAbove(dto.getFreeDeliveryAbove());
        vendor.setVendorType(dto.getVendorType());
        vendor.setShopDescription(dto.getShopDescription());
        // Set default status for new registrations
        vendor.setStatus("pending");
        vendor.setVerified(false);
        return vendor;
    }

    private Vendor toEntity(VendorDto dto) {
        Vendor vendor = new Vendor();
        vendor.setVendorId(dto.getVendorId());
        vendor.setShopkeeperName(dto.getShopkeeperName());
        vendor.setEmailAddress(dto.getEmailAddress());
        vendor.setContactNumber(dto.getContactNumber());
        vendor.setShoppeeAddress(dto.getShoppeeAddress());
        vendor.setLocationDistrict(dto.getLocationDistrict());
        vendor.setLocationState(dto.getLocationState());
        vendor.setPinCode(dto.getPinCode());
        vendor.setShoppeeName(dto.getShoppeeName());
        vendor.setBusinessRegistryNumber(dto.getBusinessRegistryNumber());
        vendor.setStatus(dto.getStatus());
        return vendor;
    }
}
