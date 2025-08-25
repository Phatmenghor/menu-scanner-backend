package com.emenu.features.notification.service.impl;

import com.emenu.config.TelegramConfig;
import com.emenu.features.auth.dto.request.TelegramAuthRequest;
import com.emenu.features.auth.dto.request.TelegramLinkRequest;
import com.emenu.features.auth.dto.response.TelegramAuthResponse;
import com.emenu.features.auth.models.User;
import com.emenu.features.auth.repository.RoleRepository;
import com.emenu.features.auth.repository.UserRepository;
import com.emenu.features.auth.service.impl.TelegramAuthServiceImpl;
import com.emenu.features.notification.constants.TelegramMessages;
import com.emenu.features.notification.dto.update.TelegramCallbackQuery;
import com.emenu.features.notification.dto.update.TelegramMessage;
import com.emenu.features.notification.dto.update.TelegramUpdate;
import com.emenu.features.notification.dto.update.TelegramUser;
import com.emenu.features.notification.service.TelegramBotService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TelegramBotServiceImpl implements TelegramBotService {

    private final TelegramConfig telegramConfig;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final TelegramAuthServiceImpl telegramAuthService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private static final String TELEGRAM_API_URL = "https://api.telegram.org/bot";
    private final Map<Long, UserRegistrationState> userStates = new HashMap<>();

    @Override
    public void processUpdate(TelegramUpdate update) {
        if (!telegramConfig.getBot().isEnabled()) {
            return;
        }

        try {
            if (update.hasMessage() && update.getMessage().hasText()) {
                processTextMessage(update);
            } else if (update.hasCallbackQuery()) {
                processCallbackQuery(update);
            }
        } catch (Exception e) {
            log.error("❌ Error processing update: {}", e.getMessage(), e);
            String chatId = update.getChatId();
            if (chatId != null) {
                sendMessage(chatId, "❌ An error occurred. Please try again later.");
            }
        }
    }

    private void processTextMessage(TelegramUpdate update) {
        TelegramMessage message = update.getMessage();
        String chatId = message.getChat().getId().toString();
        Long userId = message.getFrom().getId();
        String text = message.getText().trim();

        if (message.isCommand()) {
            processCommand(update, message.getCommand(), message.getCommandArgs());
        } else {
            processUserInput(update, text);
        }
    }

    private void processCommand(TelegramUpdate update, String command, String args) {
        String chatId = update.getChatId();
        Long telegramUserId = update.getUserId();
        TelegramUser telegramUser = update.getMessage().getFrom();

        switch (command.toLowerCase()) {
            case "start" -> handleStartCommand(chatId, telegramUserId, telegramUser, args);
            case "help" -> handleHelpCommand(chatId);
            case "register" -> handleRegisterCommand(chatId, telegramUserId, telegramUser);
            case "login" -> handleLoginCommand(chatId, telegramUserId);
            case "link" -> handleLinkCommand(chatId, telegramUserId, telegramUser, args);
            case "profile" -> handleProfileCommand(chatId, telegramUserId);
            case "cancel" -> handleCancelCommand(chatId, telegramUserId);
            case "status" -> handleStatusCommand(chatId);
            default -> handleUnknownCommand(chatId, command);
        }
    }

    private void handleStartCommand(String chatId, Long telegramUserId, TelegramUser telegramUser, String args) {
        Optional<User> existingUser = userRepository.findByTelegramUserIdAndIsDeletedFalse(telegramUserId);
        
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            String welcomeMessage = String.format("""
                🎉 Welcome back, %s!
                
                👤 Your account: %s (%s)
                🏷️ Type: %s
                📊 Status: %s
                
                🚀 Available commands:
                /profile - View your profile
                /help - See all commands
                
                <i>You're all set to use the platform! 🚀</i>
                """, 
                user.getDisplayName(),
                user.getFullName(), user.getUserIdentifier(),
                user.getUserType().getDescription(),
                user.getAccountStatus().getDescription()
            );
            sendMessage(chatId, welcomeMessage);
        } else {
            sendMessage(chatId, TelegramMessages.START_MESSAGE);
            showRegistrationOptions(chatId);
        }
    }

    private void handleRegisterCommand(String chatId, Long telegramUserId, TelegramUser telegramUser) {
        if (userRepository.existsByTelegramUserIdAndIsDeletedFalse(telegramUserId)) {
            sendMessage(chatId, "✅ You're already registered! Use /profile to view your account.");
            return;
        }
        
        startCustomerRegistration(chatId, telegramUserId, telegramUser);
    }

    private void handleLoginCommand(String chatId, Long telegramUserId) {
        try {
            Optional<User> userOpt = userRepository.findByTelegramUserIdAndIsDeletedFalse(telegramUserId);
            
            if (userOpt.isEmpty()) {
                sendMessage(chatId, """
                    ❌ <b>Account Not Found</b>
                    
                    You don't have an account yet. Please register first:
                    
                    /register - Create new account
                    /link - Link existing account
                    """);
                return;
            }
            
            User user = userOpt.get();
            
            TelegramAuthRequest authRequest = new TelegramAuthRequest();
            authRequest.setTelegramUserId(telegramUserId);
            authRequest.setTelegramUsername(user.getTelegramUsername());
            authRequest.setTelegramFirstName(user.getTelegramFirstName());
            authRequest.setTelegramLastName(user.getTelegramLastName());
            
            TelegramAuthResponse response = telegramAuthService.loginWithTelegram(authRequest);
            
            String successMessage = String.format("""
                ✅ <b>Login Successful!</b>
                
                👤 Welcome back, %s!
                🆔 Account: %s
                🏷️ Type: %s
                
                🔐 <b>Access Token:</b>
                <code>%s</code>
                
                <i>Use this token to access the web platform or API.</i>
                """,
                response.getDisplayName(),
                response.getUserIdentifier(),
                response.getUserType().getDescription(),
                response.getAccessToken()
            );
            
            sendMessage(chatId, successMessage);
            
        } catch (Exception e) {
            sendMessage(chatId, "❌ Login failed: " + e.getMessage());
        }
    }

    private void handleLinkCommand(String chatId, Long telegramUserId, TelegramUser telegramUser, String args) {
        sendMessage(chatId, """
            🔗 <b>Link Existing Account</b>
            
            To link your existing platform account to Telegram:
            
            1️⃣ Please provide your username/email
            2️⃣ Then provide your password
            
            💡 <i>Send your username/email now:</i>
            """);
        
        UserRegistrationState state = new UserRegistrationState(UserRegistrationState.State.AWAITING_LINK_CREDENTIALS);
        state.setTelegramUser(telegramUser);
        userStates.put(telegramUserId, state);
    }

    private void handleProfileCommand(String chatId, Long telegramUserId) {
        Optional<User> userOpt = userRepository.findByTelegramUserIdAndIsDeletedFalse(telegramUserId);
        
        if (userOpt.isEmpty()) {
            sendMessage(chatId, "❌ You need to register first. Use /register to create an account.");
            return;
        }
        
        User user = userOpt.get();
        String profileMessage = String.format("""
            👤 <b>Your Profile</b>
            
            🆔 <b>Username:</b> %s
            📧 <b>Email:</b> %s
            📞 <b>Phone:</b> %s
            🏷️ <b>Type:</b> %s
            📊 <b>Status:</b> %s
            🏢 <b>Business:</b> %s
            📱 <b>Telegram:</b> %s
            📅 <b>Joined:</b> %s
            
            🔧 Use the web platform for full account management.
            """,
            user.getUserIdentifier(),
            user.getEmail() != null ? user.getEmail() : "Not provided",
            user.getPhoneNumber() != null ? user.getPhoneNumber() : "Not provided",
            user.getUserType().getDescription(),
            user.getAccountStatus().getDescription(),
            user.getBusiness() != null ? user.getBusiness().getName() : "None",
            user.getTelegramDisplayName(),
            user.getCreatedAt().toLocalDate()
        );
        
        sendMessage(chatId, profileMessage);
    }

    private void handleCancelCommand(String chatId, Long telegramUserId) {
        userStates.remove(telegramUserId);
        sendMessage(chatId, "❌ Operation cancelled. You can start over anytime!");
    }

    private void handleStatusCommand(String chatId) {
        sendMessage(chatId, """
            🟢 <b>Cambodia E-Menu Platform Status</b>
            
            🤖 Bot: Online
            🌐 Platform: Active
            📱 Telegram Integration: Enabled
            
            📊 <b>Quick Stats:</b>
            • Registration: Available
            • Account Linking: Available
            • Notifications: Active
            
            💡 Use /help for available commands
            """);
    }

    private void handleHelpCommand(String chatId) {
        sendMessage(chatId, TelegramMessages.HELP_MESSAGE);
    }

    private void handleUnknownCommand(String chatId, String command) {
        String message = String.format("""
            ❓ Unknown command: /%s
            
            📋 <b>Available commands:</b>
            /start - Welcome & introduction
            /register - Create new account
            /login - Login to your account
            /link - Link existing account
            /profile - View your profile
            /help - Show all commands
            /cancel - Cancel current operation
            /status - Platform status
            
            💡 Need help? Use /help for detailed information.
            """, command);
        
        sendMessage(chatId, message);
    }

    private void processUserInput(TelegramUpdate update, String text) {
        Long telegramUserId = update.getUserId();
        String chatId = update.getChatId();
        
        UserRegistrationState state = userStates.get(telegramUserId);
        if (state == null) {
            sendMessage(chatId, """
                💡 I didn't understand that. Try one of these commands:
                
                /register - Create new account
                /login - Login to existing account
                /help - See all commands
                """);
            return;
        }
        
        processRegistrationFlow(update, text, state);
    }

    private void processRegistrationFlow(TelegramUpdate update, String text, UserRegistrationState state) {
        Long telegramUserId = update.getUserId();
        String chatId = update.getChatId();
        
        switch (state.getCurrentState()) {
            case AWAITING_EMAIL -> {
                state.setEmail(text.trim());
                state.setCurrentState(UserRegistrationState.State.AWAITING_PHONE);
                sendMessage(chatId, String.format("""
                    📧 Email saved: %s
                    
                    📞 Now please provide your phone number (optional):
                    Or send 'skip' to continue without phone number
                    """, text.trim()));
            }
            
            case AWAITING_PHONE -> {
                if (!"skip".equalsIgnoreCase(text.trim())) {
                    state.setPhoneNumber(text.trim());
                }
                completeCustomerRegistration(chatId, telegramUserId, state);
            }
            
            case AWAITING_LINK_CREDENTIALS -> {
                state.setUserIdentifier(text.trim());
                state.setCurrentState(UserRegistrationState.State.AWAITING_LINK_PASSWORD);
                sendMessage(chatId, String.format("""
                    👤 Username saved: %s
                    
                    🔐 Now please provide your password:
                    """, text.trim()));
            }
            
            case AWAITING_LINK_PASSWORD -> {
                state.setPassword(text.trim());
                completeLinkingProcess(chatId, telegramUserId, state);
            }
        }
    }

    private void startCustomerRegistration(String chatId, Long telegramUserId, TelegramUser telegramUser) {
        String message = String.format("""
            📝 <b>Customer Registration</b>
            
            Welcome %s! Let's create your account.
            
            📧 Please provide your email address:
            (This will be used for login and notifications)
            """, telegramUser.getDisplayName());
        
        sendMessage(chatId, message);
        
        UserRegistrationState state = new UserRegistrationState(UserRegistrationState.State.AWAITING_EMAIL);
        state.setTelegramUser(telegramUser);
        userStates.put(telegramUserId, state);
    }

    private void completeCustomerRegistration(String chatId, Long telegramUserId, UserRegistrationState state) {
        try {
            TelegramUser telegramUser = state.getTelegramUser();
            
            TelegramAuthRequest authRequest = new TelegramAuthRequest();
            authRequest.setTelegramUserId(telegramUserId);
            authRequest.setTelegramUsername(telegramUser.getUsername());
            authRequest.setTelegramFirstName(telegramUser.getFirstName());
            authRequest.setTelegramLastName(telegramUser.getLastName());
            authRequest.setLanguageCode(telegramUser.getLanguageCode());
            authRequest.setIsPremium(telegramUser.getIsPremium());
            authRequest.setEmail(state.getEmail());
            authRequest.setPhoneNumber(state.getPhoneNumber());
            authRequest.setUserIdentifier(generateUserIdentifier(telegramUser));
            
            TelegramAuthResponse response = telegramAuthService.registerCustomerWithTelegram(authRequest);
            
            String successMessage = String.format("""
                🎉 <b>Registration Successful!</b>
                
                ✅ Your account has been created:
                🆔 Username: <code>%s</code>
                📧 Email: %s
                📞 Phone: %s
                
                🔐 <b>Access Token:</b>
                <code>%s</code>
                
                🚀 <b>What's next?</b>
                • Use /profile to view your account
                • Visit our web platform for full features
                • You'll receive notifications here automatically
                
                <i>Welcome to Cambodia E-Menu Platform! 🇰🇭</i>
                """,
                response.getUserIdentifier(),
                response.getEmail() != null ? response.getEmail() : "Not provided",
                state.getPhoneNumber() != null ? state.getPhoneNumber() : "Not provided",
                response.getAccessToken()
            );
            
            sendMessage(chatId, successMessage);
            userStates.remove(telegramUserId);
            
        } catch (Exception e) {
            sendMessage(chatId, "❌ Registration failed: " + e.getMessage() + "\n\nPlease try again or use /cancel to start over.");
        }
    }

    private void completeLinkingProcess(String chatId, Long telegramUserId, UserRegistrationState state) {
        try {
            TelegramUser telegramUser = state.getTelegramUser();
            
            Optional<User> existingUser = userRepository.findByUserIdentifierAndIsDeletedFalse(state.getUserIdentifier());
            
            if (existingUser.isEmpty()) {
                sendMessage(chatId, """
                    ❌ <b>Account Not Found</b>
                    
                    No account found with that username/email.
                    
                    Please check your credentials and try again, or use /register to create a new account.
                    """);
                return;
            }
            
            TelegramLinkRequest linkRequest = new TelegramLinkRequest();
            linkRequest.setTelegramUserId(telegramUserId);
            linkRequest.setTelegramUsername(telegramUser.getUsername());
            linkRequest.setTelegramFirstName(telegramUser.getFirstName());
            linkRequest.setTelegramLastName(telegramUser.getLastName());
            linkRequest.setLanguageCode(telegramUser.getLanguageCode());
            linkRequest.setIsPremium(telegramUser.getIsPremium());
            linkRequest.setChatId(chatId);
            
            User user = existingUser.get();
            telegramAuthService.linkTelegramToUser(user.getId(), linkRequest);
            
            String successMessage = String.format("""
                ✅ <b>Account Linked Successfully!</b>
                
                🔗 Your Telegram account is now linked to:
                👤 Account: %s (%s)
                🏷️ Type: %s
                
                🚀 <b>New features unlocked:</b>
                • Login with /login
                • Receive notifications here
                • Quick access to your account
                
                💡 Use /profile to view your linked account details.
                """,
                user.getFullName(), user.getUserIdentifier(),
                user.getUserType().getDescription()
            );
            
            sendMessage(chatId, successMessage);
            userStates.remove(telegramUserId);
            
        } catch (Exception e) {
            sendMessage(chatId, "❌ Linking failed: " + e.getMessage() + "\n\nPlease verify your credentials and try again.");
        }
    }

    private String generateUserIdentifier(TelegramUser telegramUser) {
        String base;
        
        if (telegramUser.getUsername() != null && !telegramUser.getUsername().trim().isEmpty()) {
            base = telegramUser.getUsername().toLowerCase().replaceAll("[^a-z0-9]", "");
        } else if (telegramUser.getFirstName() != null && !telegramUser.getFirstName().trim().isEmpty()) {
            base = telegramUser.getFirstName().toLowerCase().replaceAll("[^a-z0-9]", "");
        } else {
            base = "telegramuser";
        }
        
        String userIdentifier = base + "_" + ThreadLocalRandom.current().nextInt(1000, 9999);
        
        int attempts = 0;
        while (userRepository.existsByUserIdentifierAndIsDeletedFalse(userIdentifier) && attempts < 10) {
            userIdentifier = base + "_" + ThreadLocalRandom.current().nextInt(10000, 99999);
            attempts++;
        }
        
        return userIdentifier;
    }

    private void processCallbackQuery(TelegramUpdate update) {
        TelegramCallbackQuery callbackQuery = update.getCallbackQuery();
        String chatId = callbackQuery.getMessage().getChat().getId().toString();
        String data = callbackQuery.getData();
        
        switch (data) {
            case "register_customer" -> {
                startCustomerRegistration(chatId, callbackQuery.getFrom().getId(), callbackQuery.getFrom());
            }
            case "link_account" -> {
                handleLinkCommand(chatId, callbackQuery.getFrom().getId(), callbackQuery.getFrom(), null);
            }
            case "cancel" -> {
                handleCancelCommand(chatId, callbackQuery.getFrom().getId());
            }
        }
    }

    private void showRegistrationOptions(String chatId) {
        String message = """
            🚀 <b>Get Started</b>
            
            Choose an option:
            • Register as new customer
            • Link existing account
            
            💡 What would you like to do?
            """;
        
        sendMessageWithKeyboard(chatId, message, createRegistrationKeyboard());
    }

    private Object createRegistrationKeyboard() {
        Map<String, Object> keyboard = new HashMap<>();
        keyboard.put("inline_keyboard", Arrays.asList(
            Arrays.asList(
                Map.of("text", "📝 Register as Customer", "callback_data", "register_customer")
            ),
            Arrays.asList(
                Map.of("text", "🔗 Link Existing Account", "callback_data", "link_account")
            ),
            Arrays.asList(
                Map.of("text", "❌ Cancel", "callback_data", "cancel")
            )
        ));
        return keyboard;
    }

    @Override
    public void sendMessage(String chatId, String message) {
        sendTelegramMessage(chatId, message, null);
    }

    @Override
    public void sendMessageWithKeyboard(String chatId, String message, Object keyboard) {
        sendTelegramMessage(chatId, message, keyboard);
    }

    private void sendTelegramMessage(String chatId, String message, Object keyboard) {
        try {
            String url = TELEGRAM_API_URL + telegramConfig.getBot().getToken() + "/sendMessage";

            Map<String, Object> payload = new HashMap<>();
            payload.put("chat_id", chatId);
            payload.put("text", message);
            payload.put("parse_mode", "HTML");
            payload.put("disable_web_page_preview", true);
            
            if (keyboard != null) {
                payload.put("reply_markup", keyboard);
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
            restTemplate.postForEntity(url, request, String.class);

        } catch (Exception e) {
            log.error("❌ Failed to send Telegram message to chat: {}", chatId, e);
        }
    }

    @Override
    public boolean setWebhook(String url) {
        try {
            String apiUrl = TELEGRAM_API_URL + telegramConfig.getBot().getToken() + "/setWebhook";
            
            Map<String, Object> payload = new HashMap<>();
            payload.put("url", url + "/api/v1/telegram/bot/webhook");
            payload.put("allowed_updates", Arrays.asList("message", "callback_query"));
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, request, String.class);
            
            return response.getStatusCode().is2xxSuccessful();
            
        } catch (Exception e) {
            log.error("❌ Failed to set webhook: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public Object getWebhookInfo() {
        try {
            String url = TELEGRAM_API_URL + telegramConfig.getBot().getToken() + "/getWebhookInfo";
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            return objectMapper.readValue(response.getBody(), Object.class);
        } catch (Exception e) {
            log.error("❌ Failed to get webhook info: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public boolean deleteWebhook() {
        try {
            String url = TELEGRAM_API_URL + telegramConfig.getBot().getToken() + "/deleteWebhook";
            ResponseEntity<String> response = restTemplate.postForEntity(url, null, String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.error("❌ Failed to delete webhook: {}", e.getMessage());
            return false;
        }
    }

    // Inner class for managing user registration state
    private static class UserRegistrationState {
        public enum State {
            AWAITING_EMAIL, AWAITING_PHONE, 
            AWAITING_LINK_CREDENTIALS, AWAITING_LINK_PASSWORD
        }
        
        private State currentState;
        private TelegramUser telegramUser;
        private String email;
        private String phoneNumber;
        private String userIdentifier;
        private String password;
        
        public UserRegistrationState(State state) {
            this.currentState = state;
        }
        
        // Getters and setters
        public State getCurrentState() { return currentState; }
        public void setCurrentState(State currentState) { this.currentState = currentState; }
        public TelegramUser getTelegramUser() { return telegramUser; }
        public void setTelegramUser(TelegramUser telegramUser) { this.telegramUser = telegramUser; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
        public String getUserIdentifier() { return userIdentifier; }
        public void setUserIdentifier(String userIdentifier) { this.userIdentifier = userIdentifier; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}