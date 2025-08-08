package com.emenu.features.notification.service;

import com.emenu.enums.user.UserType;
import com.emenu.features.auth.models.User;
import com.emenu.features.auth.repository.UserRepository;
import com.emenu.features.notification.dto.request.TelegramBotCallbackRequest;
import com.emenu.features.notification.dto.response.TelegramBotResponse;
import com.emenu.features.notification.models.TelegramUserSession;
import com.emenu.features.notification.repository.TelegramUserSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TelegramBotService {

    private final TelegramService telegramService;
    private final TelegramUserSessionRepository sessionRepository;
    private final UserRepository userRepository;

    // ===== WEBHOOK MESSAGE PROCESSING =====

    public TelegramBotResponse processUpdate(Map<String, Object> update) {
        log.debug("🤖 Processing Telegram update: {}", update);

        try {
            // Extract message data
            Map<String, Object> message = (Map<String, Object>) update.get("message");
            if (message == null) {
                // Try callback query
                Map<String, Object> callbackQuery = (Map<String, Object>) update.get("callback_query");
                if (callbackQuery != null) {
                    return processCallbackQuery(callbackQuery);
                }
                return TelegramBotResponse.builder()
                        .success(false)
                        .message("No message or callback query found in update")
                        .timestamp(LocalDateTime.now())
                        .build();
            }

            // Extract user and chat info
            Map<String, Object> from = (Map<String, Object>) message.get("from");
            Map<String, Object> chat = (Map<String, Object>) message.get("chat");
            String text = (String) message.get("text");

            if (from == null || chat == null) {
                return TelegramBotResponse.builder()
                        .success(false)
                        .message("Missing user or chat information")
                        .timestamp(LocalDateTime.now())
                        .build();
            }

            Long telegramUserId = ((Number) from.get("id")).longValue();
            String chatId = chat.get("id").toString();
            String telegramUsername = (String) from.get("username");
            String firstName = (String) from.get("first_name");
            String lastName = (String) from.get("last_name");

            // Find or create session
            TelegramUserSession session = findOrCreateSession(
                    telegramUserId, chatId, telegramUsername, firstName, lastName);

            // Process the message
            return processMessage(session, text, chatId);

        } catch (Exception e) {
            log.error("❌ Error processing Telegram update: {}", e.getMessage(), e);
            return TelegramBotResponse.builder()
                    .success(false)
                    .message("Error processing update: " + e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build();
        }
    }

    // ===== MESSAGE PROCESSING =====

    private TelegramBotResponse processMessage(TelegramUserSession session, String text, String chatId) {
        log.info("💬 Processing message from {}: {}", session.getDisplayName(), text);

        if (text == null || text.trim().isEmpty()) {
            return sendResponse(chatId, "Please send a text message.", "Empty Message");
        }

        String command = text.trim().toLowerCase();

        // Handle commands
        if (command.startsWith("/")) {
            return processCommand(session, command, chatId);
        }

        // Handle conversation state
        if (session.getCurrentState() != null) {
            return processConversationState(session, text, chatId);
        }

        // Default response for non-command messages
        return sendHelpMessage(chatId, session);
    }

    // ===== COMMAND PROCESSING =====

    private TelegramBotResponse processCommand(TelegramUserSession session, String command, String chatId) {
        log.debug("⚡ Processing command: {} from user: {}", command, session.getDisplayName());

        return switch (command) {
            case "/start" -> processStartCommand(session, chatId);
            case "/help" -> sendHelpMessage(chatId, session);
            case "/register" -> processRegisterCommand(session, chatId);
            case "/login" -> processLoginCommand(session, chatId);
            case "/profile" -> processProfileCommand(session, chatId);
            case "/businesses" -> processBusinessesCommand(session, chatId);
            case "/notifications" -> processNotificationsCommand(session, chatId);
            case "/cancel" -> processCancelCommand(session, chatId);
            default -> sendUnknownCommandMessage(chatId, command);
        };
    }

    private TelegramBotResponse processStartCommand(TelegramUserSession session, String chatId) {
        String welcomeMessage = String.format("""
                🇰🇭 <b>Welcome to Cambodia E-Menu Platform!</b> 🇰🇭
                
                👋 Hello %s!
                
                🎉 <b>What is Cambodia E-Menu Platform?</b>
                • 🏪 Digital menu platform for restaurants
                • 📱 Easy online ordering system
                • 💰 Affordable SaaS solution for Cambodia
                
                ✨ <b>What you can do:</b>
                • 📋 Browse restaurant menus
                • 🛒 Place orders online
                • 🔔 Get notifications
                • 🏢 Manage your business (if owner)
                
                🚀 <b>Get started:</b>
                /register - Create your account
                /login - Access existing account
                /help - See all commands
                
                📞 <b>Need help?</b> Contact support!
                """, session.getDisplayName());

        return sendResponse(chatId, welcomeMessage, "Start Command");
    }

    private TelegramBotResponse processRegisterCommand(TelegramUserSession session, String chatId) {
        if (session.isLinkedToUser()) {
            return sendResponse(chatId, 
                    "✅ You're already registered! Use /profile to view your account details.", 
                    "Already Registered");
        }

        String registerMessage = String.format("""
                📝 <b>Account Registration</b>
                
                Hi %s! Let's create your Cambodia E-Menu Platform account.
                
                🤔 <b>What type of account do you need?</b>
                
                👤 <b>Customer Account:</b>
                • Browse restaurant menus
                • Place orders online
                • Get order notifications
                
                🏪 <b>Business Account:</b>
                • Create digital menus
                • Manage your restaurant
                • Process customer orders
                • Subscription required
                
                📊 <b>Platform Account:</b>
                • Platform administration
                • Manage businesses
                • System oversight
                
                ⚡ <b>Quick Registration:</b>
                Reply with your choice:
                • Type "customer" for Customer Account
                • Type "business" for Business Account
                • Type "platform" for Platform Account
                
                Or visit our website for full registration.
                """, session.getDisplayName());

        session.setState("awaiting_account_type", null);
        sessionRepository.save(session);

        return sendResponse(chatId, registerMessage, "Register Command");
    }

    private TelegramBotResponse processLoginCommand(TelegramUserSession session, String chatId) {
        if (!session.isLinkedToUser()) {
            return sendResponse(chatId, 
                    "❌ No account linked to this Telegram. Please /register first or visit our website to link your existing account.", 
                    "No Account");
        }

        Optional<User> userOpt = userRepository.findById(session.getUserId());
        if (userOpt.isEmpty()) {
            return sendResponse(chatId, 
                    "❌ Account not found. Please contact support.", 
                    "User Not Found");
        }

        User user = userOpt.get();
        String loginMessage = String.format("""
                🔐 <b>Login Information</b>
                
                ✅ <b>Account Found:</b>
                👤 <b>Name:</b> %s
                🆔 <b>User ID:</b> %s
                🏷️ <b>Type:</b> %s
                📧 <b>Email:</b> %s
                
                🎯 <b>To login on website:</b>
                1. Visit our login page
                2. Use Telegram login option
                3. Or use your username/password
                
                🔔 <b>Notifications:</b> %s
                
                ⚙️ Use /profile for more details
                """, 
                user.getDisplayName(),
                user.getUserIdentifier(),
                user.getUserType().getDescription(),
                user.getEmail() != null ? user.getEmail() : "Not set",
                user.canReceiveTelegramNotifications() ? "Enabled ✅" : "Disabled ❌");

        return sendResponse(chatId, loginMessage, "Login Info");
    }

    private TelegramBotResponse processProfileCommand(TelegramUserSession session, String chatId) {
        if (!session.isLinkedToUser()) {
            return sendResponse(chatId, 
                    "❌ No account linked. Use /register to create an account.", 
                    "No Profile");
        }

        Optional<User> userOpt = userRepository.findById(session.getUserId());
        if (userOpt.isEmpty()) {
            return sendResponse(chatId, "❌ Profile not found. Please contact support.", "Profile Error");
        }

        User user = userOpt.get();
        String profileMessage = String.format("""
                👤 <b>Your Profile</b>
                
                🆔 <b>User ID:</b> %s
                👤 <b>Name:</b> %s
                📧 <b>Email:</b> %s
                📞 <b>Phone:</b> %s
                🏷️ <b>Type:</b> %s
                ✅ <b>Status:</b> %s
                
                🔗 <b>Telegram Linked:</b> %s
                🔔 <b>Notifications:</b> %s
                📅 <b>Linked Date:</b> %s
                
                🏢 <b>Business:</b> %s
                
                ⚙️ <b>Available Commands:</b>
                /notifications - Manage notifications
                %s
                """,
                user.getUserIdentifier(),
                user.getDisplayName(),
                user.getEmail() != null ? user.getEmail() : "Not set",
                user.getPhoneNumber() != null ? user.getPhoneNumber() : "Not set",
                user.getUserType().getDescription(),
                user.getAccountStatus().getDescription(),
                user.hasTelegramLinked() ? "Yes ✅" : "No ❌",
                user.canReceiveTelegramNotifications() ? "Enabled ✅" : "Disabled ❌",
                user.getTelegramLinkedAt() != null ? 
                    user.getTelegramLinkedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "Unknown",
                user.getBusiness() != null ? user.getBusiness().getName() : "None",
                user.isBusinessUser() ? "/businesses - Manage business" : "");

        return sendResponse(chatId, profileMessage, "Profile Info");
    }

    private TelegramBotResponse processBusinessesCommand(TelegramUserSession session, String chatId) {
        if (!session.isLinkedToUser()) {
            return sendResponse(chatId, "❌ Please /register first.", "No Account");
        }

        Optional<User> userOpt = userRepository.findById(session.getUserId());
        if (userOpt.isEmpty() || !userOpt.get().isBusinessUser()) {
            return sendResponse(chatId, 
                    "❌ This command is only available for business users.", 
                    "Not Business User");
        }

        User user = userOpt.get();
        String businessMessage = String.format("""
                🏪 <b>Business Information</b>
                
                %s
                
                📊 <b>Quick Actions:</b>
                • Visit our web platform to manage your business
                • Create and update your digital menu
                • View customer orders
                • Manage staff accounts
                
                💡 <b>Tip:</b> Use the web platform for full business management features.
                """,
                user.getBusiness() != null ? 
                    String.format("🏢 <b>Business:</b> %s\n✅ <b>Status:</b> Active\n🔗 <b>Subdomain:</b> Available", 
                            user.getBusiness().getName()) :
                    "❌ No business associated with your account."
        );

        return sendResponse(chatId, businessMessage, "Business Info");
    }

    private TelegramBotResponse processNotificationsCommand(TelegramUserSession session, String chatId) {
        if (!session.isLinkedToUser()) {
            return sendResponse(chatId, "❌ Please /register first.", "No Account");
        }

        Optional<User> userOpt = userRepository.findById(session.getUserId());
        if (userOpt.isEmpty()) {
            return sendResponse(chatId, "❌ Account not found.", "User Not Found");
        }

        User user = userOpt.get();
        String notificationMessage = String.format("""
                🔔 <b>Notification Settings</b>
                
                📊 <b>Current Status:</b> %s
                
                📝 <b>You receive notifications for:</b>
                • New user registrations
                • Business registrations
                • Product updates
                • System announcements
                
                ⚙️ <b>To change settings:</b>
                Reply with:
                • "enable" to enable notifications
                • "disable" to disable notifications
                
                💡 <b>Note:</b> You can also manage notifications on our web platform.
                """,
                user.canReceiveTelegramNotifications() ? "Enabled ✅" : "Disabled ❌");

        session.setState("awaiting_notification_setting", null);
        sessionRepository.save(session);

        return sendResponse(chatId, notificationMessage, "Notifications");
    }

    private TelegramBotResponse processCancelCommand(TelegramUserSession session, String chatId) {
        session.clearState();
        sessionRepository.save(session);

        return sendResponse(chatId, "✅ Operation cancelled. Use /help to see available commands.", "Cancelled");
    }

    private TelegramBotResponse sendUnknownCommandMessage(String chatId, String command) {
        String message = String.format("""
                ❓ Unknown command: %s
                
                📋 <b>Available commands:</b>
                /start - Welcome message
                /help - Show this help
                /register - Create account
                /login - Login information  
                /profile - View your profile
                /businesses - Business management
                /notifications - Notification settings
                /cancel - Cancel current operation
                
                💡 Type /help for detailed information.
                """, command);

        return sendResponse(chatId, message, "Unknown Command");
    }

    // ===== CONVERSATION STATE PROCESSING =====

    private TelegramBotResponse processConversationState(TelegramUserSession session, String text, String chatId) {
        String state = session.getCurrentState();
        log.debug("🔄 Processing state: {} with input: {}", state, text);

        return switch (state) {
            case "awaiting_account_type" -> processAccountTypeSelection(session, text, chatId);
            case "awaiting_notification_setting" -> processNotificationSetting(session, text, chatId);
            default -> {
                session.clearState();
                sessionRepository.save(session);
                yield sendResponse(chatId, "Session expired. Please try again.", "State Expired");
            }
        };
    }

    private TelegramBotResponse processAccountTypeSelection(TelegramUserSession session, String text, String chatId) {
        String choice = text.toLowerCase().trim();
        UserType userType;

        switch (choice) {
            case "customer" -> userType = UserType.CUSTOMER;
            case "business" -> userType = UserType.BUSINESS_USER;
            case "platform" -> userType = UserType.PLATFORM_USER;
            default -> {
                return sendResponse(chatId, 
                        "❌ Invalid choice. Please reply with 'customer', 'business', or 'platform'.", 
                        "Invalid Choice");
            }
        }

        session.clearState();
        sessionRepository.save(session);

        String responseMessage = String.format("""
                ✅ <b>Account Type Selected: %s</b>
                
                🎯 <b>Next Steps:</b>
                1. Visit our website: cambodia-emenu.com
                2. Complete registration with your details
                3. Link this Telegram account
                
                Or contact our support team for assistance.
                
                📞 <b>Support:</b> support@cambodia-emenu.com
                """, userType.getDescription());

        return sendResponse(chatId, responseMessage, "Account Type Selected");
    }

    private TelegramBotResponse processNotificationSetting(TelegramUserSession session, String text, String chatId) {
        String choice = text.toLowerCase().trim();

        if (!choice.equals("enable") && !choice.equals("disable")) {
            return sendResponse(chatId, 
                    "❌ Please reply with 'enable' or 'disable'.", 
                    "Invalid Notification Choice");
        }

        Optional<User> userOpt = userRepository.findById(session.getUserId());
        if (userOpt.isEmpty()) {
            return sendResponse(chatId, "❌ Account not found.", "User Not Found");
        }

        User user = userOpt.get();
        boolean enableNotifications = choice.equals("enable");
        
        user.setTelegramNotificationsEnabled(enableNotifications);
        userRepository.save(user);

        session.clearState();
        sessionRepository.save(session);

        String message = String.format("""
                ✅ <b>Notification Settings Updated</b>
                
                🔔 <b>Status:</b> %s
                
                %s
                """,
                enableNotifications ? "Enabled" : "Disabled",
                enableNotifications ? 
                    "You will now receive notifications via Telegram." :
                    "You will no longer receive notifications via Telegram.");

        return sendResponse(chatId, message, "Notification Setting Updated");
    }

    // ===== CALLBACK PROCESSING =====

    public TelegramBotResponse processCallback(TelegramBotCallbackRequest request) {
        log.info("📞 Processing callback from user: {} with data: {}", 
                request.getTelegramUserId(), request.getData());

        // Handle different callback data
        String data = request.getData();
        String chatId = request.getChatId();

        return switch (data) {
            case "register_customer" -> processCallbackRegister(request, UserType.CUSTOMER);
            case "register_business" -> processCallbackRegister(request, UserType.BUSINESS_USER);
            case "enable_notifications" -> processCallbackNotifications(request, true);
            case "disable_notifications" -> processCallbackNotifications(request, false);
            default -> TelegramBotResponse.builder()
                    .success(true)
                    .responseType("CALLBACK_ANSWER")
                    .callbackText("Unknown action")
                    .showAlert(false)
                    .timestamp(LocalDateTime.now())
                    .build();
        };
    }

    private TelegramBotResponse processCallbackRegister(TelegramBotCallbackRequest request, UserType userType) {
        String responseText = String.format("Registration for %s account initiated. Please visit our website to complete.", 
                userType.getDescription());

        return TelegramBotResponse.builder()
                .success(true)
                .responseType("CALLBACK_ANSWER")
                .callbackText(responseText)
                .showAlert(false)
                .timestamp(LocalDateTime.now())
                .build();
    }

    private TelegramBotResponse processCallbackNotifications(TelegramBotCallbackRequest request, boolean enable) {
        String responseText = String.format("Notifications %s", enable ? "enabled" : "disabled");

        return TelegramBotResponse.builder()
                .success(true)
                .responseType("CALLBACK_ANSWER")
                .callbackText(responseText)
                .showAlert(false)
                .timestamp(LocalDateTime.now())
                .build();
    }

    private TelegramBotResponse processCallbackQuery(Map<String, Object> callbackQuery) {
        try {
            Map<String, Object> from = (Map<String, Object>) callbackQuery.get("from");
            String data = (String) callbackQuery.get("data");
            String callbackQueryId = callbackQuery.get("id").toString();

            TelegramBotCallbackRequest request = new TelegramBotCallbackRequest();
            request.setCallbackQueryId(Long.parseLong(callbackQueryId));
            request.setTelegramUserId(((Number) from.get("id")).longValue());
            request.setData(data);

            return processCallback(request);
        } catch (Exception e) {
            log.error("❌ Error processing callback query: {}", e.getMessage(), e);
            return TelegramBotResponse.builder()
                    .success(false)
                    .message("Error processing callback: " + e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build();
        }
    }

    // ===== HELPER METHODS =====

    private TelegramBotResponse sendHelpMessage(String chatId, TelegramUserSession session) {
        String helpMessage = String.format("""
                🆘 <b>Cambodia E-Menu Platform Help</b>
                
                👋 Hi %s! Here's how to use this bot:
                
                🚀 <b>Getting Started:</b>
                /start - Welcome & introduction
                /register - Create new account
                /login - Access existing account
                
                👤 <b>Account Management:</b>
                /profile - View your profile
                /notifications - Manage notifications
                
                🏪 <b>Business Users:</b>
                /businesses - Business information
                
                ⚙️ <b>Other Commands:</b>
                /help - Show this help message
                /cancel - Cancel current operation
                
                💬 <b>Contact Support:</b>
                Email: support@cambodia-emenu.com
                
                🌐 <b>Website:</b> cambodia-emenu.com
                """, session.getDisplayName());

        return sendResponse(chatId, helpMessage, "Help Message");
    }

    private TelegramBotResponse sendResponse(String chatId, String message, String context) {
        try {
            telegramService.sendDirectMessageToUser(chatId, message, context);
            
            return TelegramBotResponse.builder()
                    .success(true)
                    .message("Response sent successfully")
                    .responseType("TEXT")
                    .chatId(chatId)
                    .timestamp(LocalDateTime.now())
                    .build();
        } catch (Exception e) {
            log.error("❌ Error sending response: {}", e.getMessage(), e);
            
            return TelegramBotResponse.builder()
                    .success(false)
                    .message("Failed to send response: " + e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build();
        }
    }

    private TelegramUserSession findOrCreateSession(Long telegramUserId, String chatId, 
                                                   String username, String firstName, String lastName) {
        Optional<TelegramUserSession> sessionOpt = sessionRepository.findByTelegramUserIdAndIsDeletedFalse(telegramUserId);
        
        if (sessionOpt.isPresent()) {
            TelegramUserSession session = sessionOpt.get();
            session.updateActivity();
            
            // Update session info
            if (username != null) session.setTelegramUsername(username);
            if (firstName != null) session.setTelegramFirstName(firstName);
            if (lastName != null) session.setTelegramLastName(lastName);
            if (chatId != null) session.setChatId(chatId);
            
            return sessionRepository.save(session);
        } else {
            TelegramUserSession session = new TelegramUserSession();
            session.setTelegramUserId(telegramUserId);
            session.setChatId(chatId);
            session.setTelegramUsername(username);
            session.setTelegramFirstName(firstName);
            session.setTelegramLastName(lastName);
            session.setFirstInteraction(LocalDateTime.now());
            session.setLastActivity(LocalDateTime.now());
            session.setTotalInteractions(1L);
            
            return sessionRepository.save(session);
        }
    }

    // ===== STATISTICS =====

    public Map<String, Object> getBotStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime last24Hours = now.minusHours(24);
            LocalDateTime last7Days = now.minusDays(7);
            
            // Session statistics
            long totalSessions = sessionRepository.count();
            long registeredSessions = sessionRepository.countRegisteredSessions();
            long unregisteredSessions = sessionRepository.countUnregisteredSessions();
            long activeLast24Hours = sessionRepository.countActiveSince(last24Hours);
            long activeLast7Days = sessionRepository.countActiveSince(last7Days);
            long notificationEnabled = sessionRepository.countWithNotificationsEnabled();
            
            // User statistics
            long totalTelegramUsers = userRepository.countUsersWithTelegram();
            long telegramOnlyUsers = userRepository.countTelegramOnlyUsers();
            long notificationEnabledUsers = userRepository.countTelegramNotificationEnabledUsers();
            
            stats.put("totalSessions", totalSessions);
            stats.put("registeredSessions", registeredSessions);
            stats.put("unregisteredSessions", unregisteredSessions);
            stats.put("activeLast24Hours", activeLast24Hours);
            stats.put("activeLast7Days", activeLast7Days);
            stats.put("notificationEnabledSessions", notificationEnabled);
            
            stats.put("totalTelegramUsers", totalTelegramUsers);
            stats.put("telegramOnlyUsers", telegramOnlyUsers);
            stats.put("notificationEnabledUsers", notificationEnabledUsers);
            
            stats.put("generatedAt", now);
            stats.put("status", "healthy");
            
        } catch (Exception e) {
            log.error("❌ Error generating bot statistics: {}", e.getMessage(), e);
            stats.put("error", "Failed to generate statistics: " + e.getMessage());
            stats.put("status", "error");
        }
        
        return stats;
    }
}