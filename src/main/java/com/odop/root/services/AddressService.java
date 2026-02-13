package com.odop.root.services;

import com.odop.root.models.Address;
import com.odop.root.repository.AddressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing customer addresses.
 */
@Service
public class AddressService {

    @Autowired
    private AddressRepository addressRepository;

    /**
     * Get all active addresses for a customer
     */
    public List<Address> getCustomerAddresses(String customerId) {
        return addressRepository.findByCustomerIdAndIsActiveTrue(customerId);
    }

    /**
     * Get address by ID
     */
    public Address getAddressById(String addressId) {
        return addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Address not found: " + addressId));
    }

    /**
     * Get default address for a customer
     */
    public Optional<Address> getDefaultAddress(String customerId) {
        return addressRepository.findByCustomerIdAndIsDefaultTrueAndIsActiveTrue(customerId);
    }

    /**
     * Create a new address for a customer
     */
    @Transactional
    public Address createAddress(String customerId, Address address) {
        address.setCustomerId(customerId);
        address.preSave();
        
        // If this is the first address or marked as default, handle default flag
        if (address.isDefault()) {
            clearDefaultAddress(customerId);
        } else {
            // If no addresses exist, make this the default
            long count = addressRepository.countByCustomerIdAndIsActiveTrue(customerId);
            if (count == 0) {
                address.setDefault(true);
            }
        }
        
        return addressRepository.save(address);
    }

    /**
     * Update an existing address
     */
    @Transactional
    public Address updateAddress(String customerId, String addressId, Address updatedAddress) {
        Address existingAddress = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Address not found: " + addressId));
        
        // Verify ownership
        if (!existingAddress.getCustomerId().equals(customerId)) {
            throw new RuntimeException("Address does not belong to customer");
        }
        
        // Update fields
        existingAddress.setType(updatedAddress.getType());
        existingAddress.setName(updatedAddress.getName());
        existingAddress.setPhone(updatedAddress.getPhone());
        existingAddress.setAddressLine1(updatedAddress.getAddressLine1());
        existingAddress.setAddressLine2(updatedAddress.getAddressLine2());
        existingAddress.setLandmark(updatedAddress.getLandmark());
        existingAddress.setCity(updatedAddress.getCity());
        existingAddress.setDistrict(updatedAddress.getDistrict());
        existingAddress.setState(updatedAddress.getState());
        existingAddress.setPincode(updatedAddress.getPincode());
        existingAddress.setCountry(updatedAddress.getCountry());
        existingAddress.setUpdatedAt(LocalDateTime.now());
        
        // Handle default flag change
        if (updatedAddress.isDefault() && !existingAddress.isDefault()) {
            clearDefaultAddress(customerId);
            existingAddress.setDefault(true);
        }
        
        return addressRepository.save(existingAddress);
    }

    /**
     * Delete an address (soft delete)
     */
    @Transactional
    public boolean deleteAddress(String customerId, String addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Address not found: " + addressId));
        
        // Verify ownership
        if (!address.getCustomerId().equals(customerId)) {
            throw new RuntimeException("Address does not belong to customer");
        }
        
        // Soft delete
        address.setActive(false);
        address.setUpdatedAt(LocalDateTime.now());
        addressRepository.save(address);
        
        // If deleted address was default, set another as default
        if (address.isDefault()) {
            List<Address> remainingAddresses = addressRepository.findByCustomerIdAndIsActiveTrue(customerId);
            if (!remainingAddresses.isEmpty()) {
                Address newDefault = remainingAddresses.get(0);
                newDefault.setDefault(true);
                newDefault.setUpdatedAt(LocalDateTime.now());
                addressRepository.save(newDefault);
            }
        }
        
        return true;
    }

    /**
     * Set an address as default
     */
    @Transactional
    public Address setDefaultAddress(String customerId, String addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Address not found: " + addressId));
        
        // Verify ownership
        if (!address.getCustomerId().equals(customerId)) {
            throw new RuntimeException("Address does not belong to customer");
        }
        
        // Clear existing default
        clearDefaultAddress(customerId);
        
        // Set new default
        address.setDefault(true);
        address.setUpdatedAt(LocalDateTime.now());
        return addressRepository.save(address);
    }

    /**
     * Clear default flag from all addresses of a customer
     */
    private void clearDefaultAddress(String customerId) {
        Optional<Address> currentDefault = addressRepository.findByCustomerIdAndIsDefaultTrueAndIsActiveTrue(customerId);
        currentDefault.ifPresent(addr -> {
            addr.setDefault(false);
            addr.setUpdatedAt(LocalDateTime.now());
            addressRepository.save(addr);
        });
    }

    /**
     * Get addresses by type
     */
    public List<Address> getAddressesByType(String customerId, String type) {
        return addressRepository.findByCustomerIdAndTypeAndIsActiveTrue(customerId, type);
    }

    /**
     * Count customer addresses
     */
    public long countAddresses(String customerId) {
        return addressRepository.countByCustomerIdAndIsActiveTrue(customerId);
    }
}
