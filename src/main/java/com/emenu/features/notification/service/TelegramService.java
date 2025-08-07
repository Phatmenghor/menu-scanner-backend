package com.emenu.features.notification.service;

import com.emenu.config.TelegramConfig;
import com.emenu.features.notification.dto.request.TelegramMessageRequest;
import com.emenu.features.notification.dto.response.TelegramMessageResponse;
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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelegramService {
    
    private final TelegramConfig telegramConfig;
    private final RestTemplate restTemplate;
    
    // 🧪 Mock mode configuration
    @Value("${telegram.bot.mock:false}")
    private boolean mockMode;
    
    private static final String TELEGRAM_API_BASE_URL = "https://api.telegram.org/bot";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    
    /**
     * Send product created notification
     */
    @Async
    public CompletableFuture<Boolean> sendProductCreatedNotification(
            String productName, String businessName, String price, 
            String categoryName, String createdBy, String createdAt) {
        
        if (!telegramConfig.getNotifications().isProductCreated()) {
            log.debug("Product created notifications are disabled");
            return CompletableFuture.completedFuture(false);
        }
        
        String template = telegramConfig.getTemplates().getProductCreated();
        if (template == null || template.trim().isEmpty()) {
            template = getDefaultProductTemplate();
        }
        
        // ✅ FIX: Escape HTML characters and use HTML format
        String message = template
                .replace("{productName}", escapeHtml(productName != null ? productName : "Unknown Product"))
                .replace("{businessName}", escapeHtml(businessName != null ? businessName : "Unknown Business"))
                .replace("{price}", escapeHtml(price != null ? price : "0.00"))
                .replace("{categoryName}", escapeHtml(categoryName != null ? categoryName : "Uncategorized"))
                .replace("{createdBy}", escapeHtml(createdBy != null ? createdBy : "Unknown User"))
                .replace("{createdAt}", escapeHtml(createdAt != null ? createdAt : "Now"))
                .replace("{productUrl}", generateProductUrl(productName));
        
        return sendMessage(message, "Product Created", "HTML");
    }
    
    /**
     * Send user registered notification
     */
    @Async
    public CompletableFuture<Boolean> sendUserRegisteredNotification(
            String email, String fullName, String userType, String registeredAt) {
        
        if (!telegramConfig.getNotifications().isUserRegistered()) {
            log.debug("User registered notifications are disabled");
            return CompletableFuture.completedFuture(false);
        }
        
        // ✅ FIX: Use HTML format and escape special characters
        String message = String.format("""
                👤 <b>New User Registered!</b>
                
                📧 <b>Email:</b> %s
                👤 <b>Name:</b> %s
                🏷️ <b>Type:</b> %s
                📅 <b>Date:</b> %s
                """, 
                escapeHtml(email != null ? email : "No email"),
                escapeHtml(fullName != null ? fullName : "Unknown User"),
                escapeHtml(userType != null ? userType : "Unknown Type"),
                escapeHtml(registeredAt != null ? registeredAt : "Now"));
        
        return sendMessage(message, "User Registered", "HTML");
    }
    
    /**
     * Send business registered notification
     */
    @Async
    public CompletableFuture<Boolean> sendBusinessRegisteredNotification(
            String businessName, String ownerName, String businessEmail,
            String phoneNumber, String subdomain, String registeredAt) {
        
        if (!telegramConfig.getNotifications().isBusinessRegistered()) {
            log.debug("Business registered notifications are disabled");
            return CompletableFuture.completedFuture(false);
        }
        
        // ✅ FIX: Use HTML format and escape special characters
        String message = String.format("""
                🏪 <b>New Business Registered!</b>
                
                🏢 <b>Business:</b> %s
                👤 <b>Owner:</b> %s
                📧 <b>Email:</b> %s
                📞 <b>Phone:</b> %s
                🌐 <b>Subdomain:</b> %s
                📅 <b>Date:</b> %s
                """,
                escapeHtml(businessName != null ? businessName : "Unknown Business"),
                escapeHtml(ownerName != null ? ownerName : "Unknown Owner"),
                escapeHtml(businessEmail != null ? businessEmail : "No email"),
                escapeHtml(phoneNumber != null ? phoneNumber : "N/A"),
                escapeHtml(subdomain != null ? subdomain : "N/A"),
                escapeHtml(registeredAt != null ? registeredAt : "Now"));
        
        return sendMessage(message, "Business Registered", "HTML");
    }
    
    /**
     * Send message - with MOCK MODE support and FIXED chat ID usage
     */
    @Async
    public CompletableFuture<Boolean> sendMessage(String message, String logContext) {
        return sendMessage(message, logContext, "HTML");  // Default to HTML for better reliability
    }
    
    /**
     * Send message with specific parse mode - with MOCK MODE support and FIXED chat ID usage
     */
    @Async
    public CompletableFuture<Boolean> sendMessage(String message, String logContext, String parseMode) {
        if (!telegramConfig.getBot().isEnabled()) {
            log.debug("Telegram bot is disabled");
            return CompletableFuture.completedFuture(false);
        }
        
        // 🧪 MOCK MODE - Perfect for development when network is blocked
        if (mockMode) {
            log.info("📱 MOCK TELEGRAM NOTIFICATION ({})", logContext);
            log.info("╔══════════════════════════════════════════════════════════════════════════════╗");
            log.info("║ 📱 TELEGRAM MESSAGE SIMULATION                                                  ║");
            log.info("╠══════════════════════════════════════════════════════════════════════════════╣");
            log.info("║ 👤 TO: {} (PHAT_MENGHOR)                                        ║", telegramConfig.getBot().getChatId());
            log.info("║ ⏰ TIME: {}                                            ║", LocalDateTime.now().format(FORMATTER));
            log.info("║ 🏷️  CONTEXT: {}                                                    ║", String.format("%-60s", logContext));
            log.info("║ 📝 PARSE MODE: {}                                                      ║", String.format("%-60s", parseMode));
            log.info("╠══════════════════════════════════════════════════════════════════════════════╣");
            
            // Format and display the message content
            String[] lines = message.split("\\n");
            for (String line : lines) {
                if (line.trim().isEmpty()) {
                    log.info("║                                                                              ║");
                } else {
                    log.info("║ {}║", String.format("%-76s", line));
                }
            }
            
            log.info("╠══════════════════════════════════════════════════════════════════════════════╣");
            log.info("║ ✅ STATUS: Mock message logged successfully!                                   ║");
            log.info("║ 🌐 NOTE: Real Telegram API is disabled (mock mode enabled)                   ║");
            log.info("╚══════════════════════════════════════════════════════════════════════════════╝");
            
            return CompletableFuture.completedFuture(true);
        }
        
        // Real Telegram API (only runs when mock=false)
        try {
            log.info("🔄 Attempting real Telegram API call...");
            
            // ✅ FIX: Validate configuration before making API call
            String botToken = telegramConfig.getBot().getToken();
            String chatId = telegramConfig.getBot().getChatId();
            
            if (botToken == null || botToken.trim().isEmpty()) {
                log.error("❌ Telegram bot token is not configured");
                return CompletableFuture.completedFuture(false);
            }
            
            if (chatId == null || chatId.trim().isEmpty()) {
                log.error("❌ Telegram chat ID is not configured");
                return CompletableFuture.completedFuture(false);
            }
            
            String url = TELEGRAM_API_BASE_URL + botToken + "/sendMessage";
            log.debug("📡 Telegram API URL: {}", url);
            log.debug("💬 Chat ID: {}", chatId);
            log.debug("📝 Message length: {} characters", message.length());
            log.debug("🎨 Parse mode: {}", parseMode);
            
            TelegramMessageRequest request = TelegramMessageRequest.builder()
                    .chatId(chatId)  // ✅ FIX: Use configured chat ID instead of hardcoded
                    .text(message)
                    .parseMode(parseMode)  // ✅ FIX: Use specified parse mode
                    .disableWebPagePreview(true)
                    .build();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<TelegramMessageRequest> entity = new HttpEntity<>(request, headers);
            
            log.debug("🚀 Sending request to Telegram API...");
            ResponseEntity<TelegramMessageResponse> response = restTemplate.postForEntity(url, entity, TelegramMessageResponse.class);
            
            if (response.getBody() != null && response.getBody().getOk()) {
                log.info("✅ Real Telegram message sent successfully: {}", logContext);
                log.debug("📨 Message ID: {}", response.getBody().getResult() != null ? 
                    response.getBody().getResult().getMessageId() : "unknown");
                return CompletableFuture.completedFuture(true);
            } else {
                String errorMsg = response.getBody() != null ? response.getBody().getDescription() : "Unknown error";
                log.warn("❌ Telegram API returned error: {}", errorMsg);
                
                // ✅ ADD: More detailed error logging
                if (response.getBody() != null) {
                    log.warn("🔍 Error code: {}", response.getBody().getErrorCode());
                    log.warn("🔍 Response body: {}", response.getBody());
                }
                
                // ✅ FIX: Retry with plain text if parsing fails
                if (errorMsg != null && errorMsg.contains("can't parse entities") && !parseMode.equals("None")) {
                    log.info("🔄 Retrying with plain text (no formatting)...");
                    return sendMessagePlainText(message, logContext);
                }
            }
            
        } catch (Exception e) {
            log.error("💥 Failed to send real Telegram message: {}", e.getMessage());
            log.error("🔍 Full error details: ", e);
            
            // ✅ ADD: Specific error handling for common issues
            if (e.getMessage() != null) {
                if (e.getMessage().contains("chat not found")) {
                    log.error("🔍 SOLUTION: Make sure the chat ID is correct and the bot has been added to the chat");
                } else if (e.getMessage().contains("bot was kicked")) {
                    log.error("🔍 SOLUTION: Add the bot back to the chat");
                } else if (e.getMessage().contains("bad request")) {
                    log.error("🔍 SOLUTION: Check the message format and chat ID");
                } else if (e.getMessage().contains("can't parse entities")) {
                    log.info("🔄 Retrying with plain text (no formatting)...");
                    return sendMessagePlainText(message, logContext);
                }
            }
        }
        
        return CompletableFuture.completedFuture(false);
    }
    
    /**
     * Send message as plain text (fallback when formatting fails)
     */
    @Async
    public CompletableFuture<Boolean> sendMessagePlainText(String message, String logContext) {
        try {
            String botToken = telegramConfig.getBot().getToken();
            String chatId = telegramConfig.getBot().getChatId();
            String url = TELEGRAM_API_BASE_URL + botToken + "/sendMessage";
            
            // Strip HTML/Markdown formatting for plain text
            String plainMessage = stripFormatting(message);
            
            TelegramMessageRequest request = TelegramMessageRequest.builder()
                    .chatId(chatId)
                    .text(plainMessage)
                    .parseMode("None")  // No formatting
                    .disableWebPagePreview(true)
                    .build();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<TelegramMessageRequest> entity = new HttpEntity<>(request, headers);
            ResponseEntity<TelegramMessageResponse> response = restTemplate.postForEntity(url, entity, TelegramMessageResponse.class);
            
            if (response.getBody() != null && response.getBody().getOk()) {
                log.info("✅ Telegram message sent as plain text: {}", logContext);
                return CompletableFuture.completedFuture(true);
            }
        } catch (Exception e) {
            log.error("❌ Failed to send plain text message: {}", e.getMessage());
        }
        
        return CompletableFuture.completedFuture(false);
    }
    
    /**
     * Test connection - with MOCK MODE support and IMPROVED validation
     */
    public boolean testConnection() {
        if (mockMode) {
            log.info("🧪 MOCK TELEGRAM CONNECTION TEST");
            log.info("════════════════════════════════════════");
            log.info("✅ Mock connection test PASSED!");
            log.info("🔧 Bot Token: {}...", telegramConfig.getBot().getToken() != null ? 
                telegramConfig.getBot().getToken().substring(0, Math.min(20, telegramConfig.getBot().getToken().length())) : "NOT_SET");
            log.info("👤 Chat ID: {}", telegramConfig.getBot().getChatId());
            log.info("🌐 Mode: MOCK (Real API disabled)");
            log.info("💡 Tip: Set telegram.bot.mock=false to use real API");
            log.info("════════════════════════════════════════");
            return true;
        }
        
        try {
            log.info("🔄 Testing real Telegram API connection...");
            
            // ✅ FIX: Validate configuration before testing
            String botToken = telegramConfig.getBot().getToken();
            if (botToken == null || botToken.trim().isEmpty()) {
                log.error("❌ Bot token is not configured");
                return false;
            }
            
            String url = TELEGRAM_API_BASE_URL + botToken + "/getMe";
            log.debug("📡 Testing with URL: {}", url);
            
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("✅ Real Telegram connection successful!");
                log.debug("🤖 Bot info: {}", response.getBody());
                return true;
            } else {
                log.error("❌ Real Telegram connection failed with status: {}", response.getStatusCode());
                return false;
            }
        } catch (Exception e) {
            log.error("❌ Real Telegram connection test failed: {}", e.getMessage());
            log.error("🔍 Full error: ", e);
            log.info("💡 Consider enabling mock mode: telegram.bot.mock=true");
            return false;
        }
    }
    
    // Helper methods
    private String generateProductUrl(String productName) {
        if (productName == null) {
            return "https://your-domain.com/products/";
        }
        return "https://your-domain.com/products/" + productName.toLowerCase().replaceAll("\\s+", "-");
    }
    
    private String getDefaultProductTemplate() {
        return """
                🆕 <b>New Product Created!</b>
                
                📱 <b>Product:</b> {productName}
                🏪 <b>Business:</b> {businessName}
                💰 <b>Price:</b> ${price}
                📂 <b>Category:</b> {categoryName}
                👤 <b>Created by:</b> {createdBy}
                📅 <b>Date:</b> {createdAt}
                
                🔗 View: {productUrl}
                """;
    }
    
    /**
     * Escape HTML special characters to prevent parsing errors
     */
    private String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#x27;");
    }
    
    /**
     * Strip formatting for plain text fallback
     */
    private String stripFormatting(String text) {
        if (text == null) {
            return "";
        }
        return text.replaceAll("<[^>]+>", "")  // Remove HTML tags
                   .replaceAll("\\*([^*]+)\\*", "$1")  // Remove Markdown bold
                   .replaceAll("_([^_]+)_", "$1")      // Remove Markdown italic
                   .replaceAll("`([^`]+)`", "$1")      // Remove Markdown code
                   .replaceAll("\\[([^\\]]+)\\]\\([^)]+\\)", "$1"); // Remove Markdown links
    }
}