package com.emenu.features.notification.service;

import com.emenu.features.auth.models.User;

import java.util.Map;

public interface EmailService {
    void sendWelcomeEmail(User user);
    void sendEmailVerification(User user, String verificationToken);
    void sendPasswordResetEmail(User user, String resetToken);
    void sendTierUpgradeEmail(User user, String oldTier, String newTier);
    void sendSubscriptionWelcomeEmail(User user, String planName);
    void sendSubscriptionExpirationEmail(User user, String planName, String expirationDate);
    void sendCustomEmail(String to, String subject, String templateName, Map<String, Object> variables);
    void sendPlainEmail(String to, String subject, String content);
}
