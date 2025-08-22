// src/main/java/com/emenu/shared/notification/TelegramNotificationService.java
package com.emenu.features.notification.service;

import com.emenu.config.TelegramConfig;
import com.emenu.features.auth.models.User;
import com.emenu.features.auth.repository.UserRepository;
import com.emenu.features.notification.constants.TelegramMessages;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelegramNotificationService {

    private final TelegramConfig telegramConfig;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate;

    private static final String TELEGRAM_API_URL = "https://api.telegram.org/bot";

    // ===== USER REGISTRATION NOTIFICATIONS =====

    @Async
    public CompletableFuture<Void> sendCustomerRegistrationNotification(User customer) {
        if (!isTelegramEnabled()) {
            log.debug("Telegram is disabled, skipping customer registration notification");
            return CompletableFuture.completedFuture(null);
        }

        log.info("📱 Sending Telegram notification for customer registration: {}", customer.getUserIdentifier());

        try {
            // Send welcome message to customer if they have Telegram
            if (customer.hasTelegramLinked()) {
                sendCustomerWelcomeMessage(customer);
            }

            // Send admin notification to platform owner
            sendCustomerRegistrationToAdmin(customer);

            log.info("✅ Customer registration notifications sent successfully for: {}", customer.getUserIdentifier());
        } catch (Exception e) {
            log.error("❌ Failed to send customer registration notifications for: {}", customer.getUserIdentifier(), e);
        }

        return CompletableFuture.completedFuture(null);
    }

    @Async
    public CompletableFuture<Void> sendTelegramLinkNotification(User user) {
        if (!isTelegramEnabled()) {
            log.debug("Telegram is disabled, skipping Telegram link notification");
            return CompletableFuture.completedFuture(null);
        }

        log.info("📱 Sending Telegram link notification for user: {}", user.getUserIdentifier());

        try {
            // Send success message to user themselves
            sendTelegramLinkSuccessMessage(user);

            // Send admin notification to platform owner
            sendTelegramLinkToAdmin(user);

            log.info("✅ Telegram link notifications sent successfully for: {}", user.getUserIdentifier());
        } catch (Exception e) {
            log.error("❌ Failed to send Telegram link notifications for: {}", user.getUserIdentifier(), e);
        }

        return CompletableFuture.completedFuture(null);
    }

    @Async
    public CompletableFuture<Void> sendBusinessRegistrationNotification(User businessOwner, String businessName, String subdomain) {
        if (!isTelegramEnabled()) {
            log.debug("Telegram is disabled, skipping business registration notification");
            return CompletableFuture.completedFuture(null);
        }

        log.info("📱 Sending business registration notification for: {}", businessName);

        try {
            String message = TelegramMessages.buildBusinessRegistrationMessage(
                    businessName,
                    businessOwner.getFullName(),
                    businessOwner.getUserIdentifier(),
                    subdomain,
                    businessOwner.getCreatedAt()
            );

            // Send to business owner if they have Telegram
            if (businessOwner.hasTelegramLinked()) {
                sendTelegramMessage(businessOwner.getTelegramUserId().toString(), message);
            }

            // Send to platform owner
            sendTelegramMessage(telegramConfig.getBot().getChatId(), message);

            log.info("✅ Business registration notifications sent for: {}", businessName);
        } catch (Exception e) {
            log.error("❌ Failed to send business registration notifications for: {}", businessName, e);
        }

        return CompletableFuture.completedFuture(null);
    }

    // ===== PRIVATE HELPER METHODS =====

    private void sendCustomerWelcomeMessage(User customer) {
        String message = TelegramMessages.buildCustomerWelcomeMessage(
                customer.getDisplayName(),
                customer.getUserIdentifier(),
                customer.getCreatedAt()
        );

        sendTelegramMessage(customer.getTelegramUserId().toString(), message);
        log.info("📩 Customer welcome message sent to: {} (Telegram: {})", 
                customer.getUserIdentifier(), customer.getTelegramUserId());
    }

    private void sendTelegramLinkSuccessMessage(User user) {
        String message = TelegramMessages.buildTelegramLinkSuccessMessage(
                user.getDisplayName(),
                user.getUserIdentifier(),
                user.getUserType().getDescription(),
                user.getTelegramLinkedAt()
        );

        sendTelegramMessage(user.getTelegramUserId().toString(), message);
        log.info("📩 Telegram link success message sent to: {} (Telegram: {})", 
                user.getUserIdentifier(), user.getTelegramUserId());
    }

    private void sendCustomerRegistrationToAdmin(User customer) {
        String message = TelegramMessages.buildCustomerRegistrationAdminMessage(
                customer.getUserIdentifier(),
                customer.getFullName(),
                customer.getEmail(),
                customer.getPhoneNumber(),
                customer.getSocialProvider().getDisplayName(),
                customer.hasTelegramLinked(),
                customer.getCreatedAt()
        );

        sendTelegramMessage(telegramConfig.getBot().getChatId(), message);
        log.info("📩 Customer registration admin notification sent for: {}", customer.getUserIdentifier());
    }

    private void sendTelegramLinkToAdmin(User user) {
        String message = TelegramMessages.buildTelegramLinkAdminMessage(
                user.getUserIdentifier(),
                user.getFullName(),
                user.getUserType().getDescription(),
                user.getTelegramUsername(),
                user.getTelegramUserId(),
                user.getTelegramLinkedAt()
        );

        sendTelegramMessage(telegramConfig.getBot().getChatId(), message);
        log.info("📩 Telegram link admin notification sent for: {}", user.getUserIdentifier());
    }

    private void sendTelegramMessage(String chatId, String message) {
        try {
            String url = TELEGRAM_API_URL + telegramConfig.getBot().getToken() + "/sendMessage";

            Map<String, Object> payload = new HashMap<>();
            payload.put("chat_id", chatId);
            payload.put("text", message);
            payload.put("parse_mode", "HTML");
            payload.put("disable_web_page_preview", true);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.debug("✅ Telegram message sent successfully to chat: {}", chatId);
            } else {
                log.warn("⚠️ Telegram API returned status: {} for chat: {}", 
                        response.getStatusCode(), chatId);
            }
        } catch (Exception e) {
            log.error("❌ Failed to send Telegram message to chat: {} - Error: {}", chatId, e.getMessage());
        }
    }

    // ===== PUBLIC UTILITY METHODS =====

    public boolean isTelegramEnabled() {
        return telegramConfig.getBot().isEnabled();
    }

    public void sendCustomMessage(String chatId, String message) {
        if (isTelegramEnabled()) {
            sendTelegramMessage(chatId, message);
        }
    }

    public void sendCustomMessage(Long telegramUserId, String message) {
        if (isTelegramEnabled()) {
            sendTelegramMessage(telegramUserId.toString(), message);
        }
    }
}