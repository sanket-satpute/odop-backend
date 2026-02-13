package com.exhaustedpigeon.ODOP.notification.service;

import com.exhaustedpigeon.ODOP.notification.dto.*;
import com.exhaustedpigeon.ODOP.notification.model.Notification;
import com.exhaustedpigeon.ODOP.notification.model.Notification.*;
import com.exhaustedpigeon.ODOP.notification.model.NotificationPreference;
import com.exhaustedpigeon.ODOP.notification.repository.NotificationPreferenceRepository;
import com.exhaustedpigeon.ODOP.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing notifications
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    
    private final NotificationRepository notificationRepository;
    private final NotificationPreferenceRepository preferenceRepository;
    
    // ==================== SEND NOTIFICATIONS ====================
    
    /**
     * Send notification to a single user
     */
    public NotificationDto sendToUser(String userId, SendNotificationRequest request) {
        log.info("Sending notification to user: {}", userId);
        
        // Get user preferences
        NotificationPreference preferences = getOrCreatePreferences(userId, "CUSTOMER", null, null);
        
        // Check if user has global notifications enabled
        if (!preferences.isGlobalEnabled()) {
            log.info("User {} has notifications disabled", userId);
            return null;
        }
        
        // Check quiet hours
        if (isInQuietHours(preferences)) {
            log.info("User {} is in quiet hours, notification will be delayed", userId);
            // Could implement scheduled delivery here
        }
        
        // Create and save notification
        Notification notification = createNotification(userId, preferences.getUserType(), request);
        notification = notificationRepository.save(notification);
        
        // Send through enabled channels
        List<NotificationChannel> channels = determineChannels(request, preferences);
        sendThroughChannels(notification, preferences, channels);
        
        return NotificationDto.fromEntity(notification);
    }
    
    /**
     * Send notification to multiple users
     */
    @Async
    public void sendToUsers(List<String> userIds, SendNotificationRequest request) {
        log.info("Sending notification to {} users", userIds.size());
        
        for (String userId : userIds) {
            try {
                sendToUser(userId, request);
            } catch (Exception e) {
                log.error("Failed to send notification to user: {}", userId, e);
            }
        }
    }
    
    /**
     * Send notification to all users of a specific type
     */
    @Async
    public void sendToUserType(String userType, SendNotificationRequest request) {
        log.info("Sending notification to all users of type: {}", userType);
        
        List<NotificationPreference> preferences = preferenceRepository.findByUserType(userType);
        
        for (NotificationPreference pref : preferences) {
            try {
                if (pref.isGlobalEnabled()) {
                    sendToUser(pref.getUserId(), request);
                }
            } catch (Exception e) {
                log.error("Failed to send notification to user: {}", pref.getUserId(), e);
            }
        }
    }
    
    /**
     * Broadcast notification to all users
     */
    @Async
    public void broadcast(SendNotificationRequest request) {
        log.info("Broadcasting notification to all users");
        
        List<NotificationPreference> allPrefs = preferenceRepository.findByGlobalEnabledTrue();
        
        for (NotificationPreference pref : allPrefs) {
            try {
                sendToUser(pref.getUserId(), request);
            } catch (Exception e) {
                log.error("Failed to broadcast to user: {}", pref.getUserId(), e);
            }
        }
    }
    
    // ==================== ORDER NOTIFICATIONS ====================
    
    /**
     * Send order placed notification
     */
    public void notifyOrderPlaced(String customerId, String orderId, double amount) {
        SendNotificationRequest request = SendNotificationRequest.builder()
                .title("Order Placed Successfully!")
                .body(String.format("Your order #%s of ₹%.2f has been placed successfully.", 
                        orderId.substring(orderId.length() - 8), amount))
                .type(NotificationType.ORDER_PLACED)
                .category("ORDER")
                .actionUrl("/customer/orders/" + orderId)
                .actionType("OPEN_ORDER")
                .referenceId(orderId)
                .referenceType("ORDER")
                .priority(NotificationPriority.HIGH)
                .build();
        
        sendToUser(customerId, request);
    }
    
    /**
     * Send order shipped notification
     */
    public void notifyOrderShipped(String customerId, String orderId, String trackingNumber) {
        SendNotificationRequest request = SendNotificationRequest.builder()
                .title("Your Order Has Been Shipped!")
                .body(String.format("Order #%s is on its way! Track with: %s", 
                        orderId.substring(orderId.length() - 8), trackingNumber))
                .type(NotificationType.ORDER_SHIPPED)
                .category("ORDER")
                .actionUrl("/track-order/" + orderId)
                .actionType("OPEN_TRACKING")
                .referenceId(orderId)
                .referenceType("ORDER")
                .priority(NotificationPriority.HIGH)
                .build();
        
        sendToUser(customerId, request);
    }
    
    /**
     * Send order delivered notification
     */
    public void notifyOrderDelivered(String customerId, String orderId) {
        SendNotificationRequest request = SendNotificationRequest.builder()
                .title("Order Delivered!")
                .body(String.format("Your order #%s has been delivered. Enjoy your purchase!", 
                        orderId.substring(orderId.length() - 8)))
                .type(NotificationType.ORDER_DELIVERED)
                .category("ORDER")
                .actionUrl("/customer/orders/" + orderId)
                .actionType("OPEN_ORDER")
                .referenceId(orderId)
                .referenceType("ORDER")
                .priority(NotificationPriority.NORMAL)
                .build();
        
        sendToUser(customerId, request);
    }
    
    // ==================== VENDOR NOTIFICATIONS ====================
    
    /**
     * Notify vendor of new order
     */
    public void notifyVendorNewOrder(String vendorId, String orderId, String productName, int quantity) {
        SendNotificationRequest request = SendNotificationRequest.builder()
                .title("New Order Received!")
                .body(String.format("You have a new order for %d x %s", quantity, productName))
                .type(NotificationType.NEW_ORDER)
                .category("ORDER")
                .actionUrl("/vendor/orders/" + orderId)
                .actionType("OPEN_ORDER")
                .referenceId(orderId)
                .referenceType("ORDER")
                .priority(NotificationPriority.HIGH)
                .build();
        
        sendToUser(vendorId, request);
    }
    
    /**
     * Notify vendor of low stock
     */
    public void notifyLowStock(String vendorId, String productId, String productName, int currentStock) {
        SendNotificationRequest request = SendNotificationRequest.builder()
                .title("Low Stock Alert!")
                .body(String.format("%s has only %d items left in stock. Consider restocking soon.", 
                        productName, currentStock))
                .type(NotificationType.LOW_STOCK_ALERT)
                .category("INVENTORY")
                .actionUrl("/vendor/products/" + productId)
                .actionType("OPEN_PRODUCT")
                .referenceId(productId)
                .referenceType("PRODUCT")
                .priority(NotificationPriority.HIGH)
                .build();
        
        sendToUser(vendorId, request);
    }
    
    /**
     * Notify vendor of new review
     */
    public void notifyNewReview(String vendorId, String productId, String productName, int rating) {
        String stars = "★".repeat(rating) + "☆".repeat(5 - rating);
        
        SendNotificationRequest request = SendNotificationRequest.builder()
                .title("New Review Received!")
                .body(String.format("%s received a %s review on %s", productName, stars, ""))
                .type(NotificationType.REVIEW_RECEIVED)
                .category("REVIEW")
                .actionUrl("/vendor/products/" + productId + "/reviews")
                .actionType("OPEN_REVIEWS")
                .referenceId(productId)
                .referenceType("PRODUCT")
                .priority(NotificationPriority.NORMAL)
                .build();
        
        sendToUser(vendorId, request);
    }
    
    /**
     * Notify vendor of verification status update
     */
    public void notifyVerificationUpdate(String vendorId, String status, String message) {
        NotificationType type = status.equals("APPROVED") ? 
                NotificationType.VERIFICATION_UPDATE : NotificationType.VERIFICATION_UPDATE;
        
        SendNotificationRequest request = SendNotificationRequest.builder()
                .title("Verification Status Updated")
                .body(message)
                .type(type)
                .category("VERIFICATION")
                .actionUrl("/vendor/verification")
                .actionType("OPEN_VERIFICATION")
                .referenceId(vendorId)
                .referenceType("VENDOR")
                .priority(NotificationPriority.HIGH)
                .build();
        
        sendToUser(vendorId, request);
    }
    
    // ==================== PAYMENT NOTIFICATIONS ====================
    
    /**
     * Notify payment success
     */
    public void notifyPaymentSuccess(String customerId, String orderId, double amount) {
        SendNotificationRequest request = SendNotificationRequest.builder()
                .title("Payment Successful!")
                .body(String.format("₹%.2f paid successfully for order #%s", 
                        amount, orderId.substring(orderId.length() - 8)))
                .type(NotificationType.PAYMENT_SUCCESS)
                .category("PAYMENT")
                .actionUrl("/customer/orders/" + orderId)
                .actionType("OPEN_ORDER")
                .referenceId(orderId)
                .referenceType("ORDER")
                .priority(NotificationPriority.HIGH)
                .build();
        
        sendToUser(customerId, request);
    }
    
    /**
     * Notify payment failed
     */
    public void notifyPaymentFailed(String customerId, String orderId, String reason) {
        SendNotificationRequest request = SendNotificationRequest.builder()
                .title("Payment Failed")
                .body(String.format("Payment for order #%s failed: %s. Please try again.", 
                        orderId.substring(orderId.length() - 8), reason))
                .type(NotificationType.PAYMENT_FAILED)
                .category("PAYMENT")
                .actionUrl("/checkout/" + orderId)
                .actionType("RETRY_PAYMENT")
                .referenceId(orderId)
                .referenceType("ORDER")
                .priority(NotificationPriority.URGENT)
                .build();
        
        sendToUser(customerId, request);
    }
    
    /**
     * Notify refund processed
     */
    public void notifyRefundProcessed(String customerId, String orderId, double amount) {
        SendNotificationRequest request = SendNotificationRequest.builder()
                .title("Refund Processed")
                .body(String.format("₹%.2f has been refunded for order #%s. It will reflect in 5-7 business days.", 
                        amount, orderId.substring(orderId.length() - 8)))
                .type(NotificationType.REFUND_COMPLETED)
                .category("PAYMENT")
                .actionUrl("/customer/orders/" + orderId)
                .actionType("OPEN_ORDER")
                .referenceId(orderId)
                .referenceType("ORDER")
                .priority(NotificationPriority.NORMAL)
                .build();
        
        sendToUser(customerId, request);
    }
    
    // ==================== PROMOTIONAL NOTIFICATIONS ====================
    
    /**
     * Send promotional notification
     */
    public void sendPromotion(String userId, String title, String body, String actionUrl) {
        SendNotificationRequest request = SendNotificationRequest.builder()
                .title(title)
                .body(body)
                .type(NotificationType.PROMOTION)
                .category("PROMOTION")
                .actionUrl(actionUrl)
                .actionType("OPEN_URL")
                .priority(NotificationPriority.LOW)
                .build();
        
        sendToUser(userId, request);
    }
    
    /**
     * Notify price drop
     */
    public void notifyPriceDrop(String userId, String productId, String productName, 
                                 double oldPrice, double newPrice) {
        int discountPercent = (int) ((oldPrice - newPrice) / oldPrice * 100);
        
        SendNotificationRequest request = SendNotificationRequest.builder()
                .title("Price Drop Alert! 🎉")
                .body(String.format("%s is now ₹%.2f (was ₹%.2f) - %d%% off!", 
                        productName, newPrice, oldPrice, discountPercent))
                .type(NotificationType.PRICE_DROP)
                .category("PROMOTION")
                .actionUrl("/product/" + productId)
                .actionType("OPEN_PRODUCT")
                .referenceId(productId)
                .referenceType("PRODUCT")
                .priority(NotificationPriority.NORMAL)
                .build();
        
        sendToUser(userId, request);
    }
    
    /**
     * Notify back in stock
     */
    public void notifyBackInStock(String userId, String productId, String productName) {
        SendNotificationRequest request = SendNotificationRequest.builder()
                .title("Back In Stock!")
                .body(String.format("%s is back in stock. Get it before it's gone!", productName))
                .type(NotificationType.BACK_IN_STOCK)
                .category("PROMOTION")
                .actionUrl("/product/" + productId)
                .actionType("OPEN_PRODUCT")
                .referenceId(productId)
                .referenceType("PRODUCT")
                .priority(NotificationPriority.HIGH)
                .build();
        
        sendToUser(userId, request);
    }
    
    // ==================== GET NOTIFICATIONS ====================
    
    /**
     * Get notifications for a user
     */
    public Page<NotificationDto> getUserNotifications(String userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return notifications.map(NotificationDto::fromEntity);
    }
    
    /**
     * Get unread notifications count
     */
    public long getUnreadCount(String userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }
    
    /**
     * Get unread notifications
     */
    public List<NotificationDto> getUnreadNotifications(String userId) {
        return notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(userId)
                .stream()
                .map(NotificationDto::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * Get notifications by type
     */
    public List<NotificationDto> getNotificationsByType(String userId, NotificationType type) {
        return notificationRepository.findByUserIdAndTypeOrderByCreatedAtDesc(userId, type)
                .stream()
                .map(NotificationDto::fromEntity)
                .collect(Collectors.toList());
    }
    
    // ==================== MARK AS READ ====================
    
    /**
     * Mark notification as read
     */
    public void markAsRead(String notificationId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            notification.setRead(true);
            notification.setReadAt(LocalDateTime.now());
            notificationRepository.save(notification);
        });
    }
    
    /**
     * Mark all notifications as read for a user
     */
    public void markAllAsRead(String userId) {
        List<Notification> unread = notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(userId);
        LocalDateTime now = LocalDateTime.now();
        
        unread.forEach(notification -> {
            notification.setRead(true);
            notification.setReadAt(now);
        });
        
        notificationRepository.saveAll(unread);
    }
    
    /**
     * Delete notification
     */
    public void deleteNotification(String notificationId, String userId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            if (notification.getUserId().equals(userId)) {
                notificationRepository.delete(notification);
            }
        });
    }
    
    /**
     * Clear all notifications for a user
     */
    public void clearAllNotifications(String userId) {
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        notificationRepository.deleteAll(notifications);
    }
    
    // ==================== PREFERENCES ====================
    
    /**
     * Get user preferences
     */
    public NotificationPreferenceDto getPreferences(String userId) {
        NotificationPreference preferences = preferenceRepository.findByUserId(userId)
                .orElse(null);
        
        if (preferences == null) {
            return null;
        }
        
        return NotificationPreferenceDto.fromEntity(preferences);
    }
    
    /**
     * Update user preferences
     */
    public NotificationPreferenceDto updatePreferences(String userId, NotificationPreferenceDto dto) {
        NotificationPreference preferences = preferenceRepository.findByUserId(userId)
                .orElseGet(() -> NotificationPreference.createDefault(userId, dto.getUserType(), 
                        dto.getEmailAddress(), dto.getPhoneNumber()));
        
        // Update global settings
        preferences.setGlobalEnabled(dto.isGlobalEnabled());
        preferences.setQuietHoursEnabled(dto.isQuietHoursEnabled());
        preferences.setQuietHoursStart(dto.getQuietHoursStart());
        preferences.setQuietHoursEnd(dto.getQuietHoursEnd());
        preferences.setTimezone(dto.getTimezone());
        preferences.setPreferredLanguage(dto.getPreferredLanguage());
        preferences.setMaxDailyNotifications(dto.getMaxDailyNotifications());
        preferences.setMaxWeeklyPromotions(dto.getMaxWeeklyPromotions());
        
        // Update channel preferences
        if (dto.getPush() != null) {
            preferences.setPush(convertToChannelPreferences(dto.getPush()));
        }
        if (dto.getEmail() != null) {
            preferences.setEmail(convertToChannelPreferences(dto.getEmail()));
        }
        if (dto.getSms() != null) {
            preferences.setSms(convertToChannelPreferences(dto.getSms()));
        }
        if (dto.getWhatsapp() != null) {
            preferences.setWhatsapp(convertToChannelPreferences(dto.getWhatsapp()));
        }
        
        preferences.setUpdatedAt(LocalDateTime.now());
        preferences = preferenceRepository.save(preferences);
        
        return NotificationPreferenceDto.fromEntity(preferences);
    }
    
    /**
     * Register device token
     */
    public void registerDevice(String userId, RegisterDeviceRequest request) {
        NotificationPreference preferences = preferenceRepository.findByUserId(userId)
                .orElseGet(() -> NotificationPreference.createDefault(userId, "CUSTOMER", null, null));
        
        List<NotificationPreference.DeviceToken> tokens = preferences.getDeviceTokens();
        if (tokens == null) {
            tokens = new ArrayList<>();
        }
        
        // Check if device already registered
        boolean exists = tokens.stream()
                .anyMatch(t -> t.getToken().equals(request.getToken()));
        
        if (!exists) {
            NotificationPreference.DeviceToken newToken = NotificationPreference.DeviceToken.builder()
                    .token(request.getToken())
                    .platform(request.getPlatform())
                    .deviceId(request.getDeviceId())
                    .deviceName(request.getDeviceName())
                    .registeredAt(LocalDateTime.now())
                    .lastUsedAt(LocalDateTime.now())
                    .active(true)
                    .build();
            
            tokens.add(newToken);
            preferences.setDeviceTokens(tokens);
            preferences.setUpdatedAt(LocalDateTime.now());
            preferenceRepository.save(preferences);
            
            log.info("Registered device token for user: {}", userId);
        }
    }
    
    /**
     * Unregister device token
     */
    public void unregisterDevice(String userId, String deviceToken) {
        preferenceRepository.findByUserId(userId).ifPresent(preferences -> {
            if (preferences.getDeviceTokens() != null) {
                preferences.getDeviceTokens().removeIf(t -> t.getToken().equals(deviceToken));
                preferences.setUpdatedAt(LocalDateTime.now());
                preferenceRepository.save(preferences);
                log.info("Unregistered device token for user: {}", userId);
            }
        });
    }
    
    // ==================== HELPER METHODS ====================
    
    private NotificationPreference getOrCreatePreferences(String userId, String userType, 
                                                          String email, String phone) {
        return preferenceRepository.findByUserId(userId)
                .orElseGet(() -> {
                    NotificationPreference pref = NotificationPreference.createDefault(userId, userType, email, phone);
                    return preferenceRepository.save(pref);
                });
    }
    
    private Notification createNotification(String userId, String userType, SendNotificationRequest request) {
        return Notification.builder()
                .userId(userId)
                .userType(userType)
                .title(request.getTitle())
                .body(request.getBody())
                .imageUrl(request.getImageUrl())
                .iconUrl(request.getIconUrl())
                .type(request.getType())
                .category(request.getCategory())
                .actionUrl(request.getActionUrl())
                .actionType(request.getActionType())
                .actionData(request.getActionData())
                .referenceId(request.getReferenceId())
                .referenceType(request.getReferenceType())
                .priority(request.getPriority() != null ? request.getPriority() : NotificationPriority.NORMAL)
                .read(false)
                .delivered(false)
                .createdAt(LocalDateTime.now())
                .build();
    }
    
    private List<NotificationChannel> determineChannels(SendNotificationRequest request, 
                                                        NotificationPreference preferences) {
        List<NotificationChannel> channels = new ArrayList<>();
        
        // If channels specified in request, use those
        if (request.getChannels() != null && !request.getChannels().isEmpty()) {
            return request.getChannels();
        }
        
        // Otherwise determine based on preferences and notification type
        channels.add(NotificationChannel.IN_APP);  // Always store in-app
        
        if (preferences.getPush() != null && preferences.getPush().isEnabled()) {
            if (shouldSendPush(request.getType(), preferences.getPush())) {
                channels.add(NotificationChannel.PUSH);
            }
        }
        
        if (preferences.getEmail() != null && preferences.getEmail().isEnabled()) {
            if (shouldSendEmail(request.getType(), preferences.getEmail())) {
                channels.add(NotificationChannel.EMAIL);
            }
        }
        
        if (preferences.getSms() != null && preferences.getSms().isEnabled()) {
            if (shouldSendSms(request.getType(), preferences.getSms())) {
                channels.add(NotificationChannel.SMS);
            }
        }
        
        return channels;
    }
    
    private boolean shouldSendPush(NotificationType type, NotificationPreference.ChannelPreferences prefs) {
        return switch (type) {
            case ORDER_PLACED, ORDER_CONFIRMED, ORDER_SHIPPED, ORDER_DELIVERED, ORDER_CANCELLED, ORDER_RETURNED ->
                    prefs.isOrderUpdates();
            case PAYMENT_SUCCESS, PAYMENT_FAILED, REFUND_INITIATED, REFUND_COMPLETED ->
                    prefs.isPaymentUpdates();
            case SHIPMENT_DISPATCHED, SHIPMENT_IN_TRANSIT, SHIPMENT_OUT_FOR_DELIVERY, SHIPMENT_DELIVERED, SHIPMENT_DELAYED ->
                    prefs.isShipmentUpdates();
            case PROMOTION, FLASH_SALE ->
                    prefs.isPromotions();
            case PRICE_DROP ->
                    prefs.isPriceDropAlerts();
            case BACK_IN_STOCK ->
                    prefs.isBackInStockAlerts();
            case SECURITY_ALERT ->
                    prefs.isSecurityAlerts();
            case ACCOUNT_UPDATE, SYSTEM_UPDATE ->
                    prefs.isAccountUpdates();
            case NEW_ORDER ->
                    prefs.isNewOrders();
            case LOW_STOCK_ALERT ->
                    prefs.isLowStockAlerts();
            case REVIEW_RECEIVED ->
                    prefs.isReviewNotifications();
            case VERIFICATION_UPDATE ->
                    prefs.isVerificationUpdates();
            default -> true;
        };
    }
    
    private boolean shouldSendEmail(NotificationType type, NotificationPreference.ChannelPreferences prefs) {
        return shouldSendPush(type, prefs);  // Same logic for now
    }
    
    private boolean shouldSendSms(NotificationType type, NotificationPreference.ChannelPreferences prefs) {
        // SMS only for important notifications
        return switch (type) {
            case ORDER_PLACED, ORDER_SHIPPED, ORDER_DELIVERED, PAYMENT_SUCCESS, PAYMENT_FAILED,
                 SECURITY_ALERT, NEW_ORDER, VERIFICATION_UPDATE -> shouldSendPush(type, prefs);
            default -> false;
        };
    }
    
    private void sendThroughChannels(Notification notification, NotificationPreference preferences,
                                     List<NotificationChannel> channels) {
        List<NotificationChannel> sentChannels = new ArrayList<>();
        
        for (NotificationChannel channel : channels) {
            try {
                switch (channel) {
                    case PUSH -> {
                        sendPushNotification(notification, preferences);
                        sentChannels.add(channel);
                    }
                    case EMAIL -> {
                        sendEmailNotification(notification, preferences);
                        sentChannels.add(channel);
                    }
                    case SMS -> {
                        sendSmsNotification(notification, preferences);
                        sentChannels.add(channel);
                    }
                    case WHATSAPP -> {
                        sendWhatsAppNotification(notification, preferences);
                        sentChannels.add(channel);
                    }
                    case IN_APP -> sentChannels.add(channel);  // Already stored
                }
            } catch (Exception e) {
                log.error("Failed to send notification through {}: {}", channel, e.getMessage());
            }
        }
        
        notification.setSentChannels(sentChannels);
        notification.setDelivered(!sentChannels.isEmpty());
        notification.setDeliveredAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }
    
    private void sendPushNotification(Notification notification, NotificationPreference preferences) {
        // TODO: Implement Firebase Cloud Messaging
        // For now, log the notification
        log.info("PUSH: [{}] {} - {}", notification.getUserId(), notification.getTitle(), notification.getBody());
        
        // Implementation would look like:
        // if (preferences.getDeviceTokens() != null) {
        //     for (DeviceToken token : preferences.getDeviceTokens()) {
        //         if (token.isActive()) {
        //             firebaseService.sendPush(token.getToken(), notification);
        //         }
        //     }
        // }
    }
    
    private void sendEmailNotification(Notification notification, NotificationPreference preferences) {
        // TODO: Implement email sending (using existing email service if available)
        log.info("EMAIL: [{}] {} - {}", preferences.getEmailAddress(), notification.getTitle(), notification.getBody());
    }
    
    private void sendSmsNotification(Notification notification, NotificationPreference preferences) {
        // TODO: Implement SMS sending using Twilio
        log.info("SMS: [{}] {} - {}", preferences.getPhoneNumber(), notification.getTitle(), notification.getBody());
    }
    
    private void sendWhatsAppNotification(Notification notification, NotificationPreference preferences) {
        // TODO: Implement WhatsApp sending using Twilio
        log.info("WHATSAPP: [{}] {} - {}", preferences.getPhoneNumber(), notification.getTitle(), notification.getBody());
    }
    
    private boolean isInQuietHours(NotificationPreference preferences) {
        if (!preferences.isQuietHoursEnabled()) {
            return false;
        }
        
        try {
            ZoneId zone = ZoneId.of(preferences.getTimezone() != null ? 
                    preferences.getTimezone() : "Asia/Kolkata");
            LocalTime now = LocalTime.now(zone);
            LocalTime start = LocalTime.parse(preferences.getQuietHoursStart());
            LocalTime end = LocalTime.parse(preferences.getQuietHoursEnd());
            
            if (start.isBefore(end)) {
                return now.isAfter(start) && now.isBefore(end);
            } else {
                // Quiet hours span midnight
                return now.isAfter(start) || now.isBefore(end);
            }
        } catch (Exception e) {
            log.warn("Error checking quiet hours: {}", e.getMessage());
            return false;
        }
    }
    
    private NotificationPreference.ChannelPreferences convertToChannelPreferences(
            NotificationPreferenceDto.ChannelPreferencesDto dto) {
        return NotificationPreference.ChannelPreferences.builder()
                .enabled(dto.isEnabled())
                .orderUpdates(dto.isOrderUpdates())
                .paymentUpdates(dto.isPaymentUpdates())
                .shipmentUpdates(dto.isShipmentUpdates())
                .promotions(dto.isPromotions())
                .priceDropAlerts(dto.isPriceDropAlerts())
                .backInStockAlerts(dto.isBackInStockAlerts())
                .securityAlerts(dto.isSecurityAlerts())
                .accountUpdates(dto.isAccountUpdates())
                .reviewResponses(dto.isReviewResponses())
                .newOrders(dto.isNewOrders())
                .lowStockAlerts(dto.isLowStockAlerts())
                .reviewNotifications(dto.isReviewNotifications())
                .verificationUpdates(dto.isVerificationUpdates())
                .promotionFrequency(dto.getPromotionFrequency())
                .build();
    }
    
    // ==================== CLEANUP ====================
    
    /**
     * Clean up old notifications (called by scheduler)
     */
    public void cleanupOldNotifications(int daysToKeep) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(daysToKeep);
        notificationRepository.deleteByCreatedAtBefore(cutoff);
        log.info("Cleaned up notifications older than {} days", daysToKeep);
    }
}
