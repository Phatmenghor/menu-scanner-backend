package com.emenu.features.notification.service;

import com.emenu.config.TelegramConfig;
import com.emenu.features.auth.models.User;
import com.emenu.features.auth.repository.UserRepository;
import com.emenu.features.notification.dto.request.MultiRecipientNotificationRequest;
import com.emenu.features.notification.dto.request.TelegramMessageRequest;
import com.emenu.features.notification.dto.response.NotificationSendResult;
import com.emenu.features.notification.dto.response.TelegramMessageResponse;
import com.emenu.features.notification.repository.TelegramUserSessionRepository;
import com.emenu.shared.constants.NotificationConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelegramService {
    
    private final TelegramConfig telegramConfig;
    private final RestTemplate restTemplate;
    private final UserRepository userRepository;
    private final TelegramUserSessionRepository sessionRepository;
    
    private static final String TELEGRAM_API_BASE_URL = "https://api.telegram.org/bot";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    // ===== SINGLE RECIPIENT NOTIFICATIONS =====
    
    @Async
    public CompletableFuture<Boolean> sendProductCreatedNotification(
            String productName, String businessName, String price, 
            String categoryName, String createdBy, String createdAt) {
        
        if (!telegramConfig.getNotifications().isProductCreated()) {
            log.debug("Product created notifications are disabled");
            return CompletableFuture.completedFuture(false);
        }
        
        String message = buildProductCreatedMessage(productName, businessName, price, categoryName, createdBy, createdAt);
        return sendToAllNotificationEnabledUsers(message, "Product Created");
    }
    
    @Async
    public CompletableFuture<Boolean> sendUserRegisteredNotification(
            String userIdentifier, String fullName, String userType, String registeredAt) {
        
        if (!telegramConfig.getNotifications().isUserRegistered()) {
            log.debug("User registered notifications are disabled");
            return CompletableFuture.completedFuture(false);
        }
        
        String message = buildUserRegisteredMessage(userIdentifier, fullName, userType, registeredAt);
        return sendToAllNotificationEnabledUsers(message, "User Registered");
    }
    
    @Async
    public CompletableFuture<Boolean> sendBusinessRegisteredNotification(
            String businessName, String ownerName, String businessEmail,
            String phoneNumber, String subdomain, String registeredAt) {
        
        if (!telegramConfig.getNotifications().isBusinessRegistered()) {
            log.debug("Business registered notifications are disabled");
            return CompletableFuture.completedFuture(false);
        }
        
        String message = buildBusinessRegisteredMessage(businessName, ownerName, businessEmail, phoneNumber, subdomain, registeredAt);
        return sendToAllNotificationEnabledUsers(message, "Business Registered");
    }

    // ===== MULTI-RECIPIENT NOTIFICATIONS =====
    
    @Async
    public CompletableFuture<NotificationSendResult> sendMultiRecipientNotification(MultiRecipientNotificationRequest request) {
        log.info("📢 Sending multi-recipient notification: {}", request.getNotificationType());
        
        NotificationSendResult.NotificationSendResultBuilder resultBuilder = NotificationSendResult.builder()
                .notificationType(request.getNotificationType())
                .sentAt(LocalDateTime.now())
                .results(new ArrayList<>());
        
        try {
            // Get recipients
            List<User> recipients = getNotificationRecipients(request);
            resultBuilder.totalRecipients(recipients.size());
            
            if (recipients.isEmpty()) {
                log.warn("⚠️ No recipients found for notification: {}", request.getNotificationType());
                return CompletableFuture.completedFuture(resultBuilder
                        .successfulSends(0)
                        .failedSends(0)
                        .allSuccessful(true)
                        .summary("No recipients found")
                        .build());
            }

            // Build message
            String message = buildNotificationMessage(request);
            
            // Send to each recipient
            int successCount = 0;
            int failCount = 0;
            List<NotificationSendResult.RecipientResult> recipientResults = new ArrayList<>();
            
            for (User recipient : recipients) {
                try {
                    if (recipient.canReceiveTelegramNotifications()) {
                        boolean sent = sendDirectMessageToUser(recipient.getTelegramUserId().toString(), message, request.getTitle()).get();
                        
                        recipientResults.add(NotificationSendResult.RecipientResult.builder()
                                .userId(recipient.getId())
                                .telegramUserId(recipient.getTelegramUserId())
                                .recipientName(recipient.getDisplayName())
                                .channel("TELEGRAM")
                                .success(sent)
                                .sentAt(LocalDateTime.now())
                                .error(sent ? null : "Failed to send Telegram message")
                                .build());
                        
                        if (sent) successCount++;
                        else failCount++;
                    } else {
                        log.debug("User {} cannot receive Telegram notifications", recipient.getUserIdentifier());
                        failCount++;
                        
                        recipientResults.add(NotificationSendResult.RecipientResult.builder()
                                .userId(recipient.getId())
                                .telegramUserId(recipient.getTelegramUserId())
                                .recipientName(recipient.getDisplayName())
                                .channel("TELEGRAM")
                                .success(false)
                                .error("User cannot receive Telegram notifications")
                                .build());
                    }
                } catch (Exception e) {
                    log.error("Failed to send notification to user {}: {}", recipient.getUserIdentifier(), e.getMessage());
                    failCount++;
                    
                    recipientResults.add(NotificationSendResult.RecipientResult.builder()
                            .userId(recipient.getId())
                            .telegramUserId(recipient.getTelegramUserId())
                            .recipientName(recipient.getDisplayName())
                            .channel("TELEGRAM")
                            .success(false)
                            .error(e.getMessage())
                            .build());
                }
            }
            
            boolean allSuccessful = failCount == 0;
            String summary = String.format("Sent to %d/%d recipients successfully", successCount, recipients.size());
            
            log.info("✅ Multi-recipient notification completed: {} - {}", request.getNotificationType(), summary);
            
            return CompletableFuture.completedFuture(resultBuilder
                    .successfulSends(successCount)
                    .failedSends(failCount)
                    .telegramSent(successCount)
                    .telegramFailed(failCount)
                    .results(recipientResults)
                    .allSuccessful(allSuccessful)
                    .summary(summary)
                    .build());
            
        } catch (Exception e) {
            log.error("❌ Failed to send multi-recipient notification: {}", e.getMessage(), e);
            
            return CompletableFuture.completedFuture(resultBuilder
                    .successfulSends(0)
                    .failedSends(1)
                    .allSuccessful(false)
                    .summary("Failed to send notification: " + e.getMessage())
                    .errors(List.of(e.getMessage()))
                    .build());
        }
    }

    // ===== DIRECT MESSAGING =====
    
    @Async
    public CompletableFuture<Boolean> sendDirectMessageToUser(String chatId, String message, String context) {
        return sendMessage(chatId, message, context, "HTML");
    }
    
    @Async
    public CompletableFuture<Boolean> sendWelcomeMessage(Long telegramUserId, String userName) {
        String welcomeMessage = NotificationConstants.WELCOME_TELEGRAM_USER
                .replace("{userName}", userName != null ? userName : "User");
        
        return sendMessage(telegramUserId.toString(), welcomeMessage, "Welcome Message", "HTML");
    }
    
    @Async
    public CompletableFuture<Boolean> sendRegistrationSuccessMessage(Long telegramUserId, String userName) {
        String message = NotificationConstants.CUSTOMER_REGISTRATION_SUCCESS
                .replace("{userName}", userName != null ? userName : "User");
        
        return sendMessage(telegramUserId.toString(), message, "Registration Success", "HTML");
    }

    // ===== CORE MESSAGE SENDING =====
    
    @Async
    public CompletableFuture<Boolean> sendMessage(String chatId, String message, String context, String parseMode) {
        if (!telegramConfig.getBot().isEnabled()) {
            log.debug("Telegram bot is disabled");
            return CompletableFuture.completedFuture(false);
        }
        
        try {
            String botToken = telegramConfig.getBot().getToken();
            if (botToken == null || botToken.trim().isEmpty()) {
                log.error("❌ Telegram bot token is not configured");
                return CompletableFuture.completedFuture(false);
            }
            
            String url = TELEGRAM_API_BASE_URL + botToken + "/sendMessage";
            
            TelegramMessageRequest request = TelegramMessageRequest.builder()
                    .chatId(chatId)
                    .text(message)
                    .parseMode(parseMode)
                    .disableWebPagePreview(true)
                    .build();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<TelegramMessageRequest> entity = new HttpEntity<>(request, headers);
            ResponseEntity<TelegramMessageResponse> response = restTemplate.postForEntity(url, entity, TelegramMessageResponse.class);
            
            if (response.getBody() != null && response.getBody().getOk()) {
                log.debug("✅ Telegram message sent successfully: {}", context);
                return CompletableFuture.completedFuture(true);
            } else {
                String errorMsg = response.getBody() != null ? response.getBody().getDescription() : "Unknown error";
                log.warn("❌ Telegram API returned error: {}", errorMsg);
                
                // Retry with plain text if parsing fails
                if (errorMsg != null && errorMsg.contains("can't parse entities") && !parseMode.equals("None")) {
                    log.info("🔄 Retrying with plain text...");
                    return sendMessage(chatId, stripFormatting(message), context, "None");
                }
            }
            
        } catch (Exception e) {
            log.error("💥 Failed to send Telegram message: {}", e.getMessage());
        }
        
        return CompletableFuture.completedFuture(false);
    }
    
    @Async
    public CompletableFuture<Boolean> sendMessage(String chatId, String message, String context) {
        return sendMessage(chatId, message, context, "HTML");
    }

    // ===== CONNECTION TESTING =====
    
    public boolean testConnection() {
        try {
            log.info("🔄 Testing Telegram API connection...");
            
            String botToken = telegramConfig.getBot().getToken();
            if (botToken == null || botToken.trim().isEmpty()) {
                log.error("❌ Bot token is not configured");
                return false;
            }
            
            String url = TELEGRAM_API_BASE_URL + botToken + "/getMe";
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("✅ Telegram connection successful!");
                return true;
            } else {
                log.error("❌ Telegram connection failed with status: {}", response.getStatusCode());
                return false;
            }
        } catch (Exception e) {
            log.error("❌ Telegram connection test failed: {}", e.getMessage());
            return false;
        }
    }

    // ===== HELPER METHODS =====
    
    private List<User> getNotificationRecipients(MultiRecipientNotificationRequest request) {
        List<User> recipients = new ArrayList<>();
        
        // Get by user types
        if (request.getRecipientUserTypes() != null && !request.getRecipientUserTypes().isEmpty()) {
            recipients.addAll(userRepository.findUsersWithTelegramByUserTypes(request.getRecipientUserTypes()));
        }
        
        // Get specific users
        if (request.getSpecificUserIds() != null && !request.getSpecificUserIds().isEmpty()) {
            List<User> specificUsers = userRepository.findAllById(request.getSpecificUserIds())
                    .stream()
                    .filter(User::canReceiveTelegramNotifications)
                    .toList();
            recipients.addAll(specificUsers);
        }
        
        // Get by flags
        if (Boolean.TRUE.equals(request.getIncludePlatformUsers())) {
            recipients.addAll(userRepository.findPlatformAdmins());
        }
        
        if (Boolean.TRUE.equals(request.getIncludeBusinessOwners())) {
            recipients.addAll(userRepository.findBusinessOwners());
        }
        
        if (Boolean.TRUE.equals(request.getIncludeCustomers())) {
            recipients.addAll(userRepository.findAllCustomers()
                    .stream()
                    .filter(User::canReceiveTelegramNotifications)
                    .toList());
        }
        
        // Remove duplicates
        return recipients.stream().distinct().toList();
    }
    
    private CompletableFuture<Boolean> sendToAllNotificationEnabledUsers(String message, String context) {
        try {
            List<User> users = userRepository.findAllUsersWithTelegramNotifications();
            
            if (users.isEmpty()) {
                log.info("📭 No users with Telegram notifications enabled for: {}", context);
                return CompletableFuture.completedFuture(true);
            }
            
            log.info("📢 Sending {} to {} users with Telegram notifications", context, users.size());
            
            int successCount = 0;
            for (User user : users) {
                try {
                    boolean sent = sendMessage(user.getTelegramUserId().toString(), message, context).get();
                    if (sent) successCount++;
                } catch (Exception e) {
                    log.error("Failed to send notification to user {}: {}", user.getUserIdentifier(), e.getMessage());
                }
            }
            
            log.info("✅ {} notification sent to {}/{} users", context, successCount, users.size());
            return CompletableFuture.completedFuture(successCount > 0);
            
        } catch (Exception e) {
            log.error("❌ Failed to send {} notification: {}", context, e.getMessage(), e);
            return CompletableFuture.completedFuture(false);
        }
    }
    
    private String buildNotificationMessage(MultiRecipientNotificationRequest request) {
        if (request.getMessage() != null) {
            return processTemplate(request.getMessage(), request.getTemplateData());
        }
        
        // Default message format
        return String.format("""
                📢 <b>%s</b>
                
                %s
                
                📅 <b>Time:</b> %s
                🤖 <b>From:</b> Cambodia E-Menu Platform
                """,
                escapeHtml(request.getTitle()),
                escapeHtml("Notification from Cambodia E-Menu Platform"),
                LocalDateTime.now().format(FORMATTER));
    }
    
    private String buildProductCreatedMessage(String productName, String businessName, String price, String categoryName, String createdBy, String createdAt) {
        return String.format("""
                🆕 <b>New Product Created!</b>
                
                📱 <b>Product:</b> %s
                🏪 <b>Business:</b> %s
                💰 <b>Price:</b> $%s
                📂 <b>Category:</b> %s
                👤 <b>Created by:</b> %s
                📅 <b>Date:</b> %s
                """,
                escapeHtml(productName != null ? productName : "Unknown Product"),
                escapeHtml(businessName != null ? businessName : "Unknown Business"),
                escapeHtml(price != null ? price : "0.00"),
                escapeHtml(categoryName != null ? categoryName : "Uncategorized"),
                escapeHtml(createdBy != null ? createdBy : "Unknown User"),
                escapeHtml(createdAt != null ? createdAt : "Now"));
    }
    
    private String buildUserRegisteredMessage(String userIdentifier, String fullName, String userType, String registeredAt) {
        return String.format("""
                👤 <b>New User Registered!</b>
                
                🆔 <b>User ID:</b> %s
                👤 <b>Name:</b> %s
                🏷️ <b>Type:</b> %s
                📅 <b>Date:</b> %s
                """,
                escapeHtml(userIdentifier != null ? userIdentifier : "Unknown"),
                escapeHtml(fullName != null ? fullName : "Unknown User"),
                escapeHtml(userType != null ? userType : "Unknown Type"),
                escapeHtml(registeredAt != null ? registeredAt : "Now"));
    }
    
    private String buildBusinessRegisteredMessage(String businessName, String ownerName, String businessEmail, String phoneNumber, String subdomain, String registeredAt) {
        return String.format("""
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
    }
    
    private String processTemplate(String template, Map<String, Object> data) {
        if (data == null || data.isEmpty()) {
            return template;
        }
        
        String result = template;
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            String placeholder = "{" + entry.getKey() + "}";
            String value = entry.getValue() != null ? entry.getValue().toString() : "";
            result = result.replace(placeholder, escapeHtml(value));
        }
        
        return result;
    }
    
    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#x27;");
    }
    
    private String stripFormatting(String text) {
        if (text == null) return "";
        return text.replaceAll("<[^>]+>", "")
                   .replaceAll("\\*([^*]+)\\*", "$1")
                   .replaceAll("_([^_]+)_", "$1")
                   .replaceAll("`([^`]+)`", "$1")
                   .replaceAll("\\[([^\\]]+)\\]\\([^)]+\\)", "$1");
    }
}