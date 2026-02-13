package com.odop.root.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.odop.root.models.Admin;

@Repository
public interface AdminRepository extends MongoRepository<Admin, String> {

    Admin findByAdminId(String adminId);

    Admin findByEmailAddress(String emailAddress);

    Admin findByEmailAddressAndAuthorizationKey(String emailAddress, String authorizationKey);
    
 // ✅ Custom method for existence check
    boolean existsByEmailAddressOrAuthorizationKey(String emailAddress, String authorizationKey);
}
