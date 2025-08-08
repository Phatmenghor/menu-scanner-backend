package com.emenu.security;

import com.emenu.features.auth.models.User;
import com.emenu.features.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String userIdentifier) throws UsernameNotFoundException {
        log.debug("🔍 Loading user with userIdentifier: {}", userIdentifier);
        
        // Search by userIdentifier (supports both traditional and Telegram users)
        User user = userRepository.findByUserIdentifierAndIsDeletedFalse(userIdentifier)
                .orElseThrow(() -> {
                    log.warn("❌ User not found with userIdentifier: {}", userIdentifier);
                    return new UsernameNotFoundException("User not found with userIdentifier: " + userIdentifier);
                });

        log.debug("✅ User found: {} ({}), Type: {}, Social Provider: {}, Telegram: {}", 
                user.getDisplayName(), 
                user.getUserIdentifier(),
                user.getUserType(),
                user.getSocialProvider(),
                user.hasTelegramLinked() ? "Linked" : "Not Linked");

        // Create UserDetails with enhanced information
        return createUserDetails(user);
    }

    /**
     * Load user by Telegram User ID (for Telegram-specific operations)
     */
    public UserDetails loadUserByTelegramUserId(Long telegramUserId) throws UsernameNotFoundException {
        log.debug("🔍 Loading user with Telegram ID: {}", telegramUserId);
        
        User user = userRepository.findByTelegramUserIdAndIsDeletedFalse(telegramUserId)
                .orElseThrow(() -> {
                    log.warn("❌ User not found with Telegram ID: {}", telegramUserId);
                    return new UsernameNotFoundException("User not found with Telegram ID: " + telegramUserId);
                });

        log.debug("✅ Telegram user found: {} ({})", user.getDisplayName(), user.getUserIdentifier());
        return createUserDetails(user);
    }

    private UserDetails createUserDetails(User user) {
        // Enhanced account status checks
        boolean accountNonExpired = true;
        boolean accountNonLocked = !user.getAccountStatus().name().equals("LOCKED");
        boolean credentialsNonExpired = true;
        boolean enabled = user.getAccountStatus().name().equals("ACTIVE");

        // Log detailed status for debugging
        log.debug("📊 User account status - Enabled: {}, NonLocked: {}, Status: {}", 
                enabled, accountNonLocked, user.getAccountStatus());

        if (user.hasTelegramLinked()) {
            log.debug("📱 Telegram linked - Username: {}, Notifications: {}", 
                    user.getTelegramUsername(), 
                    user.canReceiveTelegramNotifications());
        }

        return new org.springframework.security.core.userdetails.User(
                user.getUserIdentifier(), // Use userIdentifier as the principal name
                user.getPassword() != null ? user.getPassword() : "", // Handle Telegram-only users
                enabled,
                accountNonExpired,
                credentialsNonExpired,
                accountNonLocked,
                mapRolesToAuthorities(user)
        );
    }

    private Collection<GrantedAuthority> mapRolesToAuthorities(User user) {
        Collection<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName().name()))
                .collect(Collectors.toList());

        log.debug("🔐 User authorities: {}", authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));

        return authorities;
    }

    /**
     * Check if user can authenticate with password (for traditional login)
     */
    public boolean canAuthenticateWithPassword(String userIdentifier) {
        try {
            User user = userRepository.findByUserIdentifierAndIsDeletedFalse(userIdentifier)
                    .orElse(null);
            
            if (user == null) {
                return false;
            }

            // User can authenticate with password if:
            // 1. They have a password set, OR
            // 2. They are a LOCAL provider (traditional account)
            boolean canAuth = user.getPassword() != null || user.getSocialProvider().requiresPassword();
            
            log.debug("🔐 Password auth check for {}: {} (hasPassword: {}, provider: {})", 
                    userIdentifier, canAuth, user.getPassword() != null, user.getSocialProvider());
            
            return canAuth;
        } catch (Exception e) {
            log.error("❌ Error checking password auth for {}: {}", userIdentifier, e.getMessage());
            return false;
        }
    }

    /**
     * Check if user can authenticate with Telegram (for Telegram login)
     */
    public boolean canAuthenticateWithTelegram(Long telegramUserId) {
        try {
            User user = userRepository.findByTelegramUserIdAndIsDeletedFalse(telegramUserId)
                    .orElse(null);
            
            if (user == null) {
                return false;
            }

            boolean canAuth = user.hasTelegramLinked() && user.isActive();
            
            log.debug("📱 Telegram auth check for {}: {} (linked: {}, active: {})", 
                    telegramUserId, canAuth, user.hasTelegramLinked(), user.isActive());
            
            return canAuth;
        } catch (Exception e) {
            log.error("❌ Error checking Telegram auth for {}: {}", telegramUserId, e.getMessage());
            return false;
        }
    }

    /**
     * Get user authentication summary for debugging
     */
    public String getUserAuthSummary(String userIdentifier) {
        try {
            User user = userRepository.findByUserIdentifierAndIsDeletedFalse(userIdentifier)
                    .orElse(null);
            
            if (user == null) {
                return "User not found";
            }

            return String.format("User: %s, Type: %s, Status: %s, Provider: %s, HasPassword: %s, TelegramLinked: %s",
                    user.getDisplayName(),
                    user.getUserType(),
                    user.getAccountStatus(),
                    user.getSocialProvider(),
                    user.getPassword() != null,
                    user.hasTelegramLinked());
        } catch (Exception e) {
            return "Error getting user summary: " + e.getMessage();
        }
    }
}