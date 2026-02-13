package com.odop.root.oauth2.repository;

import com.odop.root.oauth2.model.SocialAccount;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SocialAccountRepository extends MongoRepository<SocialAccount, String> {
    
    // Find by provider and social ID
    Optional<SocialAccount> findByProviderAndSocialId(String provider, String socialId);
    
    // Find by email and provider
    Optional<SocialAccount> findByProviderAndEmail(String provider, String email);
    
    // Find all social accounts for a user
    List<SocialAccount> findByUserId(String userId);
    
    // Find by user ID and provider
    Optional<SocialAccount> findByUserIdAndProvider(String userId, String provider);
    
    // Check if social account exists
    boolean existsByProviderAndSocialId(String provider, String socialId);
    
    // Check if email is already linked to provider
    boolean existsByProviderAndEmail(String provider, String email);
    
    // Delete social account link
    void deleteByUserIdAndProvider(String userId, String provider);
    
    // Count social accounts for a user
    long countByUserId(String userId);
}
