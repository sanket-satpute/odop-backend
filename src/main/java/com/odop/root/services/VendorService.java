package com.odop.root.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.odop.root.models.Vendor;
import com.odop.root.repository.VendorRepository;

@Service
public class VendorService {

	@Autowired
	VendorRepository vendorRepo;

	@Autowired
	private PasswordEncoder passwordEncoder;

	private static final Logger logger = LogManager.getLogger(VendorService.class);
	
	public Vendor saveVendor(Vendor vendor) {
		// Generate UUID if not provided (new registration)
		if (vendor.getVendorId() == null || vendor.getVendorId().isEmpty()) {
			vendor.setVendorId(java.util.UUID.randomUUID().toString());
		}
		
		// Only encode password if it's not already encoded (new or password change)
		if (vendor.getPassword() != null && !vendor.getPassword().startsWith("$2a$")) {
			vendor.setPassword(passwordEncoder.encode(vendor.getPassword()));
		}
		
		// Set roles if not set
		if (vendor.getRoles() == null || vendor.getRoles().isEmpty()) {
			vendor.setRoles(List.of("ROLE_VENDOR"));
			logger.info("Setting ROLE_VENDOR for vendor: " + vendor.getEmailAddress());
		}
		logger.info("Vendor roles before save: " + vendor.getRoles());
		
		// Set timestamps
		if (vendor.getCreatedAt() == null) {
			vendor.setCreatedAt(java.time.LocalDateTime.now());
		}
		vendor.setUpdatedAt(java.time.LocalDateTime.now());
		
		Vendor saved = this.vendorRepo.save(vendor);
		logger.info("Saved vendor roles: " + saved.getRoles());
		return saved;
	}
	
	public List<Vendor> getAllVendors() {
		return this.vendorRepo.findAll();
	}
	
	public Vendor getVendorById(String vendorId) {
		return this.vendorRepo.findByVendorId(vendorId);
	}
	
	public Vendor getVendorByEmailAndPasswordAndBussinessORTaxNo(String email, String password, String registryOrTaxNo) {
		Vendor vendor = vendorRepo.findByEmailAddressAndBusinessRegistryNumber(email, registryOrTaxNo);
		if (vendor != null && passwordEncoder.matches(password, vendor.getPassword())) {
			return vendor;
		}
		return null;
	}
	
	public Vendor getVendorByEmailAndPassword(String email, String password) {
		Vendor vendor = vendorRepo.findByEmailAddress(email);
		if (vendor != null && passwordEncoder.matches(password, vendor.getPassword())) {
			return vendor;
		}
		return null;
	}
	
	public Vendor getVendorByEmail(String email_address, String password, String registry_no) {
		Vendor vend = this.vendorRepo.findByEmailAddressAndBusinessRegistryNumber(email_address, registry_no);
		if (vend != null && passwordEncoder.matches(password, vend.getPassword())) {
			logger.debug("{}", vend);
			return vend;
		}
		return null;
	}
	
	public boolean deleteById(String adminId) {
		if(this.getVendorById(adminId) != null) {
			this.vendorRepo.deleteById(adminId);
			return (this.getVendorById(adminId)!= null);
		}
		return false;
	}

	public Vendor updateVendor(Vendor vendor, String vendorId) {
		Vendor existingVendor = vendorRepo.findById(vendorId)
				.orElseThrow(() -> new RuntimeException("Vendor not found with id: " + vendorId));

		existingVendor.setShoppeeName(vendor.getShoppeeName());
		existingVendor.setShopkeeperName(vendor.getShopkeeperName());
		existingVendor.setEmailAddress(vendor.getEmailAddress());
		existingVendor.setContactNumber(vendor.getContactNumber());
		existingVendor.setShoppeeAddress(vendor.getShoppeeAddress());
		existingVendor.setLocationDistrict(vendor.getLocationDistrict());
		existingVendor.setLocationState(vendor.getLocationState());
		existingVendor.setPinCode(vendor.getPinCode());
		existingVendor.setShoppeeName(vendor.getShoppeeName());
		existingVendor.setBusinessRegistryNumber(vendor.getBusinessRegistryNumber());
		existingVendor.setStatus(vendor.getStatus());
		existingVendor.setDeliveryAvailable(vendor.getDeliveryAvailable());
		existingVendor.setDeliveryRadiusInKm(vendor.getDeliveryRadiusInKm());
		existingVendor.setUpdatedAt(java.time.LocalDateTime.now());

		if (vendor.getPassword() != null && !vendor.getPassword().isEmpty()) {
			existingVendor.setPassword(passwordEncoder.encode(vendor.getPassword()));
		}
		return vendorRepo.save(existingVendor);
	}
	
	public Vendor updateVendorStatus(String id, String status) {
	    Vendor vendor = vendorRepo.findById(id).orElseThrow(() -> new RuntimeException("Vendor not found"));
	    vendor.setStatus(status);
	    return vendorRepo.save(vendor);
	  }

	public List<Vendor> getVendorsByLocation(String district, String state) {
		return vendorRepo.findByLocationDistrictAndLocationState(district, state);
	}

	public List<Vendor> getVendorsByState(String state) {
		return vendorRepo.findByLocationState(state);
	}

	// This method can be enhanced with actual geo-spatial queries if latitude/longitude are added to Vendor model
	public List<Vendor> getVendorsWithDelivery(String district, String state) {
		return vendorRepo.findByLocationDistrictAndLocationState(district, state)
				.stream()
				.filter(Vendor::getDeliveryAvailable)
				.collect(Collectors.toList());
	}
}
