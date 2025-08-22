package com.emenu.features.notification.service;

import com.emenu.features.auth.models.User;
import com.emenu.features.notification.dto.response.OrderNotificationDto;
import com.emenu.features.notification.dto.response.SubscriptionAlertDto;
import com.emenu.features.notification.dto.response.SystemEventDto;

import java.util.concurrent.CompletableFuture;

public interface TelegramNotificationService {

    CompletableFuture<Void> sendPlatformUserCreationNotification(User newUser, User createdBy);

    // ===== PLATFORM-WIDE NOTIFICATIONS (TO GROUP) =====
    CompletableFuture<Void> sendCustomerRegistrationNotification(User customer);
    CompletableFuture<Void> sendBusinessRegistrationNotification(User businessOwner, String businessName, String subdomain);
    CompletableFuture<Void> sendTelegramLinkNotification(User user);
    CompletableFuture<Void> sendSystemEventNotification(SystemEventDto systemEvent);

    // ===== BUSINESS-SPECIFIC NOTIFICATIONS =====
    CompletableFuture<Void> sendOrderNotificationToBusiness(OrderNotificationDto orderNotification);
    CompletableFuture<Void> sendSubscriptionAlertToBusiness(SubscriptionAlertDto subscriptionAlert);
    CompletableFuture<Void> sendOrderStatusUpdateToCustomer(OrderNotificationDto orderUpdate);

    // ===== UTILITY METHODS =====
    boolean isTelegramEnabled();
    void sendCustomMessage(Long telegramUserId, String message);
    void sendGroupMessage(String message);
}