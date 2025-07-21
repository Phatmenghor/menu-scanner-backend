package com.emenu.features.notification.integration;

import com.emenu.enums.notification.AlertType;
import com.emenu.enums.notification.NotificationChannel;
import com.emenu.features.auth.models.Business;
import com.emenu.features.auth.models.User;
import com.emenu.features.auth.repository.BusinessRepository;
import com.emenu.features.auth.repository.UserRepository;
import com.emenu.features.notification.dto.request.NotificationCreateRequest;
import com.emenu.features.notification.service.EmailService;
import com.emenu.features.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Integration component that listens to subscription events and sends appropriate notifications
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SubscriptionMessagingIntegration {

    private final NotificationService notificationService;
    private final EmailService emailService;
    private final BusinessRepository businessRepository;
    private final UserRepository userRepository;

    /**
     * Handle subscription expiry events
     */
    @EventListener
    public void handleSubscriptionExpiry(SubscriptionExpiryEvent event) {
        log.info("Handling subscription expiry event for business: {}", event.getBusinessId());
        
        Business business = businessRepository.findByIdAndIsDeletedFalse(event.getBusinessId())
                .orElse(null);
        
        if (business == null) {
            log.warn("Business not found for subscription expiry: {}", event.getBusinessId());
            return;
        }

        List<User> businessUsers = userRepository.findByBusinessIdAndIsDeletedFalse(event.getBusinessId());
        
        for (User user : businessUsers) {
            if (isBusinessOwnerOrManager(user)) {
                sendSubscriptionExpiryNotification(user, business, event.getDaysRemaining());
            }
        }
    }

    /**
     * Handle subscription renewal events
     */
    @EventListener
    public void handleSubscriptionRenewal(SubscriptionRenewalEvent event) {
        log.info("Handling subscription renewal event for business: {}", event.getBusinessId());
        
        Business business = businessRepository.findByIdAndIsDeletedFalse(event.getBusinessId())
                .orElse(null);
        
        if (business == null) return;

        List<User> businessUsers = userRepository.findByBusinessIdAndIsDeletedFalse(event.getBusinessId());
        
        for (User user : businessUsers) {
            if (isBusinessOwnerOrManager(user)) {
                sendSubscriptionRenewalNotification(user, business, event.getNewEndDate());
            }
        }
    }

    /**
     * Handle payment confirmation events
     */
    @EventListener
    public void handlePaymentConfirmation(PaymentConfirmationEvent event) {
        log.info("Handling payment confirmation event for business: {}", event.getBusinessId());
        
        Business business = businessRepository.findByIdAndIsDeletedFalse(event.getBusinessId())
                .orElse(null);
        
        if (business == null) return;

        List<User> businessUsers = userRepository.findByBusinessIdAndIsDeletedFalse(event.getBusinessId());
        
        for (User user : businessUsers) {
            if (isBusinessOwnerOrManager(user)) {
                sendPaymentConfirmationNotification(user, business, event.getPaymentAmount(), event.getPlanName());
            }
        }
    }

    /**
     * Handle new user registration events
     */
    @EventListener
    public void handleUserRegistration(UserRegistrationEvent event) {
        log.info("Handling user registration event for user: {}", event.getUserId());
        
        User user = userRepository.findByIdAndIsDeletedFalse(event.getUserId())
                .orElse(null);
        
        if (user == null) return;

        sendWelcomeNotification(user);
    }

    private void sendSubscriptionExpiryNotification(User user, Business business, int daysRemaining) {
        // Create in-app notification
        NotificationCreateRequest notificationRequest = new NotificationCreateRequest();
        notificationRequest.setRecipientId(user.getId());
        notificationRequest.setTitle("Subscription Expiring Soon");
        notificationRequest.setContent(String.format(
            "Your subscription for %s will expire in %d days. Please renew to continue using our services.",
            business.getName(), daysRemaining
        ));
        notificationRequest.setChannel(NotificationChannel.IN_APP);
        notificationRequest.setAlertType(AlertType.SUBSCRIPTION_EXPIRING_SOON);
        notificationRequest.setBusinessId(business.getId());

        try {
            var notification = notificationService.createNotification(notificationRequest);
            notificationService.sendNotification(notification.getId());
            
            // Also send email
            emailService.sendSubscriptionExpiryEmail(user.getEmail(), business.getName(), daysRemaining);
            
        } catch (Exception e) {
            log.error("Failed to send subscription expiry notification", e);
        }
    }

    private void sendSubscriptionRenewalNotification(User user, Business business, java.time.LocalDateTime newEndDate) {
        NotificationCreateRequest notificationRequest = new NotificationCreateRequest();
        notificationRequest.setRecipientId(user.getId());
        notificationRequest.setTitle("Subscription Renewed Successfully");
        notificationRequest.setContent(String.format(
            "Your subscription for %s has been renewed successfully. Your new expiry date is %s.",
            business.getName(), newEndDate.toLocalDate()
        ));
        notificationRequest.setChannel(NotificationChannel.IN_APP);
        notificationRequest.setAlertType(AlertType.SUBSCRIPTION_RENEWAL);
        notificationRequest.setBusinessId(business.getId());

        try {
            var notification = notificationService.createNotification(notificationRequest);
            notificationService.sendNotification(notification.getId());
        } catch (Exception e) {
            log.error("Failed to send subscription renewal notification", e);
        }
    }

    private void sendPaymentConfirmationNotification(User user, Business business, 
                                                   java.math.BigDecimal amount, String planName) {
        NotificationCreateRequest notificationRequest = new NotificationCreateRequest();
        notificationRequest.setRecipientId(user.getId());
        notificationRequest.setTitle("Payment Confirmation");
        notificationRequest.setContent(String.format(
            "Payment of $%.2f for %s plan has been processed successfully for %s.",
            amount, planName, business.getName()
        ));
        notificationRequest.setChannel(NotificationChannel.IN_APP);
        notificationRequest.setAlertType(AlertType.PAYMENT_REMINDER);
        notificationRequest.setBusinessId(business.getId());

        try {
            var notification = notificationService.createNotification(notificationRequest);
            notificationService.sendNotification(notification.getId());
            
            // Also send email confirmation
            String paymentDetails = String.format("Plan: %s\nAmount: $%.2f\nDate: %s", 
                planName, amount, java.time.LocalDateTime.now().toLocalDate());
            emailService.sendPaymentConfirmationEmail(user.getEmail(), business.getName(), paymentDetails);
            
        } catch (Exception e) {
            log.error("Failed to send payment confirmation notification", e);
        }
    }

    private void sendWelcomeNotification(User user) {
        NotificationCreateRequest notificationRequest = new NotificationCreateRequest();
        notificationRequest.setRecipientId(user.getId());
        notificationRequest.setTitle("Welcome to E-Menu Platform!");
        notificationRequest.setContent(String.format(
            "Welcome %s! We're excited to have you on board. Get started by exploring our features.",
            user.getFirstName()
        ));
        notificationRequest.setChannel(NotificationChannel.IN_APP);
        notificationRequest.setAlertType(AlertType.WELCOME_NEW_USER);

        try {
            var notification = notificationService.createNotification(notificationRequest);
            notificationService.sendNotification(notification.getId());
            
            // Also send welcome email
            emailService.sendWelcomeEmail(user.getEmail(), user.getFullName());
            
        } catch (Exception e) {
            log.error("Failed to send welcome notification", e);
        }
    }

    private boolean isBusinessOwnerOrManager(User user) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getName().name().contains("BUSINESS_OWNER") || 
                                 role.getName().name().contains("BUSINESS_MANAGER"));
    }

    // Event classes for messaging integration
    public static class SubscriptionExpiryEvent {
        private final java.util.UUID businessId;
        private final int daysRemaining;

        public SubscriptionExpiryEvent(java.util.UUID businessId, int daysRemaining) {
            this.businessId = businessId;
            this.daysRemaining = daysRemaining;
        }

        public java.util.UUID getBusinessId() { return businessId; }
        public int getDaysRemaining() { return daysRemaining; }
    }

    public static class SubscriptionRenewalEvent {
        private final java.util.UUID businessId;
        private final java.time.LocalDateTime newEndDate;

        public SubscriptionRenewalEvent(java.util.UUID businessId, java.time.LocalDateTime newEndDate) {
            this.businessId = businessId;
            this.newEndDate = newEndDate;
        }

        public java.util.UUID getBusinessId() { return businessId; }
        public java.time.LocalDateTime getNewEndDate() { return newEndDate; }
    }

    public static class PaymentConfirmationEvent {
        private final java.util.UUID businessId;
        private final java.math.BigDecimal paymentAmount;
        private final String planName;

        public PaymentConfirmationEvent(java.util.UUID businessId, java.math.BigDecimal paymentAmount, String planName) {
            this.businessId = businessId;
            this.paymentAmount = paymentAmount;
            this.planName = planName;
        }

        public java.util.UUID getBusinessId() { return businessId; }
        public java.math.BigDecimal getPaymentAmount() { return paymentAmount; }
        public String getPlanName() { return planName; }
    }

    public static class UserRegistrationEvent {
        private final java.util.UUID userId;

        public UserRegistrationEvent(java.util.UUID userId) {
            this.userId = userId;
        }

        public java.util.UUID getUserId() { return userId; }
    }
}