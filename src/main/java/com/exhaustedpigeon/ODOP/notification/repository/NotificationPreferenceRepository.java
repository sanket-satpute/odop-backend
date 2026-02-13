package com.exhaustedpigeon.ODOP.notification.repository;

import com.exhaustedpigeon.ODOP.notification.model.NotificationPreference;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationPreferenceRepository extends MongoRepository<NotificationPreference, String> {
    
    Optional<NotificationPreference> findByUserId(String userId);
    
    List<NotificationPreference> findByUserType(String userType);
    
    // Find users with push enabled
    List<NotificationPreference> findByPushEnabled(boolean enabled);
    
    // Find users with email enabled
    List<NotificationPreference> findByEmailEnabled(boolean enabled);
    
    // Find users with specific language preference
    List<NotificationPreference> findByPreferredLanguage(String language);
    
    // Find users who want promotions
    List<NotificationPreference> findByPushPromotionsTrue();
    
    // Find users with global notifications enabled
    List<NotificationPreference> findByGlobalEnabledTrue();
    
    boolean existsByUserId(String userId);
}
