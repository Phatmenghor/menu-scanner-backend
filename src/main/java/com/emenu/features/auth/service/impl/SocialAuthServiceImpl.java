package com.emenu.features.auth.service.impl;

import com.emenu.enums.social.SocialAuthProvider;
import com.emenu.enums.user.AccountStatus;
import com.emenu.enums.user.UserType;
import com.emenu.exception.custom.ValidationException;
import com.emenu.features.auth.models.Role;
import com.emenu.features.auth.models.User;
import com.emenu.features.auth.repository.RoleRepository;
import com.emenu.features.auth.repository.UserRepository;
import com.emenu.features.auth.service.RefreshTokenService;
import com.emenu.features.auth.dto.request.SocialAuthRequest;
import com.emenu.features.auth.dto.response.SocialAuthResponse;
import com.emenu.features.auth.dto.response.SocialSyncResponse;
import com.emenu.features.auth.service.SocialAuthService;
import com.emenu.features.auth.service.social.provider.GoogleAuthProvider;
import com.emenu.features.auth.service.social.provider.SocialUserInfo;
import com.emenu.features.auth.service.social.provider.TelegramAuthProvider;
import com.emenu.security.jwt.JWTGenerator;
import com.emenu.security.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SocialAuthServiceImpl implements SocialAuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final TelegramAuthProvider telegramAuthProvider;
    private final GoogleAuthProvider googleAuthProvider;
    private final JWTGenerator jwtGenerator;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;
    private final SecurityUtils securityUtils;

    @Override
    public SocialAuthResponse authenticate(SocialAuthRequest request) {
        log.info("Social auth: provider={}, userType={}", request.getProvider(), request.getUserType());

        SocialAuthProvider provider = SocialAuthProvider.fromProviderKey(request.getProvider());
        SocialUserInfo userInfo = fetchUserInfo(provider, request.getAccessToken());

        User user = findOrCreateUser(userInfo, provider, request.getUserType(), request.getBusinessId());

        syncSocialData(user, provider, userInfo);
        userRepository.save(user);

        List<String> roles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .toList();

        String accessToken = jwtGenerator.generateAccessTokenFromUsername(user.getUserIdentifier(), roles);
        String refreshToken = refreshTokenService.createRefreshToken(
                user, request.getIpAddress(), request.getDeviceInfo()
        ).getToken();

        log.info("Social auth successful: user={}, provider={}", user.getUserIdentifier(), provider);

        return SocialAuthResponse.builder()
                .success(true)
                .message("Authentication successful")
                .provider(provider)
                .userId(user.getId())
                .userIdentifier(user.getUserIdentifier())
                .userType(user.getUserType().name())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .socialId(userInfo.id())
                .socialUsername(userInfo.username())
                .syncedAt(java.time.LocalDateTime.now())
                .operationType(user.getCreatedAt().isAfter(java.time.LocalDateTime.now().minusSeconds(5)) ? "register" : "login")
                .isNewUser(user.getCreatedAt().isAfter(java.time.LocalDateTime.now().minusSeconds(5)))
                .build();
    }

    @Override
    public SocialSyncResponse syncSocialAccount(SocialAuthRequest request) {
        UUID currentUserId = securityUtils.getCurrentUserId();
        User user = userRepository.findByIdAndIsDeletedFalse(currentUserId)
                .orElseThrow(() -> new ValidationException("User not found"));

        SocialAuthProvider provider = SocialAuthProvider.fromProviderKey(request.getProvider());
        SocialUserInfo userInfo = fetchUserInfo(provider, request.getAccessToken());

        syncSocialData(user, provider, userInfo);
        userRepository.save(user);

        return SocialSyncResponse.builder()
                .success(true)
                .message(provider.getDisplayName() + " account synced successfully")
                .provider(provider.getProviderKey())
                .syncedAt(java.time.LocalDateTime.now())
                .build();
    }

    @Override
    public SocialSyncResponse unsyncSocialAccount(String providerKey) {
        UUID currentUserId = securityUtils.getCurrentUserId();
        User user = userRepository.findByIdAndIsDeletedFalse(currentUserId)
                .orElseThrow(() -> new ValidationException("User not found"));

        SocialAuthProvider provider = SocialAuthProvider.fromProviderKey(providerKey);

        switch (provider) {
            case TELEGRAM -> user.unsyncTelegram();
            case GOOGLE -> user.unsyncGoogle();
            default -> throw new ValidationException("Unsupported provider: " + provider);
        }

        userRepository.save(user);

        return SocialSyncResponse.builder()
                .success(true)
                .message(provider.getDisplayName() + " account unsynced successfully")
                .provider(provider.getProviderKey())
                .build();
    }

    private SocialUserInfo fetchUserInfo(SocialAuthProvider provider, String accessToken) {
        return switch (provider) {
            case TELEGRAM -> telegramAuthProvider.getUserInfo(accessToken);
            case GOOGLE -> googleAuthProvider.getUserInfo(accessToken);
            default -> throw new ValidationException("Provider not yet implemented: " + provider);
        };
    }

    private User findOrCreateUser(SocialUserInfo userInfo, SocialAuthProvider provider, 
                                   UserType userType, UUID businessId) {
        return switch (provider) {
            case TELEGRAM -> findOrCreateByTelegram(userInfo, userType, businessId);
            case GOOGLE -> findOrCreateByGoogle(userInfo, userType, businessId);
            default -> throw new ValidationException("Provider not yet implemented: " + provider);
        };
    }

    private User findOrCreateByTelegram(SocialUserInfo userInfo, UserType userType, UUID businessId) {
        Long telegramId = Long.parseLong(userInfo.id());
        
        return userRepository.findByTelegramIdAndIsDeletedFalse(telegramId)
                .orElseGet(() -> createNewUser(userInfo, userType, businessId));
    }

    private User findOrCreateByGoogle(SocialUserInfo userInfo, UserType userType, UUID businessId) {
        if (userType == UserType.BUSINESS_USER && businessId != null) {
            return userRepository.findByGoogleIdAndUserTypeAndBusinessIdAndIsDeletedFalse(
                    userInfo.id(), userType, businessId
            ).orElseGet(() -> createNewUser(userInfo, userType, businessId));
        } else {
            return userRepository.findByGoogleIdAndUserTypeAndIsDeletedFalse(userInfo.id(), userType)
                    .orElseGet(() -> createNewUser(userInfo, userType, businessId));
        }
    }

    private User createNewUser(SocialUserInfo userInfo, UserType userType, UUID businessId) {
        String userIdentifier = generateUserIdentifier(userInfo, userType);

        String defaultRole = switch (userType) {
            case PLATFORM_USER -> "PLATFORM_OWNER";
            case BUSINESS_USER -> "BUSINESS_OWNER";
            case CUSTOMER -> "CUSTOMER";
        };

        Role role = roleRepository.findByNameAndIsDeletedFalse(defaultRole)
                .orElseThrow(() -> new ValidationException("Default role not found: " + defaultRole));

        User user = new User();
        user.setUserIdentifier(userIdentifier);
        user.setEmail(userInfo.email());
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        user.setFirstName(userInfo.firstName());
        user.setLastName(userInfo.lastName());
        user.setUserType(userType);
        user.setAccountStatus(AccountStatus.ACTIVE);
        user.setBusinessId(businessId);
        user.setRoles(List.of(role));

        return userRepository.save(user);
    }

    private void syncSocialData(User user, SocialAuthProvider provider, SocialUserInfo userInfo) {
        switch (provider) {
            case TELEGRAM -> user.syncTelegram(
                    Long.parseLong(userInfo.id()),
                    userInfo.username(),
                    userInfo.firstName(),
                    userInfo.lastName()
            );
            case GOOGLE -> user.syncGoogle(userInfo.id(), userInfo.email());
        }
    }

    private String generateUserIdentifier(SocialUserInfo userInfo, UserType userType) {
        String base = userInfo.username() != null ? userInfo.username() :
                      userInfo.email() != null ? userInfo.email().split("@")[0] :
                      "user" + userInfo.id().substring(0, 8);

        String identifier = base.toLowerCase().replaceAll("[^a-z0-9_]", "");
        
        int suffix = 1;
        String candidate = identifier;
        while (userRepository.existsByUserIdentifierAndUserTypeAndIsDeletedFalse(candidate, userType)) {
            candidate = identifier + suffix;
            suffix++;
        }
        
        return candidate;
    }
}
