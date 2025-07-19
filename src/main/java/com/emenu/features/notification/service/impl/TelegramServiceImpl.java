package com.emenu.features.notification.service.impl;

import com.emenu.features.notification.service.TelegramService;
import com.emenu.features.usermanagement.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelegramServiceImpl implements TelegramService {

    private final RestTemplate restTemplate;

    @Value("${app.notifications.telegram.bot-token}")
    private String botToken;

    @Value("${app.notifications.telegram.api-url}")
    private String apiUrl;

    @Value("${app.notifications.telegram.enabled:true}")
    private boolean telegramEnabled;

    @Override
    @Async
    public void sendMessage(String chatId, String message) {
        if (!telegramEnabled) {
            log.info("Telegram notifications disabled, skipping message to chat: {}", chatId);
            return;
        }

        try {
            String url = apiUrl + botToken + "/sendMessage";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            Map<String, Object> body = new HashMap<>();
            body.put("chat_id", chatId);
            body.put("text", message);
            body.put("parse_mode", "HTML");
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Telegram message sent successfully to chat: {}", chatId);
            } else {
                log.error("Failed to send Telegram message. Status: {}", response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Error sending Telegram message to chat: {}", chatId, e);
        }
    }

    @Override
    public void sendMessage(User user, String message) {
        if (user.canReceiveTelegramNotifications()) {
            sendMessage(user.getTelegramChatId(), message);
        } else {
            log.debug("User {} cannot receive Telegram notifications", user.getEmail());
        }
    }

    @Override
    public void sendWelcomeMessage(User user) {
        if (isUserRegistered(user)) {
            String message = String.format(
                "🎉 <b>Welcome to E-Menu Platform!</b>\n\n" +
                "Hello %s,\n\n" +
                "Your account has been successfully verified. " +
                "You can now enjoy all the features of our platform.\n\n" +
                "Need help? Contact our support team anytime!",
                user.getFirstName()
            );
            sendMessage(user, message);
        }
    }

    @Override
    public void sendOrderNotification(User user, String orderDetails) {
        if (isUserRegistered(user)) {
            String message = String.format(
                "🍽️ <b>Order Update</b>\n\n" +
                "Hello %s,\n\n" +
                "%s\n\n" +
                "Thank you for using E-Menu Platform!",
                user.getFirstName(),
                orderDetails
            );
            sendMessage(user, message);
        }
    }

    @Override
    public boolean isUserRegistered(User user) {
        return user.getTelegramChatId() != null && !user.getTelegramChatId().trim().isEmpty();
    }
}