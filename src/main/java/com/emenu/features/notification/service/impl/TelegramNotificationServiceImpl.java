package com.emenu.features.notification.service.impl;

import com.emenu.config.TelegramConfig;
import com.emenu.enums.user.RoleEnum;
import com.emenu.features.auth.models.User;
import com.emenu.features.auth.repository.UserRepository;
import com.emenu.features.notification.constants.TelegramMessages;
import com.emenu.features.notification.dto.response.*;
import com.emenu.features.notification.mapper.TelegramNotificationMapper;
import com.emenu.features.notification.service.TelegramNotificationService;
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
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelegramNotificationServiceImpl implements TelegramNotificationService {

    private final TelegramConfig telegramConfig;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate;
    private final TelegramNotificationMapper notificationMapper;

    private static final String TELEGRAM_API_URL = "https://api.telegram.org/bot";

    // ===== PLATFORM-WIDE NOTIFICATIONS (TO GROUP) =====

    @Override
    @Async
    public CompletableFuture<Void> sendPlatformUserCreationNotification(User newUser, User createdBy) {
        if (!isNotificationEnabled("systemEvents")) {
            return CompletableFuture.completedFuture(null);
        }

        log.info("📱 Processing platform user creation notification for: {}", newUser.getUserIdentifier());

        try {
            // ✅ NEW: Use DTO and mapper pattern like other notifications
            PlatformUserCreationNotificationDto dto = notificationMapper.toPlatformUserCreationDto(newUser, createdBy);
            String message = notificationMapper.buildPlatformUserCreationMessage(dto);

            sendGroupMessage(message);
            log.info("📩 Platform user creation notification sent to group");

            log.info("✅ Platform user creation notification completed for: {}", newUser.getUserIdentifier());

        } catch (Exception e) {
            log.error("❌ Failed to send platform user creation notification for: {}", newUser.getUserIdentifier(), e);
        }

        return CompletableFuture.completedFuture(null);
    }

    public String buildPlatformUserCreationMessage(PlatformUserCreationNotificationDto dto) {
        return TelegramMessages.buildPlatformUserCreationMessage(dto);
    }

    @Override
    @Async
    public CompletableFuture<Void> sendCustomerRegistrationNotification(User customer) {
        if (!isNotificationEnabled("customerRegistration")) {
            return CompletableFuture.completedFuture(null);
        }

        log.info("📱 Processing customer registration notification for: {}", customer.getUserIdentifier());

        try {
            CustomerRegistrationNotificationDto dto = notificationMapper.toCustomerRegistrationDto(customer);

            // Send welcome message to customer if they have Telegram
            if (dto.getTelegramUserId() != null && isNotificationEnabled("welcomeMessages")) {
                String welcomeMessage = notificationMapper.buildCustomerWelcomeMessage(dto);
                sendTelegramMessage(dto.getTelegramUserId().toString(), welcomeMessage);
                log.info("📩 Welcome message sent to customer: {}", customer.getUserIdentifier());
            }

            // Send to group
            String groupMessage = notificationMapper.buildCustomerRegistrationGroupMessage(dto);
            sendGroupMessage(groupMessage);
            log.info("📩 Customer registration notification sent to group");

            log.info("✅ Customer registration notifications completed for: {}", customer.getUserIdentifier());

        } catch (Exception e) {
            log.error("❌ Failed to send customer registration notifications for: {}", customer.getUserIdentifier(), e);
        }

        return CompletableFuture.completedFuture(null);
    }

    @Override
    @Async
    public CompletableFuture<Void> sendBusinessRegistrationNotification(User businessOwner, String businessName, String subdomain) {
        if (!isNotificationEnabled("businessRegistration")) {
            return CompletableFuture.completedFuture(null);
        }

        log.info("📱 Processing business registration notification for: {}", businessName);

        try {
            BusinessRegistrationNotificationDto dto = notificationMapper.toBusinessRegistrationDto(businessOwner, businessName, subdomain);
            String message = notificationMapper.buildBusinessRegistrationMessage(dto);

            // Send to business owner if they have Telegram
            if (dto.getOwnerTelegramUserId() != null) {
                sendTelegramMessage(dto.getOwnerTelegramUserId().toString(), message);
                log.info("📩 Business registration notification sent to owner: {}", businessOwner.getUserIdentifier());
            }

            // Send to group
            sendGroupMessage(message);
            log.info("📩 Business registration notification sent to group");

            log.info("✅ Business registration notifications completed for: {}", businessName);

        } catch (Exception e) {
            log.error("❌ Failed to send business registration notifications for: {}", businessName, e);
        }

        return CompletableFuture.completedFuture(null);
    }

    @Override
    @Async
    public CompletableFuture<Void> sendTelegramLinkNotification(User user) {
        if (!isNotificationEnabled("telegramLinking")) {
            return CompletableFuture.completedFuture(null);
        }

        log.info("📱 Processing Telegram link notification for: {}", user.getUserIdentifier());

        try {
            TelegramLinkNotificationDto dto = notificationMapper.toTelegramLinkDto(user);

            // Send success message to user if account updates are enabled
            if (isNotificationEnabled("accountUpdates")) {
                String successMessage = notificationMapper.buildTelegramLinkSuccessMessage(dto);
                sendTelegramMessage(dto.getTelegramUserId().toString(), successMessage);
                log.info("📩 Telegram link success message sent to: {}", user.getUserIdentifier());
            }

            // Send to group
            String groupMessage = notificationMapper.buildTelegramLinkGroupMessage(dto);
            sendGroupMessage(groupMessage);
            log.info("📩 Telegram link notification sent to group");

            log.info("✅ Telegram link notifications completed for: {}", user.getUserIdentifier());

        } catch (Exception e) {
            log.error("❌ Failed to send Telegram link notifications for: {}", user.getUserIdentifier(), e);
        }

        return CompletableFuture.completedFuture(null);
    }

    @Override
    @Async
    public CompletableFuture<Void> sendSystemEventNotification(SystemEventDto systemEvent) {
        if (!isNotificationEnabled("systemEvents")) {
            return CompletableFuture.completedFuture(null);
        }

        log.info("📱 Processing system event notification: {}", systemEvent.getTitle());

        try {
            String message = notificationMapper.buildSystemEventMessage(systemEvent);
            sendGroupMessage(message);
            log.info("✅ System event notification sent to group: {}", systemEvent.getTitle());

        } catch (Exception e) {
            log.error("❌ Failed to send system event notification: {}", systemEvent.getTitle(), e);
        }

        return CompletableFuture.completedFuture(null);
    }

    // ===== BUSINESS-SPECIFIC NOTIFICATIONS =====

    @Override
    @Async
    public CompletableFuture<Void> sendOrderNotificationToBusiness(OrderNotificationDto orderNotification) {
        if (!isNotificationEnabled("orderNotifications")) {
            return CompletableFuture.completedFuture(null);
        }

        log.info("📱 Processing order notification for business: {}", orderNotification.getBusinessId());

        try {
            List<User> businessUsers = getBusinessManagementUsers(orderNotification.getBusinessId());
            String message = notificationMapper.buildOrderNotificationMessage(orderNotification);

            for (User user : businessUsers) {
                if (user.canReceiveTelegramNotifications()) {
                    sendTelegramMessage(user.getTelegramUserId().toString(), message);
                    log.debug("📨 Order notification sent to: {}", user.getUserIdentifier());
                }
            }

            log.info("✅ Order notifications sent to {} business users", businessUsers.size());

        } catch (Exception e) {
            log.error("❌ Failed to send order notifications for business: {}", orderNotification.getBusinessId(), e);
        }

        return CompletableFuture.completedFuture(null);
    }

    @Override
    @Async
    public CompletableFuture<Void> sendSubscriptionAlertToBusiness(SubscriptionAlertDto subscriptionAlert) {
        if (!isNotificationEnabled("subscriptionAlerts")) {
            return CompletableFuture.completedFuture(null);
        }

        log.info("📱 Processing subscription alert for business: {}", subscriptionAlert.getBusinessId());

        try {
            List<User> businessOwners = getBusinessOwners(subscriptionAlert.getBusinessId());
            String message = notificationMapper.buildSubscriptionAlertMessage(subscriptionAlert);

            for (User owner : businessOwners) {
                if (owner.canReceiveTelegramNotifications()) {
                    sendTelegramMessage(owner.getTelegramUserId().toString(), message);
                    log.debug("📨 Subscription alert sent to: {}", owner.getUserIdentifier());
                }
            }

            log.info("✅ Subscription alerts sent to {} business owners", businessOwners.size());

        } catch (Exception e) {
            log.error("❌ Failed to send subscription alerts for business: {}", subscriptionAlert.getBusinessId(), e);
        }

        return CompletableFuture.completedFuture(null);
    }

    @Override
    @Async
    public CompletableFuture<Void> sendOrderStatusUpdateToCustomer(OrderNotificationDto orderUpdate) {
        log.info("📱 Processing order status update for customer");

        try {
            String message = notificationMapper.buildOrderStatusUpdateMessage(orderUpdate);
            // Note: Customer telegram ID should be included in the DTO for this to work
            // This would need to be modified based on how you want to identify the customer
            
            log.info("✅ Order status update notification prepared");

        } catch (Exception e) {
            log.error("❌ Failed to send order status update", e);
        }

        return CompletableFuture.completedFuture(null);
    }

    // ===== UTILITY METHODS =====

    @Override
    public boolean isTelegramEnabled() {
        return telegramConfig.getBot().isEnabled();
    }

    @Override
    public void sendCustomMessage(Long telegramUserId, String message) {
        if (isTelegramEnabled()) {
            sendTelegramMessage(telegramUserId.toString(), message);
        }
    }

    @Override
    public void sendGroupMessage(String message) {
        if (!telegramConfig.getBot().hasGroupChat()) {
            log.warn("⚠️ Group chat ID not configured, cannot send group notification");
            return;
        }
        
        sendTelegramMessage(telegramConfig.getBot().getGroupChatId(), message);
        log.debug("📨 Message sent to group: {}", telegramConfig.getBot().getGroupChatId());
    }

    // ===== PRIVATE HELPER METHODS =====

    private boolean isNotificationEnabled(String notificationType) {
        if (!isTelegramEnabled()) {
            log.debug("Telegram is disabled");
            return false;
        }

        TelegramConfig.Notifications notifications = telegramConfig.getBot().getNotifications();
        return switch (notificationType) {
            case "customerRegistration" -> notifications.isCustomerRegistration();
            case "businessRegistration" -> notifications.isBusinessRegistration();
            case "telegramLinking" -> notifications.isTelegramLinking();
            case "systemEvents" -> notifications.isSystemEvents();
            case "orderNotifications" -> notifications.isOrderNotifications();
            case "subscriptionAlerts" -> notifications.isSubscriptionAlerts();
            case "paymentUpdates" -> notifications.isPaymentUpdates();
            case "welcomeMessages" -> notifications.isWelcomeMessages();
            case "accountUpdates" -> notifications.isAccountUpdates();
            default -> false;
        };
    }

    private List<User> getBusinessManagementUsers(UUID businessId) {
        return userRepository.findBusinessUsersWithTelegram(businessId)
                .stream()
                .filter(this::hasBusinessManagementRole)
                .toList();
    }

    private List<User> getBusinessOwners(UUID businessId) {
        return userRepository.findBusinessUsersWithTelegram(businessId)
                .stream()
                .filter(this::hasBusinessOwnerRole)
                .toList();
    }

    private boolean hasBusinessOwnerRole(User user) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getName() == RoleEnum.BUSINESS_OWNER);
    }

    private boolean hasBusinessManagementRole(User user) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getName() == RoleEnum.BUSINESS_OWNER || 
                                role.getName() == RoleEnum.BUSINESS_MANAGER);
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
}