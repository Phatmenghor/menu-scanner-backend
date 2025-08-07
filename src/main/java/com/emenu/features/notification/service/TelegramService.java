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
        
        String message = template
                .replace("{productName}", productName != null ? productName : "Unknown Product")
                .replace("{businessName}", businessName != null ? businessName : "Unknown Business")
                .replace("{price}", price != null ? price : "0.00")
                .replace("{categoryName}", categoryName != null ? categoryName : "Uncategorized")
                .replace("{createdBy}", createdBy != null ? createdBy : "Unknown User")
                .replace("{createdAt}", createdAt != null ? createdAt : "Now")
                .replace("{productUrl}", generateProductUrl(productName));
        
        return sendMessage(message, "Product Created");
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
        
        String message = String.format("""
                👤 *New User Registered!*
                
                📧 *Email:* %s
                👤 *Name:* %s
                🏷️ *Type:* %s
                📅 *Date:* %s
                """, 
                email != null ? email : "No email",
                fullName != null ? fullName : "Unknown User",
                userType != null ? userType : "Unknown Type",
                registeredAt != null ? registeredAt : "Now");
        
        return sendMessage(message, "User Registered");
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
        
        String message = String.format("""
                🏪 *New Business Registered!*
                
                🏢 *Business:* %s
                👤 *Owner:* %s
                📧 *Email:* %s
                📞 *Phone:* %s
                🌐 *Subdomain:* %s
                📅 *Date:* %s
                """,
                businessName != null ? businessName : "Unknown Business",
                ownerName != null ? ownerName : "Unknown Owner",
                businessEmail != null ? businessEmail : "No email",
                phoneNumber != null ? phoneNumber : "N/A",
                subdomain != null ? subdomain : "N/A",
                registeredAt != null ? registeredAt : "Now");
        
        return sendMessage(message, "Business Registered");
    }
    
    /**
     * Send message - with MOCK MODE support
     */
    @Async
    public CompletableFuture<Boolean> sendMessage(String message, String logContext) {
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
            String url = TELEGRAM_API_BASE_URL + telegramConfig.getBot().getToken() + "/sendMessage";
            
            TelegramMessageRequest request = TelegramMessageRequest.builder()
                    .chatId("1898032377")
                    .text(message)
                    .parseMode("Markdown")
                    .disableWebPagePreview(true)
                    .build();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<TelegramMessageRequest> entity = new HttpEntity<>(request, headers);
            
            ResponseEntity<TelegramMessageResponse> response = restTemplate.postForEntity(url, entity, TelegramMessageResponse.class);
            
            if (response.getBody() != null && response.getBody().getOk()) {
                log.info("✅ Real Telegram message sent successfully: {}", logContext);
                return CompletableFuture.completedFuture(true);
            } else {
                log.warn("❌ Telegram API returned error: {}", 
                        response.getBody() != null ? response.getBody().getDescription() : "Unknown error");
            }
            
        } catch (Exception e) {
            log.error("💥 Failed to send real Telegram message: {}", e.getMessage());
        }
        
        return CompletableFuture.completedFuture(false);
    }
    
    /**
     * Test connection - with MOCK MODE support
     */
    public boolean testConnection() {
        if (mockMode) {
            log.info("🧪 MOCK TELEGRAM CONNECTION TEST");
            log.info("════════════════════════════════════════");
            log.info("✅ Mock connection test PASSED!");
            log.info("🔧 Bot Token: {}...", telegramConfig.getBot().getToken().substring(0, 20));
            log.info("👤 Chat ID: {}", telegramConfig.getBot().getChatId());
            log.info("🌐 Mode: MOCK (Real API disabled)");
            log.info("💡 Tip: Set telegram.bot.mock=false to use real API");
            log.info("════════════════════════════════════════");
            return true;
        }
        
        try {
            log.info("🔄 Testing real Telegram API connection...");
            String url = TELEGRAM_API_BASE_URL + telegramConfig.getBot().getToken() + "/getMe";
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("✅ Real Telegram connection successful!");
                return true;
            } else {
                log.error("❌ Real Telegram connection failed with status: {}", response.getStatusCode());
                return false;
            }
        } catch (Exception e) {
            log.error("❌ Real Telegram connection test failed: {}", e.getMessage());
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
                🆕 *New Product Created!*
                
                📱 *Product:* {productName}
                🏪 *Business:* {businessName}
                💰 *Price:* ${price}
                📂 *Category:* {categoryName}
                👤 *Created by:* {createdBy}
                📅 *Date:* {createdAt}
                
                🔗 View: {productUrl}
                """;
    }
}