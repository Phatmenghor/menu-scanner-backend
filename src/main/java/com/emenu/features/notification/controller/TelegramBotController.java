package com.emenu.features.notification.controller;

import com.emenu.features.notification.dto.update.TelegramUpdate;
import com.emenu.features.notification.service.TelegramBotService;
import com.emenu.shared.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/telegram/bot")
@RequiredArgsConstructor
@Slf4j
public class TelegramBotController {

    private final TelegramBotService telegramBotService;

    /**
     * Telegram Webhook Endpoint - PUBLIC ACCESS
     */
    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(@RequestBody TelegramUpdate update) {
        log.debug("📱 Received Telegram update: {}", update.getUpdateId());
        
        try {
            telegramBotService.processUpdate(update);
            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            log.error("❌ Error processing Telegram update {}: {}", update.getUpdateId(), e.getMessage(), e);
            return ResponseEntity.ok("OK"); // Always return OK to Telegram
        }
    }

    /**
     * Set webhook URL - PUBLIC ACCESS FOR EASY SETUP
     */
    @PostMapping("/set-webhook")
    public ResponseEntity<ApiResponse<String>> setWebhook(@RequestParam String url) {
        log.info("🔧 Setting Telegram webhook to: {}", url);
        
        try {
            boolean success = telegramBotService.setWebhook(url);
            if (success) {
                return ResponseEntity.ok(ApiResponse.success("Webhook set successfully", url + "/api/v1/telegram/bot/webhook"));
            } else {
                return ResponseEntity.badRequest().body(ApiResponse.error("Failed to set webhook"));
            }
        } catch (Exception e) {
            log.error("❌ Error setting webhook: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error("Error setting webhook: " + e.getMessage()));
        }
    }

    /**
     * Get webhook info - PUBLIC ACCESS
     */
    @GetMapping("/webhook-info")
    public ResponseEntity<ApiResponse<Object>> getWebhookInfo() {
        try {
            Object webhookInfo = telegramBotService.getWebhookInfo();
            return ResponseEntity.ok(ApiResponse.success("Webhook info retrieved", webhookInfo));
        } catch (Exception e) {
            log.error("❌ Error getting webhook info: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error("Error getting webhook info: " + e.getMessage()));
        }
    }

    /**
     * Send test message - PUBLIC ACCESS
     */
    @PostMapping("/send-test-message")
    public ResponseEntity<ApiResponse<String>> sendTestMessage(
            @RequestParam String chatId, 
            @RequestParam String message) {
        try {
            telegramBotService.sendMessage(chatId, "🧪 <b>Test Message</b>\n\n" + message + "\n\n<i>Sent from API</i>");
            return ResponseEntity.ok(ApiResponse.success("Test message sent", chatId));
        } catch (Exception e) {
            log.error("❌ Error sending test message: {}", e.getMessage());
            return ResponseEntity.badRequest().body(ApiResponse.error("Error sending message: " + e.getMessage()));
        }
    }
}