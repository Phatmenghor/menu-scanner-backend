package com.emenu.features.notification.service;

import com.emenu.enums.NotificationType;
import com.emenu.features.user_management.domain.User;

public interface NotificationService {
    
    // Email notifications
    void sendWelcomeEmail(User user);
    void sendEmailVerification(User user, String token);
    void sendPasswordResetEmail(User user, String token);
    void sendPasswordChangeConfirmation(User user);
    void sendAccountLockedNotification(User user);
    void sendAccountUnlockedNotification(User user);
    void sendSubscriptionExpiryNotification(User user, int daysUntilExpiry);
    void sendTierUpgradeNotification(User user, String oldTier, String newTier);
    void sendLoyaltyPointsNotification(User user, int pointsAdded);
    
    // Telegram notifications
    void sendTelegramNotification(User user, String message);
    void sendTelegramWelcome(User user);
    void sendTelegramOrderUpdate(User user, String orderDetails);
    
    // SMS notifications (future implementation)
    void sendSMSVerification(User user, String code);
    void sendSMSNotification(User user, String message);
    
    // Generic notification method
    void sendNotification(User user, String subject, String message, NotificationType type);
}