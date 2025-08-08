
package com.emenu.features.notification.mapper;

import com.emenu.features.auth.models.User;
import com.emenu.features.notification.dto.request.MultiRecipientNotificationRequest;
import com.emenu.features.notification.dto.response.NotificationSendResult;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class NotificationMapper {

    public MultiRecipientNotificationRequest createUserRegistrationNotification(User user) {
        Map<String, Object> templateData = new HashMap<>();
        templateData.put("userIdentifier", user.getUserIdentifier());
        templateData.put("fullName", user.getDisplayName());
        templateData.put("userType", user.getUserType().name());
        templateData.put("email", user.getEmail() != null ? user.getEmail() : "Not provided");
        templateData.put("registeredAt", LocalDateTime.now().toString());
        templateData.put("hasTelegram", user.hasTelegramLinked() ? "Yes" : "No");
        templateData.put("socialProvider", user.getSocialProvider().getDisplayName());

        return MultiRecipientNotificationRequest.builder()
                .notificationType("USER_REGISTERED")
                .title("New User Registration")
                .message("""
                        👤 <b>New User Registered!</b>
                        
                        🆔 <b>User ID:</b> {userIdentifier}
                        👤 <b>Name:</b> {fullName}
                        🏷️ <b>Type:</b> {userType}
                        📧 <b>Email:</b> {email}
                        📱 <b>Provider:</b> {socialProvider}
                        🔗 <b>Telegram:</b> {hasTelegram}
                        📅 <b>Date:</b> {registeredAt}
                        """)
                .templateData(templateData)
                .includePlatformUsers(true)
                .includeBusinessOwners(false)
                .includeCustomers(false)
                .sendImmediate(true)
                .sourceAction("USER_REGISTRATION")
                .build();
    }

    public MultiRecipientNotificationRequest createBusinessRegistrationNotification(
            String businessName, String ownerName, String ownerUserIdentifier, 
            String businessEmail, String phoneNumber, String subdomain) {
        
        Map<String, Object> templateData = new HashMap<>();
        templateData.put("businessName", businessName);
        templateData.put("ownerName", ownerName);
        templateData.put("ownerUserIdentifier", ownerUserIdentifier);
        templateData.put("businessEmail", businessEmail != null ? businessEmail : "Not provided");
        templateData.put("phoneNumber", phoneNumber != null ? phoneNumber : "Not provided");
        templateData.put("subdomain", subdomain != null ? subdomain : "Not set");
        templateData.put("registeredAt", LocalDateTime.now().toString());

        return MultiRecipientNotificationRequest.builder()
                .notificationType("BUSINESS_REGISTERED")
                .title("New Business Registration")
                .message("""
                        🏪 <b>New Business Registered!</b>
                        
                        🏢 <b>Business:</b> {businessName}
                        👤 <b>Owner:</b> {ownerName} ({ownerUserIdentifier})
                        📧 <b>Email:</b> {businessEmail}
                        📞 <b>Phone:</b> {phoneNumber}
                        🌐 <b>Subdomain:</b> {subdomain}
                        📅 <b>Date:</b> {registeredAt}
                        """)
                .templateData(templateData)
                .includePlatformUsers(true)
                .includeBusinessOwners(false)
                .includeCustomers(false)
                .sendImmediate(true)
                .sourceAction("BUSINESS_REGISTRATION")
                .build();
    }

    public MultiRecipientNotificationRequest createProductCreatedNotification(
            String productName, String businessName, String price, 
            String categoryName, String createdBy) {
        
        Map<String, Object> templateData = new HashMap<>();
        templateData.put("productName", productName);
        templateData.put("businessName", businessName);
        templateData.put("price", price);
        templateData.put("categoryName", categoryName);
        templateData.put("createdBy", createdBy);
        templateData.put("createdAt", LocalDateTime.now().toString());

        return MultiRecipientNotificationRequest.builder()
                .notificationType("PRODUCT_CREATED")
                .title("New Product Created")
                .message("""
                        🆕 <b>New Product Created!</b>
                        
                        📱 <b>Product:</b> {productName}
                        🏪 <b>Business:</b> {businessName}
                        💰 <b>Price:</b> ${price}
                        📂 <b>Category:</b> {categoryName}
                        👤 <b>Created by:</b> {createdBy}
                        📅 <b>Date:</b> {createdAt}
                        """)
                .templateData(templateData)
                .includePlatformUsers(true)
                .includeBusinessOwners(false)
                .includeCustomers(false)
                .sendImmediate(true)
                .sourceAction("PRODUCT_CREATION")
                .build();
    }

    public MultiRecipientNotificationRequest createWelcomeNotification(User user) {
        Map<String, Object> templateData = new HashMap<>();
        templateData.put("userName", user.getDisplayName());
        templateData.put("userType", user.getUserType().getDescription());
        templateData.put("platform", "Cambodia E-Menu Platform");
        templateData.put("supportEmail", "support@cambodia-emenu.com");

        return MultiRecipientNotificationRequest.builder()
                .notificationType("WELCOME_MESSAGE")
                .title("Welcome to Cambodia E-Menu Platform")
                .message("""
                        🎉 <b>Welcome to Cambodia E-Menu Platform!</b>
                        
                        👋 Hi {userName}!
                        
                        ✅ Your {userType} account is now active.
                        🔔 You'll receive important notifications here.
                        
                        🚀 Get started:
                        • Explore our features
                        • Complete your profile
                        • Contact us: {supportEmail}
                        
                        Thank you for joining {platform}! 🇰🇭
                        """)
                .templateData(templateData)
                .specificUserIds(List.of(user.getId()))
                .sendImmediate(true)
                .sourceAction("USER_WELCOME")
                .build();
    }

    public NotificationSendResult.RecipientResult createRecipientResult(
            User user, boolean success, String error, String channel) {
        
        return NotificationSendResult.RecipientResult.builder()
                .userId(user.getId())
                .telegramUserId(user.getTelegramUserId())
                .recipientName(user.getDisplayName())
                .channel(channel)
                .success(success)
                .error(error)
                .sentAt(LocalDateTime.now())
                .build();
    }

    public NotificationSendResult createSuccessResult(
            String notificationType, int totalRecipients, int successCount, 
            List<NotificationSendResult.RecipientResult> results) {
        
        return NotificationSendResult.builder()
                .notificationType(notificationType)
                .sentAt(LocalDateTime.now())
                .totalRecipients(totalRecipients)
                .successfulSends(successCount)
                .failedSends(totalRecipients - successCount)
                .telegramSent(successCount)
                .telegramFailed(totalRecipients - successCount)
                .results(results)
                .allSuccessful(successCount == totalRecipients)
                .summary(String.format("Sent to %d/%d recipients successfully", successCount, totalRecipients))
                .build();
    }

    public NotificationSendResult createErrorResult(String notificationType, String error) {
        return NotificationSendResult.builder()
                .notificationType(notificationType)
                .sentAt(LocalDateTime.now())
                .totalRecipients(0)
                .successfulSends(0)
                .failedSends(1)
                .allSuccessful(false)
                .summary("Failed to send notification: " + error)
                .errors(List.of(error))
                .build();
    }
}