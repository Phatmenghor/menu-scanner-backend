package com.emenu.features.notification.service.impl;

import com.emenu.enums.NotificationType;
import com.emenu.features.notification.service.EmailService;
import com.emenu.features.notification.service.NotificationService;
import com.emenu.features.notification.service.TelegramService;
import com.emenu.features.user_management.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final EmailService emailService;
    private final TelegramService telegramService;

    @Override
    public void sendWelcomeEmail(User user) {
        log.info("Sending welcome email to: {}", user.getEmail());
        
        Map<String, Object> context = new HashMap<>();
        context.put("firstName", user.getFirstName());
        context.put("lastName", user.getLastName());
        context.put("userType", user.getUserType().getDescription());
        context.put("companyName", "E-Menu Platform");
        
        emailService.sendEmailToUser(
            user, 
            "Welcome to E-Menu Platform!", 
            "welcome", 
            context
        );
    }

    @Override
    public void sendEmailVerification(User user, String token) {
        log.info("Sending email verification to: {}", user.getEmail());
        
        Map<String, Object> context = new HashMap<>();
        context.put("firstName", user.getFirstName());
        context.put("verificationToken", token);
        context.put("verificationUrl", buildVerificationUrl(token));
        context.put("expiryHours", 24);
        
        emailService.sendEmailToUser(
            user, 
            "Verify Your Email Address", 
            "email-verification", 
            context
        );
    }

    @Override
    public void sendPasswordResetEmail(User user, String token) {
        log.info("Sending password reset email to: {}", user.getEmail());
        
        Map<String, Object> context = new HashMap<>();
        context.put("firstName", user.getFirstName());
        context.put("resetToken", token);
        context.put("resetUrl", buildPasswordResetUrl(token));
        context.put("expiryHours", 1);
        
        emailService.sendEmailToUser(
            user, 
            "Reset Your Password", 
            "password-reset", 
            context
        );
    }

    @Override
    public void sendPasswordChangeConfirmation(User user) {
        log.info("Sending password change confirmation to: {}", user.getEmail());
        
        Map<String, Object> context = new HashMap<>();
        context.put("firstName", user.getFirstName());
        context.put("changeTime", user.getLastPasswordChange());
        context.put("ipAddress", user.getLastLoginIp());
        
        emailService.sendEmailToUser(
            user, 
            "Password Changed Successfully", 
            "password-change-confirmation", 
            context
        );
    }

    @Override
    public void sendAccountLockedNotification(User user) {
        log.info("Sending account locked notification to: {}", user.getEmail());
        
        Map<String, Object> context = new HashMap<>();
        context.put("firstName", user.getFirstName());
        context.put("lockTime", user.getAccountLockedUntil());
        context.put("supportEmail", "support@emenu-platform.com");
        
        emailService.sendEmailToUser(
            user, 
            "Account Temporarily Locked", 
            "account-locked", 
            context
        );
    }

    @Override
    public void sendAccountUnlockedNotification(User user) {
        log.info("Sending account unlocked notification to: {}", user.getEmail());
        
        Map<String, Object> context = new HashMap<>();
        context.put("firstName", user.getFirstName());
        context.put("unlockTime", java.time.LocalDateTime.now());
        
        emailService.sendEmailToUser(
            user, 
            "Account Unlocked", 
            "account-unlocked", 
            context
        );
    }

    @Override
    public void sendSubscriptionExpiryNotification(User user, int daysUntilExpiry) {
        log.info("Sending subscription expiry notification to: {} (expires in {} days)", 
                user.getEmail(), daysUntilExpiry);
        
        Map<String, Object> context = new HashMap<>();
        context.put("firstName", user.getFirstName());
        context.put("daysUntilExpiry", daysUntilExpiry);
        context.put("subscriptionPlan", user.getSubscriptionPlan().getDisplayName());
        context.put("expiryDate", user.getSubscriptionEnds());
        context.put("renewalUrl", buildRenewalUrl(user));
        
        String subject = daysUntilExpiry == 1 ? 
            "Your Subscription Expires Tomorrow!" : 
            String.format("Your Subscription Expires in %d Days", daysUntilExpiry);
        
        emailService.sendEmailToUser(
            user, 
            subject, 
            "subscription-expiry", 
            context
        );
    }

    @Override
    public void sendTierUpgradeNotification(User user, String oldTier, String newTier) {
        log.info("Sending tier upgrade notification to: {} ({} -> {})", 
                user.getEmail(), oldTier, newTier);
        
        Map<String, Object> context = new HashMap<>();
        context.put("firstName", user.getFirstName());
        context.put("oldTier", oldTier);
        context.put("newTier", newTier);
        context.put("loyaltyPoints", user.getLoyaltyPoints());
        context.put("benefits", getTierBenefits(newTier));
        
        emailService.sendEmailToUser(
            user, 
            "Congratulations! You've Been Upgraded!", 
            "tier-upgrade", 
            context
        );
        
        // Also send Telegram notification if enabled
        if (user.canReceiveTelegramNotifications()) {
            String telegramMessage = String.format(
                "🎉 Congratulations %s!\n\n" +
                "You've been upgraded from %s to %s tier!\n" +
                "Your current loyalty points: %d\n\n" +
                "Enjoy your new benefits!",
                user.getFirstName(), oldTier, newTier, user.getLoyaltyPoints()
            );
            telegramService.sendMessage(user, telegramMessage);
        }
    }

    @Override
    public void sendLoyaltyPointsNotification(User user, int pointsAdded) {
        log.info("Sending loyalty points notification to: {} (+{} points)", 
                user.getEmail(), pointsAdded);
        
        if (user.canReceiveTelegramNotifications()) {
            String message = String.format(
                "🎁 <b>Loyalty Points Added!</b>\n\n" +
                "Hello %s,\n\n" +
                "You've earned <b>%d</b> loyalty points!\n" +
                "Total points: <b>%d</b>\n" +
                "Current tier: <b>%s</b>\n\n" +
                "Keep earning to unlock more benefits!",
                user.getFirstName(),
                pointsAdded,
                user.getLoyaltyPoints(),
                user.getCustomerTier().getDisplayName()
            );
            telegramService.sendMessage(user, message);
        }
    }

    @Override
    public void sendTelegramNotification(User user, String message) {
        telegramService.sendMessage(user, message);
    }

    @Override
    public void sendTelegramWelcome(User user) {
        telegramService.sendWelcomeMessage(user);
    }

    @Override
    public void sendTelegramOrderUpdate(User user, String orderDetails) {
        telegramService.sendOrderNotification(user, orderDetails);
    }

    @Override
    public void sendSMSVerification(User user, String code) {
        // TODO: Implement SMS service integration
        log.info("SMS verification not implemented yet. Code for {}: {}", user.getPhoneNumber(), code);
    }

    @Override
    public void sendSMSNotification(User user, String message) {
        // TODO: Implement SMS service integration
        log.info("SMS notification not implemented yet. Message for {}: {}", user.getPhoneNumber(), message);
    }

    @Override
    public void sendNotification(User user, String subject, String message, NotificationType type) {
        switch (type) {
            case EMAIL:
                if (user.canReceiveEmailNotifications()) {
                    emailService.sendEmail(user.getEmail(), subject, message);
                }
                break;
            case TELEGRAM:
                if (user.canReceiveTelegramNotifications()) {
                    telegramService.sendMessage(user, message);
                }
                break;
            case BOTH:
                if (user.canReceiveEmailNotifications()) {
                    emailService.sendEmail(user.getEmail(), subject, message);
                }
                if (user.canReceiveTelegramNotifications()) {
                    telegramService.sendMessage(user, message);
                }
                break;
            case NONE:
            default:
                log.debug("No notification sent for user: {}", user.getEmail());
                break;
        }
    }

    // Helper methods
    private String buildVerificationUrl(String token) {
        return "https://emenu-platform.com/verify-email?token=" + token;
    }

    private String buildPasswordResetUrl(String token) {
        return "https://emenu-platform.com/reset-password?token=" + token;
    }

    private String buildRenewalUrl(User user) {
        return "https://emenu-platform.com/subscription/renew?user=" + user.getId();
    }

    private String getTierBenefits(String tier) {
        return switch (tier.toUpperCase()) {
            case "SILVER" -> "5% discount on orders, Priority customer support";
            case "GOLD" -> "10% discount on orders, Exclusive offers, Priority support";
            case "PLATINUM" -> "15% discount on orders, VIP support, Early access to new features";
            case "VIP" -> "20% discount on orders, Dedicated account manager, Premium features";
            default -> "Thank you for being a valued customer!";
        };
    }
}
