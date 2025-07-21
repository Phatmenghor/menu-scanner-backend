package com.emenu.features.notification.service;

import java.util.Map;

public interface TelegramService {
    
    // Basic Telegram Messaging
    void sendTelegramMessage(String chatId, String message);
    void sendTelegramMessage(String chatId, String message, Map<String, Object> options);
    
    // Template-based Telegram
    void sendTemplatedTelegramMessage(String chatId, String templateName, Map<String, String> variables);
    
    // Business Telegram Notifications
    void sendSubscriptionAlert(String chatId, String businessName, String alertType);
    void sendPaymentReminder(String chatId, String businessName, String paymentDetails);
    void sendSystemAlert(String chatId, String alertMessage);
    
    // Telegram Bot Management
    boolean isTelegramConfigured();
    String getBotInfo();
    void setWebhook(String webhookUrl);
}
