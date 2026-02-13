package com.odop.repository;

import com.odop.model.CustomerPreferences;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerPreferencesRepository extends MongoRepository<CustomerPreferences, String> {

    /**
     * Find preferences by customer ID
     */
    Optional<CustomerPreferences> findByCustomerId(String customerId);

    /**
     * Check if preferences exist for a customer
     */
    boolean existsByCustomerId(String customerId);

    /**
     * Delete preferences by customer ID
     */
    void deleteByCustomerId(String customerId);

    /**
     * Find customers with two-factor authentication enabled
     */
    @Query("{ 'securitySettings.twoFactorEnabled': true }")
    List<CustomerPreferences> findCustomersWithTwoFactorEnabled();

    /**
     * Find customers who have enabled email notifications
     */
    @Query("{ 'notificationChannels.email': true }")
    List<CustomerPreferences> findCustomersWithEmailNotifications();

    /**
     * Find customers who have enabled SMS notifications
     */
    @Query("{ 'notificationChannels.sms': true }")
    List<CustomerPreferences> findCustomersWithSmsNotifications();

    /**
     * Find customers who have opted in to promotional emails
     */
    @Query("{ 'notificationPreferences.promotions.email': true }")
    List<CustomerPreferences> findCustomersOptedInToPromotions();

    /**
     * Find customers who have opted out of data analytics
     */
    @Query("{ 'privacySettings.allowDataAnalytics': false }")
    List<CustomerPreferences> findCustomersOptedOutOfAnalytics();

    /**
     * Find customers by theme mode
     */
    @Query("{ 'appearanceSettings.themeMode': ?0 }")
    List<CustomerPreferences> findByThemeMode(String themeMode);

    /**
     * Find customers with connected service
     */
    @Query("{ 'connectedServices': { $elemMatch: { 'id': ?0, 'connected': true } } }")
    List<CustomerPreferences> findCustomersWithConnectedService(String serviceId);

    /**
     * Find customers by language preference
     */
    List<CustomerPreferences> findByLanguage(String language);

    /**
     * Count customers with newsletter subscription
     */
    @Query(value = "{ 'notificationPreferences.newsletter.email': true }", count = true)
    long countNewsletterSubscribers();
}
