package com.emenu.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "telegram")
@Data
public class TelegramConfig {
    
    private Bot bot = new Bot();
    private Notifications notifications = new Notifications();
    private Templates templates = new Templates();
    private Webhook webhook = new Webhook();
    private Features features = new Features();
    
    @Data
    public static class Bot {
        private String token;
        private boolean enabled = true;
        private String chatId; // Default admin chat ID
        private String webhookUrl;
        private boolean polling = false; // Disable polling for webhook mode
        private int timeout = 30;
        private int retryAttempts = 3;
        private String baseUrl = "https://api.telegram.org/bot";
        
        // Bot information
        private String name = "Cambodia E-Menu Bot";
        private String username;
        private String description = "Digital menu platform for Cambodia restaurants";
    }
    
    @Data
    public static class Notifications {
        private boolean enabled = true;
        private boolean productCreated = true;
        private boolean userRegistered = true;
        private boolean businessRegistered = true;
        private boolean orderPlaced = true;
        private boolean paymentReceived = true;
        private boolean subscriptionExpiring = true;
        private boolean systemAlerts = true;
        
        // Recipients configuration
        private Recipients recipients = new Recipients();
        
        @Data
        public static class Recipients {
            private boolean notifyPlatformUsers = true;
            private boolean notifyBusinessOwners = false;
            private boolean notifyCustomers = true;
            private boolean notifyAdminsOnly = false;
        }
    }
    
    @Data
    public static class Templates {
        private String productCreated;
        private String userRegistered;
        private String businessRegistered;
        private String orderPlaced;
        private String paymentReceived;
        private String subscriptionExpiring;
        private String systemAlert;
        private String welcomeMessage;
        private String helpMessage;
    }
    
    @Data
    public static class Webhook {
        private boolean enabled = true;
        private String url;
        private String secretToken;
        private int maxConnections = 40;
        private String[] allowedUpdates = {"message", "callback_query", "inline_query"};
        private boolean dropPendingUpdates = true;
    }
    
    @Data
    public static class Features {
        private boolean botCommands = true;
        private boolean inlineKeyboards = true;
        private boolean fileUploads = false;
        private boolean groupChat = false;
        private boolean channelPosts = false;
        private boolean inlineQueries = true;
        private boolean callbackQueries = true;
        
        // Authentication features
        private boolean loginWidget = true;
        private boolean registration = true;
        private boolean accountLinking = true;
        
        // Business features
        private boolean businessManagement = true;
        private boolean orderNotifications = true;
        private boolean menuUpdates = true;
        
        // Admin features
        private boolean adminCommands = true;
        private boolean systemMonitoring = true;
        private boolean userManagement = true;
    }
    
    // Helper methods
    public boolean isWebhookMode() {
        return webhook.enabled && webhook.url != null && !webhook.url.trim().isEmpty();
    }
    
    public boolean isPollingMode() {
        return bot.polling && !isWebhookMode();
    }
    
    public String getFullWebhookUrl() {
        if (webhook.url == null) {
            return null;
        }
        return webhook.url + (webhook.url.endsWith("/") ? "" : "/") + "webhook";
    }
    
    public boolean shouldNotifyRecipientType(String recipientType) {
        return switch (recipientType.toUpperCase()) {
            case "PLATFORM_USERS" -> notifications.recipients.notifyPlatformUsers;
            case "BUSINESS_OWNERS" -> notifications.recipients.notifyBusinessOwners;
            case "CUSTOMERS" -> notifications.recipients.notifyCustomers;
            case "ADMINS_ONLY" -> notifications.recipients.notifyAdminsOnly;
            default -> false;
        };
    }
    
    public boolean isNotificationTypeEnabled(String notificationType) {
        if (!notifications.enabled) {
            return false;
        }
        
        return switch (notificationType.toUpperCase()) {
            case "PRODUCT_CREATED" -> notifications.productCreated;
            case "USER_REGISTERED" -> notifications.userRegistered;
            case "BUSINESS_REGISTERED" -> notifications.businessRegistered;
            case "ORDER_PLACED" -> notifications.orderPlaced;
            case "PAYMENT_RECEIVED" -> notifications.paymentReceived;
            case "SUBSCRIPTION_EXPIRING" -> notifications.subscriptionExpiring;
            case "SYSTEM_ALERTS" -> notifications.systemAlerts;
            default -> true; // Default to enabled for unknown types
        };
    }
    
    public boolean isFeatureEnabled(String feature) {
        return switch (feature.toUpperCase()) {
            case "BOT_COMMANDS" -> features.botCommands;
            case "INLINE_KEYBOARDS" -> features.inlineKeyboards;
            case "FILE_UPLOADS" -> features.fileUploads;
            case "GROUP_CHAT" -> features.groupChat;
            case "CHANNEL_POSTS" -> features.channelPosts;
            case "INLINE_QUERIES" -> features.inlineQueries;
            case "CALLBACK_QUERIES" -> features.callbackQueries;
            case "LOGIN_WIDGET" -> features.loginWidget;
            case "REGISTRATION" -> features.registration;
            case "ACCOUNT_LINKING" -> features.accountLinking;
            case "BUSINESS_MANAGEMENT" -> features.businessManagement;
            case "ORDER_NOTIFICATIONS" -> features.orderNotifications;
            case "MENU_UPDATES" -> features.menuUpdates;
            case "ADMIN_COMMANDS" -> features.adminCommands;
            case "SYSTEM_MONITORING" -> features.systemMonitoring;
            case "USER_MANAGEMENT" -> features.userManagement;
            default -> false;
        };
    }
}