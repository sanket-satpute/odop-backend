package com.odop.root.repository;

import com.odop.root.models.Address;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Address entity operations.
 */
@Repository
public interface AddressRepository extends MongoRepository<Address, String> {
    
    /**
     * Find all addresses for a customer
     */
    List<Address> findByCustomerIdAndIsActiveTrue(String customerId);
    
    /**
     * Find all addresses for a customer (including inactive)
     */
    List<Address> findByCustomerId(String customerId);
    
    /**
     * Find default address for a customer
     */
    Optional<Address> findByCustomerIdAndIsDefaultTrueAndIsActiveTrue(String customerId);
    
    /**
     * Find addresses by type
     */
    List<Address> findByCustomerIdAndTypeAndIsActiveTrue(String customerId, String type);
    
    /**
     * Count addresses for a customer
     */
    long countByCustomerIdAndIsActiveTrue(String customerId);
    
    /**
     * Delete all addresses for a customer
     */
    void deleteByCustomerId(String customerId);
    
    /**
     * Check if address exists for customer
     */
    boolean existsByAddressIdAndCustomerId(String addressId, String customerId);
}
