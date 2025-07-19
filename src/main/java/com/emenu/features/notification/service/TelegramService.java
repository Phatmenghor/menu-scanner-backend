package com.emenu.features.notification.service;

import com.emenu.features.usermanagement.domain.User;

public interface TelegramService {
    void sendMessage(String chatId, String message);
    void sendMessage(User user, String message);
    void sendWelcomeMessage(User user);
    void sendOrderNotification(User user, String orderDetails);
    boolean isUserRegistered(User user);
}