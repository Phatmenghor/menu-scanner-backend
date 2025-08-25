// src/main/java/com/emenu/features/auth/service/impl/TelegramAuthServiceImpl.java
package com.emenu.features.auth.service.impl;

import com.emenu.enums.auth.SocialProvider;
import com.emenu.enums.user.AccountStatus;
import com.emenu.enums.user.RoleEnum;
import com.emenu.enums.user.UserType;
import com.emenu.exception.custom.ValidationException;
import com.emenu.features.auth.dto.request.TelegramAuthRequest;
import com.emenu.features.auth.dto.request.TelegramLinkRequest;
import com.emenu.features.auth.dto.response.TelegramAuthResponse;
import com.emenu.features.auth.models.Role;
import com.emenu.features.auth.models.User;
import com.emenu.features.auth.repository.RoleRepository;
import com.emenu.features.auth.repository.UserRepository;
import com.emenu.features.auth.service.TelegramService;
import com.emenu.features.notification.service.TelegramNotificationService;
import com.emenu.security.SecurityUtils;
import com.emenu.security.jwt.JWTGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TelegramAuthServiceImpl implements TelegramService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final SecurityUtils securityUtils;
    private final JWTGenerator jwtGenerator;
    private final TelegramNotificationService telegramNotificationService;

    // ===== TELEGRAM LOGIN =====
    @Override
    public TelegramAuthResponse loginWithTelegram(TelegramAuthRequest request) {
        log.info("📱 Telegram login attempt for user: {}", request.getTelegramUserId());
        
        Optional<User> userOpt = userRepository.findByTelegramUserIdAndIsDeletedFalse(request.getTelegramUserId());
        
        if (userOpt.isEmpty()) {
            log.warn("❌ No user linked to Telegram ID: {}", request.getTelegramUserId());
            throw new ValidationException("No user account linked to this Telegram account. Please register first or link your existing account.");
        }
        
        User user = userOpt.get();
        validateAccountStatus(user);
        
        user.updateTelegramActivity();
        userRepository.save(user);
        
        Authentication authentication = createAuthenticationFromUser(user);
        String token = jwtGenerator.generateAccessToken(authentication);
        
        TelegramAuthResponse response = buildTelegramAuthResponse(user, token, false);
        
        log.info("✅ Telegram login successful for user: {}", user.getUserIdentifier());
        return response;
    }

    // ===== TELEGRAM REGISTRATION (CUSTOMER ONLY) =====

    @Override
    public TelegramAuthResponse registerCustomerWithTelegram(TelegramAuthRequest request) {
        log.info("📝 Telegram customer registration for user: {}", request.getTelegramUserId());
        
        if (userRepository.existsByTelegramUserIdAndIsDeletedFalse(request.getTelegramUserId())) {
            throw new ValidationException("This Telegram account is already linked to a user. Please login instead.");
        }
        
        String userIdentifier = generateUserIdentifier(request);
        
        if (userRepository.existsByUserIdentifierAndIsDeletedFalse(userIdentifier)) {
            throw new ValidationException("User identifier '" + userIdentifier + "' is already taken. Please try again.");
        }
        
        User user = createCustomerFromTelegramData(request, userIdentifier);
        user = userRepository.save(user);
        
        // 📱 Send Telegram notification for customer registration
        telegramNotificationService.sendCustomerRegistrationNotification(user);
        
        Authentication authentication = createAuthenticationFromUser(user);
        String token = jwtGenerator.generateAccessToken(authentication);
        
        TelegramAuthResponse response = buildTelegramAuthResponse(user, token, true);
        response.setMessage("Customer account created successfully with Telegram!");
        
        log.info("✅ Telegram customer registration successful for user: {}", user.getUserIdentifier());
        return response;
    }

    // ===== LINK TELEGRAM TO EXISTING USER =====
    
    public void linkTelegramToCurrentUser(TelegramLinkRequest request) {
        User currentUser = securityUtils.getCurrentUser();
        linkTelegramToUser(currentUser.getId(), request);
    }

    public void linkTelegramToUser(UUID userId, TelegramLinkRequest request) {
        log.info("🔗 Linking Telegram {} to user ID: {}", request.getTelegramUserId(), userId);

        Optional<User> existingTelegramUser = userRepository.findByTelegramUserIdAndIsDeletedFalse(request.getTelegramUserId());
        if (existingTelegramUser.isPresent() && !existingTelegramUser.get().getId().equals(userId)) {
            throw new ValidationException("This Telegram account is already linked to another user.");
        }

        User user = userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new ValidationException("User not found"));

        if (user.hasTelegramLinked()) {
            throw new ValidationException("User already has a Telegram account linked");
        }

        user.linkTelegram(
                request.getTelegramUserId(),
                request.getTelegramUsername(),
                request.getTelegramFirstName(),
                request.getTelegramLastName()
        );

        userRepository.save(user);

        try {
            telegramNotificationService.sendTelegramLinkNotification(user);
        } catch (Exception e) {
            log.warn("⚠️ Failed to send Telegram link notification: {}", e.getMessage());
        }

        log.info("✅ Telegram successfully linked to user: {}", user.getUserIdentifier());
    }

    // ===== UNLINK TELEGRAM =====
    
    public void unlinkTelegramFromCurrentUser() {
        User currentUser = securityUtils.getCurrentUser();
        unlinkTelegramFromUser(currentUser.getId());
    }
    
    public void unlinkTelegramFromUser(UUID userId) {
        log.info("🔓 Unlinking Telegram from user: {}", userId);
        
        User user = userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new ValidationException("User not found"));
        
        if (!user.hasTelegramLinked()) {
            throw new ValidationException("User does not have Telegram linked");
        }
        
        user.unlinkTelegram();
        userRepository.save(user);
        
        log.info("✅ Telegram unlinked from user: {}", user.getUserIdentifier());
    }

    // ===== CHECK TELEGRAM STATUS =====
    
    public boolean isTelegramLinked(Long telegramUserId) {
        return userRepository.existsByTelegramUserIdAndIsDeletedFalse(telegramUserId);
    }
    
    public Optional<User> findUserByTelegramId(Long telegramUserId) {
        return userRepository.findByTelegramUserIdAndIsDeletedFalse(telegramUserId);
    }

    // ===== HELPER METHODS =====
    
    private String generateUserIdentifier(TelegramAuthRequest request) {
        String base;
        
        if (request.getUserIdentifier() != null && !request.getUserIdentifier().trim().isEmpty()) {
            base = request.getUserIdentifier().toLowerCase().replaceAll("[^a-z0-9]", "");
        } else if (request.getTelegramUsername() != null && !request.getTelegramUsername().trim().isEmpty()) {
            base = request.getTelegramUsername().toLowerCase().replaceAll("[^a-z0-9]", "");
        } else if (request.getTelegramFirstName() != null && !request.getTelegramFirstName().trim().isEmpty()) {
            base = request.getTelegramFirstName().toLowerCase().replaceAll("[^a-z0-9]", "");
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
    
    private User createCustomerFromTelegramData(TelegramAuthRequest request, String userIdentifier) {
        User user = new User();
        
        user.setUserIdentifier(userIdentifier);
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setFirstName(request.getTelegramFirstName());
        user.setLastName(request.getTelegramLastName());
        user.setUserType(UserType.CUSTOMER);
        user.setAccountStatus(AccountStatus.ACTIVE);
        user.setSocialProvider(SocialProvider.TELEGRAM);
        
        user.linkTelegram(
                request.getTelegramUserId(),
                request.getTelegramUsername(),
                request.getTelegramFirstName(),
                request.getTelegramLastName()
        );
        
        Role customerRole = roleRepository.findByName(RoleEnum.CUSTOMER)
                .orElseThrow(() -> new ValidationException("Customer role not found"));
        user.setRoles(List.of(customerRole));
        
        return user;
    }
    
    private Authentication createAuthenticationFromUser(User user) {
        List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName().name()))
                .toList();
        
        return new UsernamePasswordAuthenticationToken(user.getUserIdentifier(), null, authorities);
    }
    
    private TelegramAuthResponse buildTelegramAuthResponse(User user, String token, boolean isNewUser) {
        TelegramAuthResponse response = new TelegramAuthResponse();
        
        response.setAccessToken(token);
        response.setTokenType("Bearer");
        response.setUserId(user.getId());
        response.setUserIdentifier(user.getUserIdentifier());
        response.setEmail(user.getEmail());
        response.setFullName(user.getFullName());
        response.setDisplayName(user.getDisplayName());
        response.setUserType(user.getUserType());
        response.setRoles(user.getRoles().stream().map(role -> role.getName().name()).toList());
        response.setBusinessId(user.getBusinessId());
        response.setBusinessName(user.getBusiness() != null ? user.getBusiness().getName() : null);
        response.setSocialProvider(user.getSocialProvider());
        response.setTelegramUserId(user.getTelegramUserId());
        response.setTelegramUsername(user.getTelegramUsername());
        response.setTelegramDisplayName(user.getTelegramDisplayName());
        response.setTelegramLinkedAt(user.getTelegramLinkedAt());
        response.setIsNewUser(isNewUser);
        response.setHasPasswordSet(user.getPassword() != null);
        
        if (isNewUser) {
            response.setWelcomeMessage(String.format("Welcome to Cambodia E-Menu Platform, %s! 🇰🇭🎉", user.getDisplayName()));
        } else {
            response.setWelcomeMessage(String.format("Welcome back, %s! 🎉", user.getDisplayName()));
        }
        
        return response;
    }
    
    private void validateAccountStatus(User user) {
        if (user.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new ValidationException("Account is " + user.getAccountStatus().name().toLowerCase() + ". Please contact support.");
        }
    }
}