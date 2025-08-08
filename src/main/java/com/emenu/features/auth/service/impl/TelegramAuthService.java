package com.emenu.features.auth.service.impl;

import com.emenu.enums.auth.SocialProvider;
import com.emenu.enums.user.AccountStatus;
import com.emenu.enums.user.RoleEnum;
import com.emenu.enums.user.UserType;
import com.emenu.exception.custom.ValidationException;
import com.emenu.features.auth.dto.request.TelegramLoginRequest;
import com.emenu.features.auth.dto.request.TelegramRegisterRequest;
import com.emenu.features.auth.dto.response.TelegramLoginResponse;
import com.emenu.features.auth.dto.response.TelegramRegisterResponse;
import com.emenu.features.auth.models.Role;
import com.emenu.features.auth.models.User;
import com.emenu.features.auth.repository.RoleRepository;
import com.emenu.features.auth.repository.UserRepository;
import com.emenu.features.notification.models.TelegramUserSession;
import com.emenu.features.notification.repository.TelegramUserSessionRepository;
import com.emenu.features.notification.service.TelegramService;
import com.emenu.security.jwt.JWTGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TelegramAuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final TelegramUserSessionRepository sessionRepository;
    private final TelegramService telegramService;
    private final JWTGenerator jwtGenerator;

    // ===== TELEGRAM LOGIN =====
    
    public TelegramLoginResponse loginWithTelegram(TelegramLoginRequest request) {
        log.info("🔐 Telegram login attempt for user: {}", request.getTelegramUserId());
        
        // Find or create session
        TelegramUserSession session = findOrCreateSession(request);
        
        // Find linked user
        Optional<User> userOpt = userRepository.findByTelegramUserIdAndIsDeletedFalse(request.getTelegramUserId());
        
        if (userOpt.isEmpty()) {
            log.warn("❌ No user linked to Telegram ID: {}", request.getTelegramUserId());
            throw new ValidationException("No user account linked to this Telegram account. Please register first.");
        }
        
        User user = userOpt.get();
        
        // Validate account status
        if (user.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new ValidationException("Account is " + user.getAccountStatus().name().toLowerCase() + ". Please contact support.");
        }
        
        // Update Telegram activity
        user.updateTelegramActivity();
        session.updateActivity();
        
        userRepository.save(user);
        sessionRepository.save(session);
        
        // Generate JWT token
        Authentication authentication = createAuthenticationFromUser(user);
        String token = jwtGenerator.generateAccessToken(authentication);
        
        // Build response
        TelegramLoginResponse response = buildLoginResponse(user, token, false);
        
        log.info("✅ Telegram login successful for user: {}", user.getUserIdentifier());
        
        // Send welcome back message
        telegramService.sendDirectMessageToUser(
                request.getTelegramUserId().toString(),
                String.format("🎉 Welcome back, %s!\n\nYou've successfully logged in to Cambodia E-Menu Platform.", 
                        user.getDisplayName()),
                "Login Success"
        );
        
        return response;
    }

    // ===== TELEGRAM REGISTRATION =====
    
    public TelegramRegisterResponse registerWithTelegram(TelegramRegisterRequest request) {
        log.info("📝 Telegram registration for user: {}", request.getTelegramUserId());
        
        // Check if Telegram user already linked
        if (userRepository.existsByTelegramUserIdAndIsDeletedFalse(request.getTelegramUserId())) {
            throw new ValidationException("This Telegram account is already linked to a user. Please login instead.");
        }
        
        // Generate userIdentifier if not provided
        String userIdentifier = request.getUserIdentifier();
        if (userIdentifier == null || userIdentifier.trim().isEmpty()) {
            userIdentifier = generateUserIdentifierFromTelegram(request);
        }
        
        // Check userIdentifier availability
        if (userRepository.existsByUserIdentifierAndIsDeletedFalse(userIdentifier)) {
            throw new ValidationException("User identifier '" + userIdentifier + "' is already taken. Please choose a different one.");
        }
        
        // Create user
        User user = createUserFromTelegramData(request, userIdentifier);
        
        // Create or update session
        TelegramUserSession session = findOrCreateSession(request);
        session.markAsRegistered(user.getId());
        sessionRepository.save(session);
        
        // Build response
        TelegramRegisterResponse response = buildRegisterResponse(user);
        
        log.info("✅ Telegram registration successful for user: {}", user.getUserIdentifier());
        
        // Send welcome message
        telegramService.sendRegistrationSuccessMessage(request.getTelegramUserId(), user.getDisplayName());
        
        // Notify platform users about new registration
        notifyPlatformUsersAboutNewRegistration(user);
        
        return response;
    }

    // ===== LINK EXISTING USER TO TELEGRAM =====
    
    @Transactional
    public void linkExistingUserToTelegram(UUID userId, TelegramLoginRequest telegramData) {
        log.info("🔗 Linking existing user {} to Telegram: {}", userId, telegramData.getTelegramUserId());
        
        // Check if Telegram is already linked to another user
        Optional<User> existingTelegramUser = userRepository.findByTelegramUserIdAndIsDeletedFalse(telegramData.getTelegramUserId());
        if (existingTelegramUser.isPresent() && !existingTelegramUser.get().getId().equals(userId)) {
            throw new ValidationException("This Telegram account is already linked to another user.");
        }
        
        // Get user
        User user = userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new ValidationException("User not found"));
        
        // Link Telegram
        user.linkTelegram(
                telegramData.getTelegramUserId(),
                telegramData.getTelegramUsername(),
                telegramData.getTelegramFirstName(),
                telegramData.getTelegramLastName()
        );
        
        userRepository.save(user);
        
        // Create or update session
        TelegramUserSession session = findOrCreateSession(telegramData);
        session.markAsRegistered(userId);
        sessionRepository.save(session);
        
        log.info("✅ User {} successfully linked to Telegram: {}", user.getUserIdentifier(), telegramData.getTelegramUserId());
        
        // Send confirmation message
        telegramService.sendDirectMessageToUser(
                telegramData.getTelegramUserId().toString(),
                String.format("""
                        🔗 <b>Account Linked Successfully!</b>
                        
                        Your Telegram account has been linked to:
                        👤 <b>User:</b> %s
                        🏷️ <b>Type:</b> %s
                        
                        🎉 You can now login using Telegram!
                        """, user.getDisplayName(), user.getUserType().getDescription()),
                "Account Linked"
        );
    }

    // ===== UNLINK TELEGRAM =====
    
    @Transactional
    public void unlinkTelegramFromUser(UUID userId) {
        log.info("🔓 Unlinking Telegram from user: {}", userId);
        
        User user = userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new ValidationException("User not found"));
        
        if (!user.hasTelegramLinked()) {
            throw new ValidationException("User does not have Telegram linked");
        }
        
        Long telegramUserId = user.getTelegramUserId();
        
        // Unlink from user
        user.unlinkTelegram();
        userRepository.save(user);
        
        // Update session
        Optional<TelegramUserSession> session = sessionRepository.findByTelegramUserIdAndIsDeletedFalse(telegramUserId);
        session.ifPresent(s -> {
            s.setUserId(null);
            s.setIsRegistered(false);
            sessionRepository.save(s);
        });
        
        log.info("✅ Telegram unlinked from user: {}", user.getUserIdentifier());
    }

    // ===== HELPER METHODS =====
    
    private TelegramUserSession findOrCreateSession(TelegramLoginRequest request) {
        Optional<TelegramUserSession> sessionOpt = sessionRepository.findByTelegramUserIdAndIsDeletedFalse(request.getTelegramUserId());
        
        if (sessionOpt.isPresent()) {
            TelegramUserSession session = sessionOpt.get();
            session.updateActivity();
            // Update session data with latest info
            updateSessionFromRequest(session, request);
            return session;
        } else {
            return createNewSession(request);
        }
    }
    
    private TelegramUserSession findOrCreateSession(TelegramRegisterRequest request) {
        Optional<TelegramUserSession> sessionOpt = sessionRepository.findByTelegramUserIdAndIsDeletedFalse(request.getTelegramUserId());
        
        if (sessionOpt.isPresent()) {
            TelegramUserSession session = sessionOpt.get();
            session.updateActivity();
            updateSessionFromRequest(session, request);
            return session;
        } else {
            return createNewSession(request);
        }
    }
    
    private TelegramUserSession createNewSession(TelegramLoginRequest request) {
        TelegramUserSession session = new TelegramUserSession();
        session.setTelegramUserId(request.getTelegramUserId());
        session.setTelegramUsername(request.getTelegramUsername());
        session.setTelegramFirstName(request.getTelegramFirstName());
        session.setTelegramLastName(request.getTelegramLastName());
        session.setChatId(request.getChatId() != null ? request.getChatId() : request.getTelegramUserId().toString());
        session.setLanguageCode(request.getLanguageCode());
        session.setIsPremium(request.getIsPremium());
        session.setFirstInteraction(LocalDateTime.now());
        session.setLastActivity(LocalDateTime.now());
        
        return sessionRepository.save(session);
    }
    
    private TelegramUserSession createNewSession(TelegramRegisterRequest request) {
        TelegramUserSession session = new TelegramUserSession();
        session.setTelegramUserId(request.getTelegramUserId());
        session.setTelegramUsername(request.getTelegramUsername());
        session.setTelegramFirstName(request.getTelegramFirstName());
        session.setTelegramLastName(request.getTelegramLastName());
        session.setChatId(request.getChatId() != null ? request.getChatId() : request.getTelegramUserId().toString());
        session.setLanguageCode(request.getLanguageCode());
        session.setIsPremium(request.getIsPremium());
        session.setFirstInteraction(LocalDateTime.now());
        session.setLastActivity(LocalDateTime.now());
        
        return sessionRepository.save(session);
    }
    
    private void updateSessionFromRequest(TelegramUserSession session, TelegramLoginRequest request) {
        if (request.getTelegramUsername() != null) session.setTelegramUsername(request.getTelegramUsername());
        if (request.getTelegramFirstName() != null) session.setTelegramFirstName(request.getTelegramFirstName());
        if (request.getTelegramLastName() != null) session.setTelegramLastName(request.getTelegramLastName());
        if (request.getChatId() != null) session.setChatId(request.getChatId());
        if (request.getLanguageCode() != null) session.setLanguageCode(request.getLanguageCode());
        if (request.getIsPremium() != null) session.setIsPremium(request.getIsPremium());
    }
    
    private void updateSessionFromRequest(TelegramUserSession session, TelegramRegisterRequest request) {
        if (request.getTelegramUsername() != null) session.setTelegramUsername(request.getTelegramUsername());
        if (request.getTelegramFirstName() != null) session.setTelegramFirstName(request.getTelegramFirstName());
        if (request.getTelegramLastName() != null) session.setTelegramLastName(request.getTelegramLastName());
        if (request.getChatId() != null) session.setChatId(request.getChatId());
        if (request.getLanguageCode() != null) session.setLanguageCode(request.getLanguageCode());
        if (request.getIsPremium() != null) session.setIsPremium(request.getIsPremium());
    }
    
    private String generateUserIdentifierFromTelegram(TelegramRegisterRequest request) {
        String base;
        
        if (request.getTelegramUsername() != null && !request.getTelegramUsername().trim().isEmpty()) {
            base = request.getTelegramUsername().toLowerCase().replaceAll("[^a-z0-9]", "");
        } else if (request.getTelegramFirstName() != null && !request.getTelegramFirstName().trim().isEmpty()) {
            base = request.getTelegramFirstName().toLowerCase().replaceAll("[^a-z0-9]", "");
        } else {
            base = "telegramuser";
        }
        
        // Add random numbers to ensure uniqueness
        String userIdentifier = base + "_" + ThreadLocalRandom.current().nextInt(1000, 9999);
        
        // Ensure it's unique
        int attempts = 0;
        while (userRepository.existsByUserIdentifierAndIsDeletedFalse(userIdentifier) && attempts < 10) {
            userIdentifier = base + "_" + ThreadLocalRandom.current().nextInt(10000, 99999);
            attempts++;
        }
        
        return userIdentifier;
    }
    
    private User createUserFromTelegramData(TelegramRegisterRequest request, String userIdentifier) {
        User user = new User();
        
        // Basic user info
        user.setUserIdentifier(userIdentifier);
        user.setEmail(request.getEmail()); // Optional
        user.setPhoneNumber(request.getPhoneNumber()); // Optional
        user.setFirstName(request.getFirstName() != null ? request.getFirstName() : request.getTelegramFirstName());
        user.setLastName(request.getLastName() != null ? request.getLastName() : request.getTelegramLastName());
        user.setUserType(request.getUserType());
        user.setAccountStatus(AccountStatus.ACTIVE);
        user.setSocialProvider(SocialProvider.TELEGRAM);
        
        // Telegram info
        user.linkTelegram(
                request.getTelegramUserId(),
                request.getTelegramUsername(),
                request.getTelegramFirstName(),
                request.getTelegramLastName()
        );
        
        // Set roles based on user type
        List<Role> roles = getUserRoles(request.getUserType());
        user.setRoles(roles);
        
        return userRepository.save(user);
    }
    
    private List<Role> getUserRoles(UserType userType) {
        return switch (userType) {
            case CUSTOMER -> List.of(roleRepository.findByName(RoleEnum.CUSTOMER)
                    .orElseThrow(() -> new ValidationException("Customer role not found")));
            case BUSINESS_USER -> List.of(roleRepository.findByName(RoleEnum.BUSINESS_OWNER)
                    .orElseThrow(() -> new ValidationException("Business owner role not found")));
            case PLATFORM_USER -> List.of(roleRepository.findByName(RoleEnum.PLATFORM_MANAGER)
                    .orElseThrow(() -> new ValidationException("Platform manager role not found")));
        };
    }
    
    private Authentication createAuthenticationFromUser(User user) {
        List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName().name()))
                .toList();
        
        return new UsernamePasswordAuthenticationToken(user.getUserIdentifier(), null, authorities);
    }
    
    private TelegramLoginResponse buildLoginResponse(User user, String token, boolean isNewUser) {
        TelegramLoginResponse response = new TelegramLoginResponse();
        
        // Authentication
        response.setAccessToken(token);
        response.setTokenType("Bearer");
        
        // User info
        response.setUserId(user.getId());
        response.setUserIdentifier(user.getUserIdentifier());
        response.setEmail(user.getEmail());
        response.setFullName(user.getFullName());
        response.setDisplayName(user.getDisplayName());
        response.setProfileImageUrl(user.getProfileImageUrl());
        response.setUserType(user.getUserType());
        response.setRoles(user.getRoles().stream().map(role -> role.getName().name()).toList());
        
        // Business info
        response.setBusinessId(user.getBusinessId());
        response.setBusinessName(user.getBusiness() != null ? user.getBusiness().getName() : null);
        
        // Telegram info
        response.setSocialProvider(user.getSocialProvider());
        response.setTelegramUserId(user.getTelegramUserId());
        response.setTelegramUsername(user.getTelegramUsername());
        response.setTelegramDisplayName(user.getTelegramDisplayName());
        response.setTelegramLinkedAt(user.getTelegramLinkedAt());
        response.setTelegramNotificationsEnabled(user.getTelegramNotificationsEnabled());
        
        // Status
        response.setIsNewUser(isNewUser);
        response.setHasPasswordSet(user.getPassword() != null);
        response.setWelcomeMessage(String.format("Welcome back, %s! 🎉", user.getDisplayName()));
        
        // Features
        List<String> features = new ArrayList<>();
        features.add("Telegram Login");
        features.add("Real-time Notifications");
        if (user.isBusinessUser()) {
            features.add("Business Management");
            features.add("Menu Management");
        } else if (user.isCustomer()) {
            features.add("Browse Menus");
            features.add("Place Orders");
        } else if (user.isPlatformUser()) {
            features.add("Platform Administration");
            features.add("User Management");
        }
        response.setAvailableFeatures(features);
        
        return response;
    }
    
    private TelegramRegisterResponse buildRegisterResponse(User user) {
        TelegramRegisterResponse response = new TelegramRegisterResponse();
        
        // User info
        response.setUserId(user.getId());
        response.setUserIdentifier(user.getUserIdentifier());
        response.setEmail(user.getEmail());
        response.setFullName(user.getFullName());
        response.setDisplayName(user.getDisplayName());
        response.setUserType(user.getUserType());
        response.setRoles(user.getRoles().stream().map(role -> role.getName().name()).toList());
        
        // Telegram info
        response.setTelegramUserId(user.getTelegramUserId());
        response.setTelegramUsername(user.getTelegramUsername());
        response.setTelegramDisplayName(user.getTelegramDisplayName());
        response.setTelegramLinkedAt(user.getTelegramLinkedAt());
        
        // Registration status
        response.setIsNewUser(true);
        response.setWelcomeMessage(String.format("Welcome to Cambodia E-Menu Platform, %s! 🇰🇭🎉", user.getDisplayName()));
        
        // Next steps
        List<String> nextSteps = new ArrayList<>();
        nextSteps.add("🔐 You can now login with Telegram");
        nextSteps.add("🔔 You'll receive notifications via Telegram");
        if (user.isBusinessUser()) {
            nextSteps.add("🏪 Set up your business profile");
            nextSteps.add("🍽️ Create your digital menu");
        } else if (user.isCustomer()) {
            nextSteps.add("🔍 Explore restaurants near you");
            nextSteps.add("📱 Browse digital menus");
        }
        response.setNextSteps(nextSteps);
        
        response.setLoginUrl("Use Telegram to login anytime!");
        
        return response;
    }
    
    private void notifyPlatformUsersAboutNewRegistration(User user) {
        String registeredAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        
        telegramService.sendUserRegisteredNotification(
                user.getUserIdentifier(),
                user.getDisplayName(),
                user.getUserType().name(),
                registeredAt
        );
    }
}