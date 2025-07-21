package com.emenu.features.notification.service.impl;

import com.emenu.enums.notification.AlertType;
import com.emenu.enums.notification.NotificationChannel;
import com.emenu.features.auth.models.Business;
import com.emenu.features.auth.models.Subscription;
import com.emenu.features.auth.models.User;
import com.emenu.features.auth.repository.BusinessRepository;
import com.emenu.features.auth.repository.SubscriptionRepository;
import com.emenu.features.auth.repository.UserRepository;
import com.emenu.features.notification.dto.request.NotificationCreateRequest;
import com.emenu.features.notification.service.AlertService;
import com.emenu.features.notification.service.EmailService;
import com.emenu.features.notification.service.NotificationService;
import com.emenu.features.notification.service.TelegramService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AlertServiceImpl implements AlertService {

    private final NotificationService notificationService;
    private final EmailService emailService;
    private final TelegramService telegramService;
    private final BusinessRepository businessRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;

    @Override
    public void checkAndSendSubscriptionAlerts() {
        log.info("Checking subscription alerts");

        // Check subscriptions expiring in 7 days
        LocalDateTime sevenDaysFromNow = LocalDateTime.now().plusDays(7);
        List<Subscription> expiringSoon = subscriptionRepository.findExpiringSubscriptions(LocalDateTime.now(), sevenDaysFromNow);

        for (Subscription subscription : expiringSoon) {
            sendSubscriptionExpiringAlert(subscription.getBusinessId(), (int) subscription.getDaysRemaining());
        }

        // Check expired subscriptions
        List<Subscription> expired = subscriptionRepository.findExpiredSubscriptions(LocalDateTime.now());
        for (Subscription subscription : expired) {
            sendSubscriptionExpiredAlert(subscription.getBusinessId());
        }

        log.info("Subscription alerts check completed. Expiring soon: {}, Expired: {}", 
                expiringSoon.size(), expired.size());
    }

    @Override
    public void sendSubscriptionExpiringAlert(UUID businessId, int daysRemaining) {
        Business business = businessRepository.findByIdAndIsDeletedFalse(businessId)
                .orElse(null);
        
        if (business == null) {
            log.warn("Business not found for subscription alert: {}", businessId);
            return;
        }

        // Send alert to business owner/managers
        List<User> businessUsers = userRepository.findByBusinessIdAndIsDeletedFalse(businessId);
        
        for (User user : businessUsers) {
            if (isBusinessOwnerOrManager(user)) {
                sendSubscriptionExpiryNotification(user, business, daysRemaining);
            }
        }

        log.info("Subscription expiring alert sent for business: {}, days remaining: {}", 
                business.getName(), daysRemaining);
    }

    @Override
    public void sendSubscriptionExpiredAlert(UUID businessId) {
        Business business = businessRepository.findByIdAndIsDeletedFalse(businessId)
                .orElse(null);
        
        if (business == null) {
            log.warn("Business not found for expired subscription alert: {}", businessId);
            return;
        }

        // Send alert to business owner/managers
        List<User> businessUsers = userRepository.findByBusinessIdAndIsDeletedFalse(businessId);
        
        for (User user : businessUsers) {
            if (isBusinessOwnerOrManager(user)) {
                sendSubscriptionExpiredNotification(user, business);
            }
        }

        log.info("Subscription expired alert sent for business: {}", business.getName());
    }

    @Override
    public void sendSubscriptionRenewalReminder(UUID businessId) {
        // Implementation for renewal reminder
        log.info("Sending subscription renewal reminder for business: {}", businessId);
    }

    @Override
    public void checkAndSendPaymentAlerts() {
        log.info("Checking payment alerts");
        // Implementation for checking overdue payments and sending alerts
    }

    @Override
    public void sendPaymentOverdueAlert(UUID businessId) {
        // Implementation for payment overdue alert
        log.info("Sending payment overdue alert for business: {}", businessId);
    }

    @Override
    public void sendPaymentReminderAlert(UUID businessId, int daysOverdue) {
        // Implementation for payment reminder
        log.info("Sending payment reminder for business: {}, days overdue: {}", businessId, daysOverdue);
    }

    @Override
    public void sendBusinessSuspendedAlert(UUID businessId) {
        Business business = businessRepository.findByIdAndIsDeletedFalse(businessId)
                .orElse(null);
        
        if (business == null) return;

        String title = "Business Account Suspended";
        String message = String.format("Your business account '%s' has been suspended. Please contact support for assistance.", business.getName());
        
        List<User> businessUsers = userRepository.findByBusinessIdAndIsDeletedFalse(businessId);
        for (User user : businessUsers) {
            sendAlert(user.getId(), AlertType.ACCOUNT_SUSPENDED, title, message);
        }

        log.info("Business suspended alert sent for: {}", business.getName());
    }

    @Override
    public void sendBusinessReactivatedAlert(UUID businessId) {
        Business business = businessRepository.findByIdAndIsDeletedFalse(businessId)
                .orElse(null);
        
        if (business == null) return;

        String title = "Business Account Reactivated";
        String message = String.format("Your business account '%s' has been reactivated. You can now access all services.", business.getName());
        
        List<User> businessUsers = userRepository.findByBusinessIdAndIsDeletedFalse(businessId);
        for (User user : businessUsers) {
            sendAlert(user.getId(), AlertType.WELCOME_NEW_USER, title, message);
        }

        log.info("Business reactivated alert sent for: {}", business.getName());
    }

    @Override
    public void sendStaffLimitReachedAlert(UUID businessId) {
        String title = "Staff Limit Reached";
        String message = "You have reached your subscription's staff limit. Upgrade your plan to add more staff members.";
        sendBusinessAlert(businessId, AlertType.STAFF_LIMIT_REACHED, title, message);
    }

    @Override
    public void sendMenuLimitReachedAlert(UUID businessId) {
        String title = "Menu Items Limit Reached";
        String message = "You have reached your subscription's menu items limit. Upgrade your plan to add more items.";
        sendBusinessAlert(businessId, AlertType.MENU_LIMIT_REACHED, title, message);
    }

    @Override
    public void sendSecurityAlert(UUID userId, String alertType, String description) {
        String title = "Security Alert: " + alertType;
        sendAlert(userId, AlertType.SECURITY_ALERT, title, description);
        log.info("Security alert sent to user: {}, type: {}", userId, alertType);
    }

    @Override
    public void sendLoginAlert(UUID userId, String ipAddress, String location) {
        String title = "New Login Detected";
        String message = String.format("A new login was detected from IP %s (%s). If this wasn't you, please secure your account immediately.", ipAddress, location);
        sendAlert(userId, AlertType.SECURITY_ALERT, title, message);
    }

    @Override
    public void sendPasswordChangeAlert(UUID userId) {
        String title = "Password Changed";
        String message = "Your password has been changed successfully. If you didn't make this change, please contact support immediately.";
        sendAlert(userId, AlertType.PASSWORD_RESET, title, message);
    }

    @Override
    public void sendAccountLockedAlert(UUID userId) {
        String title = "Account Locked";
        String message = "Your account has been locked due to multiple failed login attempts. Please contact support to unlock your account.";
        sendAlert(userId, AlertType.SECURITY_ALERT, title, message);
    }

    @Override
    public void sendSystemMaintenanceAlert() {
        String title = "Scheduled System Maintenance";
        String message = "Our system will undergo scheduled maintenance tonight from 2 AM to 4 AM. Some services may be temporarily unavailable.";
        
        // Send to all platform users and business owners
        sendBulkSystemAlert(title, message);
        log.info("System maintenance alert sent to all users");
    }

    @Override
    public void sendSystemUpdateAlert(String updateDetails) {
        String title = "System Update Available";
        String message = "A new system update is available with the following improvements: " + updateDetails;
        
        sendBulkSystemAlert(title, message);
        log.info("System update alert sent: {}", updateDetails);
    }

    @Override
    public void sendBackupCompleteAlert() {
        String title = "System Backup Completed";
        String message = "Daily system backup has been completed successfully.";
        
        // Send only to platform administrators
        sendPlatformAdminAlert(title, message);
        log.info("Backup complete alert sent to platform administrators");
    }

    @Override
    public void sendCustomAlert(UUID recipientId, AlertType alertType, String title, String message) {
        sendAlert(recipientId, alertType, title, message);
        log.info("Custom alert sent to user: {}", recipientId);
    }

    @Override
    public void sendBulkAlert(List<UUID> recipientIds, AlertType alertType, String title, String message) {
        for (UUID recipientId : recipientIds) {
            sendAlert(recipientId, alertType, title, message);
        }
        log.info("Bulk alert sent to {} recipients", recipientIds.size());
    }

    @Override
    public void enableAlert(UUID userId, AlertType alertType) {
        // Implementation for enabling specific alert types for users
        log.info("Alert {} enabled for user: {}", alertType, userId);
    }

    @Override
    public void disableAlert(UUID userId, AlertType alertType) {
        // Implementation for disabling specific alert types for users
        log.info("Alert {} disabled for user: {}", alertType, userId);
    }

    @Override
    public boolean isAlertEnabled(UUID userId, AlertType alertType) {
        // Implementation for checking if alert is enabled
        // For now, return true (all alerts enabled)
        return true;
    }

    // Private helper methods
    private void sendSubscriptionExpiryNotification(User user, Business business, int daysRemaining) {
        String title = "Subscription Expiring Soon";
        String message = String.format(
            "Your subscription for %s will expire in %d days. Please renew to continue using our services.",
            business.getName(), daysRemaining
        );

        // Send in-app notification
        sendAlert(user.getId(), AlertType.SUBSCRIPTION_EXPIRING_SOON, title, message);

        // Send email notification
        emailService.sendSubscriptionExpiryEmail(user.getEmail(), business.getName(), daysRemaining);
    }

    private void sendSubscriptionExpiredNotification(User user, Business business) {
        String title = "Subscription Expired";
        String message = String.format(
            "Your subscription for %s has expired. Please renew immediately to restore access.",
            business.getName()
        );

        // Send in-app notification
        sendAlert(user.getId(), AlertType.SUBSCRIPTION_EXPIRED, title, message);

        // Send email notification
        emailService.sendSubscriptionExpiredEmail(user.getEmail(), business.getName());
    }

    private void sendAlert(UUID recipientId, AlertType alertType, String title, String message) {
        NotificationCreateRequest request = new NotificationCreateRequest();
        request.setRecipientId(recipientId);
        request.setTitle(title);
        request.setContent(message);
        request.setAlertType(alertType);
        request.setChannel(NotificationChannel.IN_APP);

        try {
            var notification = notificationService.createNotification(request);
            notificationService.sendNotification(notification.getId());
        } catch (Exception e) {
            log.error("Failed to send alert to user: {}", recipientId, e);
        }
    }

    private void sendBusinessAlert(UUID businessId, AlertType alertType, String title, String message) {
        List<User> businessUsers = userRepository.findByBusinessIdAndIsDeletedFalse(businessId);
        
        for (User user : businessUsers) {
            if (isBusinessOwnerOrManager(user)) {
                sendAlert(user.getId(), alertType, title, message);
            }
        }
    }

    private void sendBulkSystemAlert(String title, String message) {
        // Get all active platform users and business owners
        // This would be implemented with proper user queries
        log.info("Sending bulk system alert: {}", title);
    }

    private void sendPlatformAdminAlert(String title, String message) {
        // Get all platform administrators
        // This would be implemented with proper user queries for platform roles
        log.info("Sending platform admin alert: {}", title);
    }

    private boolean isBusinessOwnerOrManager(User user) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getName().name().contains("BUSINESS_OWNER") || 
                                 role.getName().name().contains("BUSINESS_MANAGER"));
    }
}