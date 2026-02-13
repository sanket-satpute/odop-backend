package com.odop.root.controller;

import com.odop.root.models.Address;
import com.odop.root.services.AddressService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for managing customer addresses.
 */
@RestController
@RequestMapping("odop/customer/{customerId}/addresses")
@CrossOrigin
public class AddressController {

    private static final Logger logger = LogManager.getLogger(AddressController.class);

    @Autowired
    private AddressService addressService;

    /**
     * Get all addresses for a customer
     */
    @GetMapping
    public ResponseEntity<List<Address>> getCustomerAddresses(@PathVariable String customerId) {
        logger.info("Getting addresses for customer: {}", customerId);
        List<Address> addresses = addressService.getCustomerAddresses(customerId);
        return ResponseEntity.ok(addresses);
    }

    /**
     * Get a specific address by ID
     */
    @GetMapping("/{addressId}")
    public ResponseEntity<Address> getAddressById(
            @PathVariable String customerId,
            @PathVariable String addressId) {
        logger.info("Getting address {} for customer: {}", addressId, customerId);
        Address address = addressService.getAddressById(addressId);
        
        // Verify ownership
        if (!address.getCustomerId().equals(customerId)) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(address);
    }

    /**
     * Get default address for a customer
     */
    @GetMapping("/default")
    public ResponseEntity<Address> getDefaultAddress(@PathVariable String customerId) {
        logger.info("Getting default address for customer: {}", customerId);
        return addressService.getDefaultAddress(customerId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Create a new address
     */
    @PostMapping
    public ResponseEntity<Address> createAddress(
            @PathVariable String customerId,
            @RequestBody Address address) {
        logger.info("Creating address for customer: {}", customerId);
        Address createdAddress = addressService.createAddress(customerId, address);
        return ResponseEntity.ok(createdAddress);
    }

    /**
     * Update an existing address
     */
    @PutMapping("/{addressId}")
    public ResponseEntity<Address> updateAddress(
            @PathVariable String customerId,
            @PathVariable String addressId,
            @RequestBody Address address) {
        logger.info("Updating address {} for customer: {}", addressId, customerId);
        Address updatedAddress = addressService.updateAddress(customerId, addressId, address);
        return ResponseEntity.ok(updatedAddress);
    }

    /**
     * Delete an address
     */
    @DeleteMapping("/{addressId}")
    public ResponseEntity<Map<String, Boolean>> deleteAddress(
            @PathVariable String customerId,
            @PathVariable String addressId) {
        logger.info("Deleting address {} for customer: {}", addressId, customerId);
        boolean deleted = addressService.deleteAddress(customerId, addressId);
        return ResponseEntity.ok(Map.of("deleted", deleted));
    }

    /**
     * Set an address as default
     */
    @PatchMapping("/{addressId}/default")
    public ResponseEntity<Address> setDefaultAddress(
            @PathVariable String customerId,
            @PathVariable String addressId) {
        logger.info("Setting address {} as default for customer: {}", addressId, customerId);
        Address address = addressService.setDefaultAddress(customerId, addressId);
        return ResponseEntity.ok(address);
    }

    /**
     * Get addresses by type (home, work, other)
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<List<Address>> getAddressesByType(
            @PathVariable String customerId,
            @PathVariable String type) {
        logger.info("Getting {} addresses for customer: {}", type, customerId);
        List<Address> addresses = addressService.getAddressesByType(customerId, type);
        return ResponseEntity.ok(addresses);
    }

    /**
     * Get address count for a customer
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> getAddressCount(@PathVariable String customerId) {
        long count = addressService.countAddresses(customerId);
        return ResponseEntity.ok(Map.of("count", count));
    }
}
