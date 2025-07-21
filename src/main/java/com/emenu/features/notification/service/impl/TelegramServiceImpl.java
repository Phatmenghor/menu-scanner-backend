package com.emenu.features.notification.service.impl;

import com.emenu.enums.notification.TemplateName;
import com.emenu.features.notification.service.MessageTemplateService;
import com.emenu.features.notification.service.TelegramService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelegramServiceImpl implements TelegramService {

    private final RestTemplate restTemplate;
    private final MessageTemplateService templateService;

    @Value("${app.notifications.telegram.bot-token:}")
    private String botToken;

    @Value("${app.notifications.telegram.enabled:false}")
    private boolean telegramEnabled;

    private static final String TELEGRAM_API_BASE = "https://api.telegram.org/bot";

    @Override
    public void sendTelegramMessage(String chatId, String message) {
        sendTelegramMessage(chatId, message, null);
    }

    @Override
    public void sendTelegramMessage(String chatId, String message, Map<String, Object> options) {
        if (!isTelegramConfigured()) {
            log.warn("Telegram is not configured. Message not sent to chat: {}", chatId);
            return;
        }

        try {
            String url = TELEGRAM_API_BASE + botToken + "/sendMessage";
            
            Map<String, Object> payload = new HashMap<>();
            payload.put("chat_id", chatId);
            payload.put("text", message);
            payload.put("parse_mode", "HTML");
            
            if (options != null) {
                payload.putAll(options);
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                log.info("Telegram message sent successfully to chat: {}", chatId);
            } else {
                log.error("Failed to send Telegram message. Status: {}, Response: {}", 
                         response.getStatusCode(), response.getBody());
            }
            
        } catch (Exception e) {
            log.error("Error sending Telegram message to chat: {}", chatId, e);
            throw new RuntimeException("Failed to send Telegram message", e);
        }
    }

    @Override
    public void sendTemplatedTelegramMessage(String chatId, String templateName, Map<String, String> variables) {
        try {
            TemplateName template = TemplateName.valueOf(templateName.toUpperCase());
            String processedContent = templateService.processTemplate(template, variables);
            
            sendTelegramMessage(chatId, processedContent);
            log.info("Templated Telegram message sent to chat: {}", chatId);
        } catch (Exception e) {
            log.error("Failed to send templated Telegram message to chat: {}", chatId, e);
            throw new RuntimeException("Failed to send templated Telegram message", e);
        }
    }

    @Override
    public void sendSubscriptionAlert(String chatId, String businessName, String alertType) {
        String message = String.format(
            "🚨 <b>Subscription Alert</b>\n\n" +
            "Business: <b>%s</b>\n" +
            "Alert: <b>%s</b>\n\n" +
            "Please check your subscription status and take necessary action.",
            businessName, alertType
        );
        
        sendTelegramMessage(chatId, message);
    }

    @Override
    public void sendPaymentReminder(String chatId, String businessName, String paymentDetails) {
        String message = String.format(
            "💰 <b>Payment Reminder</b>\n\n" +
            "Business: <b>%s</b>\n" +
            "Details: %s\n\n" +
            "Please process your payment to continue using our services.",
            businessName, paymentDetails
        );
        
        sendTelegramMessage(chatId, message);
    }

    @Override
    public void sendSystemAlert(String chatId, String alertMessage) {
        String message = String.format(
            "⚠️ <b>System Alert</b>\n\n%s", alertMessage
        );
        
        sendTelegramMessage(chatId, message);
    }

    @Override
    public boolean isTelegramConfigured() {
        return telegramEnabled && botToken != null && !botToken.trim().isEmpty();
    }

    @Override
    public String getBotInfo() {
        if (!isTelegramConfigured()) {
            return "Telegram bot is not configured";
        }

        try {
            String url = TELEGRAM_API_BASE + botToken + "/getMe";
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            } else {
                return "Failed to get bot info";
            }
        } catch (Exception e) {
            log.error("Error getting bot info", e);
            return "Error: " + e.getMessage();
        }
    }

    @Override
    public void setWebhook(String webhookUrl) {
        if (!isTelegramConfigured()) {
            log.warn("Telegram is not configured. Cannot set webhook.");
            return;
        }

        try {
            String url = TELEGRAM_API_BASE + botToken + "/setWebhook";
            
            Map<String, String> payload = new HashMap<>();
            payload.put("url", webhookUrl);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, String>> request = new HttpEntity<>(payload, headers);
            
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                log.info("Telegram webhook set successfully: {}", webhookUrl);
            } else {
                log.error("Failed to set Telegram webhook: {}", response.getBody());
            }
            
        } catch (Exception e) {
            log.error("Error setting Telegram webhook", e);
        }
    }
}