package com.odop.root.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.odop.root.models.Customer;

@Repository
public interface CustomerRepository extends MongoRepository<Customer, String> {

    Customer findByCustomerId(String customerId);

    Customer findByEmailAddress(String emailAddress);
    
    boolean existsByEmailAddressOrContactNumber(String emailAddress, long contactNumber);
}
