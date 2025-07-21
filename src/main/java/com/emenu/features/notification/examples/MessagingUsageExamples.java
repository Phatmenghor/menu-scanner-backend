package com.emenu.features.notification.examples;

import com.emenu.enums.notification.AlertType;
import com.emenu.enums.notification.MessageType;
import com.emenu.enums.notification.NotificationChannel;
import com.emenu.features.notification.dto.request.BulkNotificationRequest;
import com.emenu.features.notification.dto.request.MessageThreadCreateRequest;
import com.emenu.features.notification.dto.request.NotificationCreateRequest;
import com.emenu.features.notification.service.AlertService;
import com.emenu.features.notification.service.EmailService;
import com.emenu.features.notification.service.MessagingService;
import com.emenu.features.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class MessagingUsageExamples {

    private final MessagingService messagingService;
    private final NotificationService notificationService;
    private final AlertService alertService;
    private final EmailService emailService;

    /**
     * Example 1: Send a subscription expiry alert to a business
     */
    public void sendSubscriptionExpiryAlert(UUID businessId, String businessName, int daysRemaining) {
        log.info("Example: Sending subscription expiry alert");
        
        // Method 1: Using AlertService (recommended for system alerts)
        alertService.sendSubscriptionExpiringAlert(businessId, daysRemaining);
        
        // Method 2: Manual notification creation
        NotificationCreateRequest request = new NotificationCreateRequest();
        request.setRecipientId(businessId); // This should be user ID in real scenario
        request.setTitle("Subscription Expiring Soon");
        request.setContent(String.format(
            "Your subscription for %s will expire in %d days. Please renew to continue.",
            businessName, daysRemaining
        ));
        request.setChannel(NotificationChannel.EMAIL);
        request.setAlertType(AlertType.SUBSCRIPTION_EXPIRING_SOON);
        request.setBusinessId(businessId);
        
        var notification = notificationService.createNotification(request);
        notificationService.sendNotification(notification.getId());
    }

    /**
     * Example 2: Create a support ticket (message thread)
     */
    public void createSupportTicket(UUID customerId, String subject, String initialMessage) {
        log.info("Example: Creating support ticket");
        
        MessageThreadCreateRequest threadRequest = new MessageThreadCreateRequest();
        threadRequest.setSubject(subject);
        threadRequest.setMessageType(MessageType.SUPPORT_TICKET);
        threadRequest.setContent(initialMessage);
        threadRequest.setCustomerId(customerId);
        threadRequest.setPriority(2); // Normal priority
        threadRequest.setIsSystemGenerated(false);
        
        var thread = messagingService.createThread(threadRequest);
        log.info("Support ticket created with ID: {}", thread.getId());
    }

    /**
     * Example 3: Send bulk notification to all business owners
     */
    public void sendBulkAnnouncementToBusinessOwners(String title, String message) {
        log.info("Example: Sending bulk announcement");
        
        BulkNotificationRequest bulkRequest = new BulkNotificationRequest();
        bulkRequest.setTitle(title);
        bulkRequest.setContent(message);
        bulkRequest.setChannel(NotificationChannel.IN_APP);
        bulkRequest.setAlertType(AlertType.PLATFORM_ANNOUNCEMENT);
        bulkRequest.setUserTypes(Arrays.asList(com.emenu.enums.UserType.BUSINESS_USER));
        bulkRequest.setOnlyActiveUsers(true);
        
        var notifications = notificationService.createBulkNotification(bulkRequest);
        log.info("Created {} bulk notifications", notifications.size());
    }

    /**
     * Example 4: Send templated email
     */
    public void sendWelcomeEmailToNewUser(String userEmail, String userName) {
        log.info("Example: Sending templated welcome email");
        
        // Method 1: Direct email service
        emailService.sendWelcomeEmail(userEmail, userName);
        
        // Method 2: Using template with variables
        Map<String, String> variables = new HashMap<>();
        variables.put("userName", userName);
        variables.put("platformName", "E-Menu Platform");
        variables.put("supportEmail", "support@emenu-platform.com");
        
        emailService.sendTemplatedEmail(userEmail, "WELCOME_USER", variables);
    }

    /**
     * Example 5: Schedule notification for future delivery
     */
    public void scheduleMaintenanceNotification(LocalDateTime maintenanceTime) {
        log.info("Example: Scheduling maintenance notification");
        
        NotificationCreateRequest request = new NotificationCreateRequest();
        request.setRecipientId(UUID.randomUUID()); // Should be actual admin user ID
        request.setTitle("Scheduled Maintenance Alert");
        request.setContent(String.format(
            "System maintenance is scheduled for %s. Please prepare accordingly.",
            maintenanceTime
        ));
        request.setChannel(NotificationChannel.EMAIL);
        request.setAlertType(AlertType.SYSTEM_ALERT);
        request.setScheduledAt(maintenanceTime.minusHours(24)); // Send 24 hours before
        
        var notification = notificationService.scheduleNotification(request);
        log.info("Maintenance notification scheduled with ID: {}", notification.getId());
    }

    /**
     * Example 6: Send payment reminder with custom variables
     */
    public void sendPaymentReminder(UUID businessId, String businessName, String amount) {
        log.info("Example: Sending payment reminder");
        
        // Using templated notification
        Map<String, String> variables = new HashMap<>();
        variables.put("businessName", businessName);
        variables.put("amount", amount);
        variables.put("dueDate", LocalDateTime.now().plusDays(7).toLocalDate().toString());
        
        var notification = notificationService.sendTemplatedNotification(
            businessId, // Should be user ID
            "PAYMENT_REMINDER",
            variables,
            NotificationChannel.EMAIL
        );
        
        log.info("Payment reminder sent with ID: {}", notification.getId());
    }

    /**
     * Example 7: Business limit reached alert
     */
    public void sendBusinessLimitAlert(UUID businessId, String limitType) {
        log.info("Example: Sending business limit alert");
        
        switch (limitType.toLowerCase()) {
            case "staff" -> alertService.sendStaffLimitReachedAlert(businessId);
            case "menu" -> alertService.sendMenuLimitReachedAlert(businessId);
            default -> {
                String message = String.format("You have reached your %s limit. Please upgrade your plan.", limitType);
                alertService.sendCustomAlert(businessId, AlertType.SUBSCRIPTION_LIMIT_EXCEEDED, 
                    "Limit Reached", message);
            }
        }
    }

    /**
     * Example 8: Create customer inquiry thread
     */
    public void createCustomerInquiry(UUID customerId, UUID businessId, String inquiry) {
        log.info("Example: Creating customer inquiry");
        
        MessageThreadCreateRequest threadRequest = new MessageThreadCreateRequest();
        threadRequest.setSubject("Customer Inquiry");
        threadRequest.setMessageType(MessageType.CUSTOMER_SUPPORT);
        threadRequest.setContent(inquiry);
        threadRequest.setCustomerId(customerId);
        threadRequest.setBusinessId(businessId);
        threadRequest.setPriority(1); // Low priority
        
        var thread = messagingService.createThread(threadRequest);
        
        // Notify business about new inquiry
        NotificationCreateRequest notificationRequest = new NotificationCreateRequest();
        notificationRequest.setRecipientId(businessId); // Should be business owner user ID
        notificationRequest.setTitle("New Customer Inquiry");
        notificationRequest.setContent("You have received a new customer inquiry. Please check your messages.");
        notificationRequest.setChannel(NotificationChannel.IN_APP);
        notificationRequest.setAlertType(AlertType.CUSTOMER_INQUIRY);
        notificationRequest.setBusinessId(businessId);
        
        var notification = notificationService.createNotification(notificationRequest);
        notificationService.sendNotification(notification.getId());
        
        log.info("Customer inquiry created and business notified");
    }

    /**
     * Example 9: Send security alert
     */
    public void sendSecurityAlert(UUID userId, String ipAddress, String location) {
        log.info("Example: Sending security alert");
        
        alertService.sendLoginAlert(userId, ipAddress, location);
        
        // Additional custom security alert
        String message = String.format(
            "Suspicious login activity detected from IP %s (%s). If this wasn't you, please secure your account immediately.",
            ipAddress, location
        );
        
        alertService.sendSecurityAlert(userId, "Suspicious Login", message);
    }

    /**
     * Example 10: Process subscription renewal workflow
     */
    public void processSubscriptionRenewal(UUID businessId, String businessName, UUID subscriptionId) {
        log.info("Example: Processing subscription renewal workflow");
        
        // 1. Send renewal success notification
        NotificationCreateRequest request = new NotificationCreateRequest();
        request.setRecipientId(businessId); // Should be user ID
        request.setTitle("Subscription Renewed Successfully");
        request.setContent(String.format(
            "Your subscription for %s has been renewed successfully. Thank you for continuing with us!",
            businessName
        ));
        request.setChannel(NotificationChannel.IN_APP);
        request.setAlertType(AlertType.SUBSCRIPTION_RENEWAL);
        request.setBusinessId(businessId);
        request.setRelatedEntityType("Subscription");
        request.setRelatedEntityId(subscriptionId);
        
        var notification = notificationService.createNotification(request);
        notificationService.sendNotification(notification.getId());
        
        // 2. Send confirmation email
        Map<String, String> variables = new HashMap<>();
        variables.put("businessName", businessName);
        variables.put("renewalDate", LocalDateTime.now().toLocalDate().toString());
        
        // This would require the recipient's email - in real scenario, fetch from user repository
        // emailService.sendTemplatedEmail(recipientEmail, "SUBSCRIPTION_RENEWAL_SUCCESS", variables);
        
        log.info("Subscription renewal notifications sent for business: {}", businessName);
    }
}