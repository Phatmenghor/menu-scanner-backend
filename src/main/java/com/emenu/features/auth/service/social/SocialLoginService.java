package com.emenu.features.auth.service.social;

import com.emenu.enums.auth.SocialProvider;
import com.emenu.enums.user.AccountStatus;
import com.emenu.enums.user.RoleEnum;
import com.emenu.enums.user.UserType;
import com.emenu.exception.custom.ValidationException;
import com.emenu.features.auth.dto.request.LinkSocialAccountRequest;
import com.emenu.features.auth.dto.request.SocialLoginRequest;
import com.emenu.features.auth.dto.request.SocialLoginResult;
import com.emenu.features.auth.dto.response.SocialAccountResponse;
import com.emenu.features.auth.dto.response.SocialLoginResponse;
import com.emenu.features.auth.mapper.AuthMapper;
import com.emenu.features.auth.models.Role;
import com.emenu.features.auth.models.SocialUserAccount;
import com.emenu.features.auth.models.User;
import com.emenu.features.auth.repository.RoleRepository;
import com.emenu.features.auth.repository.SocialUserAccountRepository;
import com.emenu.features.auth.repository.UserRepository;
import com.emenu.security.SecurityUtils;
import com.emenu.security.jwt.JWTGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SocialLoginService {

    private final TelegramLoginService telegramLoginService;
    private final GoogleOAuth2Service googleOAuth2Service;
    private final UserRepository userRepository;
    private final SocialUserAccountRepository socialUserAccountRepository;
    private final RoleRepository roleRepository;
    private final JWTGenerator jwtGenerator;
    private final SecurityUtils securityUtils;

    /**
     * Main social login entry point
     */
    public SocialLoginResponse processSocialLogin(SocialLoginRequest request) {
        log.info("🔐 Processing social login with provider: {}", request.getProvider());

        try {
            // Get social login result based on provider
            SocialLoginResult socialResult = getSocialLoginResult(request);
            
            if (!socialResult.isSuccess()) {
                throw new ValidationException("Social login failed: " + socialResult.getErrorMessage());
            }

            // Find or create user
            User user = findOrCreateUser(socialResult);
            
            // Find or create social account
            SocialUserAccount socialAccount = findOrCreateSocialAccount(user, socialResult);
            
            // Update last login
            socialAccount.markAsUsed();
            socialUserAccountRepository.save(socialAccount);

            // Generate JWT token
            String token = generateTokenForUser(user);

            // Build response
            SocialLoginResponse response = buildSocialLoginResponse(user, socialAccount, token, false);
            
            log.info("✅ Social login successful for user: {} via {}", 
                    user.getUserIdentifier(), request.getProvider());
            
            return response;

        } catch (Exception e) {
            log.error("❌ Social login failed for provider {}: {}", request.getProvider(), e.getMessage(), e);
            throw new ValidationException("Social login failed: " + e.getMessage());
        }
    }

    /**
     * Link social account to existing user
     */
    public SocialAccountResponse linkSocialAccount(LinkSocialAccountRequest request) {
        log.info("🔗 Linking social account with provider: {}", request.getProvider());

        try {
            User currentUser = securityUtils.getCurrentUser();
            
            // Get social login result
            SocialLoginRequest socialRequest = new SocialLoginRequest();
            socialRequest.setProvider(request.getProvider());
            socialRequest.setCode(request.getCode());
            socialRequest.setTelegramData(request.getTelegramData());
            
            SocialLoginResult socialResult = getSocialLoginResult(socialRequest);
            
            if (!socialResult.isSuccess()) {
                throw new ValidationException("Social verification failed: " + socialResult.getErrorMessage());
            }

            // Check if account is already linked to another user
            Optional<SocialUserAccount> existing = socialUserAccountRepository
                    .findByProviderAndProviderIdAndIsDeletedFalse(
                            socialResult.getProvider(), socialResult.getProviderId());
            
            if (existing.isPresent() && !existing.get().getUserId().equals(currentUser.getId())) {
                throw new ValidationException("This social account is already linked to another user");
            }

            // Create or update social account
            SocialUserAccount socialAccount = existing.orElse(new SocialUserAccount());
            socialAccount.setUserId(currentUser.getId());
            socialAccount.setProvider(socialResult.getProvider());
            socialAccount.setProviderId(socialResult.getProviderId());
            socialAccount.setProviderUsername(socialResult.getUsername());
            socialAccount.setProviderEmail(socialResult.getEmail());
            socialAccount.setProviderName(socialResult.getName());
            socialAccount.setProviderPictureUrl(socialResult.getPictureUrl());
            socialAccount.setProviderData(socialResult.getProviderData());
            socialAccount.setLastLoginAt(LocalDateTime.now());
            
            // Set as primary if requested
            if (Boolean.TRUE.equals(request.getMakePrimary())) {
                // Remove primary from other accounts
                socialUserAccountRepository.findByUserIdAndIsDeletedFalse(currentUser.getId())
                        .forEach(account -> {
                            account.setIsPrimary(false);
                            socialUserAccountRepository.save(account);
                        });
                socialAccount.setIsPrimary(true);
            }

            socialAccount = socialUserAccountRepository.save(socialAccount);

            log.info("✅ Social account linked successfully for user: {} via {}", 
                    currentUser.getUserIdentifier(), request.getProvider());

            return mapToSocialAccountResponse(socialAccount);

        } catch (Exception e) {
            log.error("❌ Failed to link social account: {}", e.getMessage(), e);
            throw new ValidationException("Failed to link social account: " + e.getMessage());
        }
    }

    /**
     * Get user's linked social accounts
     */
    @Transactional(readOnly = true)
    public List<SocialAccountResponse> getUserSocialAccounts() {
        User currentUser = securityUtils.getCurrentUser();
        
        return socialUserAccountRepository.findByUserIdAndIsDeletedFalse(currentUser.getId())
                .stream()
                .map(this::mapToSocialAccountResponse)
                .collect(Collectors.toList());
    }

    /**
     * Unlink social account
     */
    public void unlinkSocialAccount(UUID socialAccountId) {
        User currentUser = securityUtils.getCurrentUser();
        
        SocialUserAccount socialAccount = socialUserAccountRepository.findById(socialAccountId)
                .orElseThrow(() -> new ValidationException("Social account not found"));
        
        if (!socialAccount.getUserId().equals(currentUser.getId())) {
            throw new ValidationException("You can only unlink your own social accounts");
        }
        
        // Check if this is the only way to login
        long socialAccountCount = socialUserAccountRepository.countByUserIdAndIsDeletedFalse(currentUser.getId());
        boolean hasPassword = currentUser.getPassword() != null && !currentUser.getPassword().isEmpty();
        
        if (socialAccountCount == 1 && !hasPassword) {
            throw new ValidationException("Cannot unlink the only login method. Please set a password first or link another social account.");
        }
        
        socialAccount.softDelete();
        socialUserAccountRepository.save(socialAccount);
        
        log.info("✅ Social account unlinked for user: {} provider: {}", 
                currentUser.getUserIdentifier(), socialAccount.getProvider());
    }

    // ================================
    // PRIVATE HELPER METHODS
    // ================================

    private SocialLoginResult getSocialLoginResult(SocialLoginRequest request) {
        return switch (request.getProvider()) {
            case GOOGLE -> {
                if (request.getCode() == null || request.getCode().trim().isEmpty()) {
                    yield SocialLoginResult.failure("Google authorization code is required");
                }
                yield googleOAuth2Service.processGoogleLogin(request.getCode(), request.getRedirectUri());
            }
            case TELEGRAM -> {
                if (request.getTelegramData() == null) {
                    yield SocialLoginResult.failure("Telegram login data is required");
                }
                yield telegramLoginService.verifyTelegramLogin(request.getTelegramData());
            }
            case LOCAL -> SocialLoginResult.failure("Local provider not supported for social login");
        };
    }

    private User findOrCreateUser(SocialLoginResult socialResult) {
        // Try to find existing social account
        Optional<SocialUserAccount> existingSocial = socialUserAccountRepository
                .findWithUserByProviderAndProviderId(socialResult.getProvider(), socialResult.getProviderId());
        
        if (existingSocial.isPresent()) {
            User user = existingSocial.get().getUser();
            securityUtils.validateAccountStatus(user);
            return user;
        }

        // Try to find user by email (if provided and it's Google)
        if (socialResult.getProvider() == SocialProvider.GOOGLE && socialResult.getEmail() != null) {
            Optional<User> existingUser = userRepository.findByEmailAndIsDeletedFalse(socialResult.getEmail());
            if (existingUser.isPresent()) {
                User user = existingUser.get();
                securityUtils.validateAccountStatus(user);
                return user;
            }
        }

        // Create new customer user for social login
        return createNewCustomerFromSocial(socialResult);
    }

    private User createNewCustomerFromSocial(SocialLoginResult socialResult) {
        log.info("🆕 Creating new customer from social login: {}", socialResult.getProvider());

        // Generate unique userIdentifier
        String baseIdentifier = generateUserIdentifierFromSocial(socialResult);
        String userIdentifier = ensureUniqueUserIdentifier(baseIdentifier);

        User user = new User();
        user.setUserIdentifier(userIdentifier);
        user.setEmail(socialResult.getEmail()); // Can be null for Telegram
        user.setPassword(null); // No password for social-only users
        user.setUserType(UserType.CUSTOMER);
        user.setAccountStatus(AccountStatus.ACTIVE);
        
        // Set name from social data
        if (socialResult.getName() != null && !socialResult.getName().trim().isEmpty()) {
            String[] nameParts = socialResult.getName().trim().split("\\s+", 2);
            user.setFirstName(nameParts[0]);
            if (nameParts.length > 1) {
                user.setLastName(nameParts[1]);
            }
        }
        
        user.setProfileImageUrl(socialResult.getPictureUrl());

        // Set customer role
        Role customerRole = roleRepository.findByName(RoleEnum.CUSTOMER)
                .orElseThrow(() -> new ValidationException("Customer role not found"));
        user.setRoles(List.of(customerRole));

        user = userRepository.save(user);
        
        log.info("✅ New customer created from social login: {} ({})", 
                userIdentifier, socialResult.getProvider());
        
        return user;
    }

    private String generateUserIdentifierFromSocial(SocialLoginResult socialResult) {
        // For Google, prefer email prefix
        if (socialResult.getProvider() == SocialProvider.GOOGLE && socialResult.getEmail() != null) {
            String emailPrefix = socialResult.getEmail().split("@")[0];
            return cleanUserIdentifier(emailPrefix);
        }
        
        // For Telegram, prefer username
        if (socialResult.getProvider() == SocialProvider.TELEGRAM && socialResult.getUsername() != null) {
            return cleanUserIdentifier(socialResult.getUsername());
        }
        
        // Fallback to provider + ID
        return socialResult.getProvider().name().toLowerCase() + "_" + socialResult.getProviderId();
    }

    private String cleanUserIdentifier(String input) {
        return input.toLowerCase()
                .replaceAll("[^a-z0-9]", "")
                .substring(0, Math.min(input.length(), 20));
    }

    private String ensureUniqueUserIdentifier(String baseIdentifier) {
        String candidate = baseIdentifier;
        int counter = 1;
        
        while (userRepository.existsByUserIdentifierAndIsDeletedFalse(candidate)) {
            candidate = baseIdentifier + counter;
            counter++;
            
            if (counter > 999) { // Prevent infinite loop
                candidate = baseIdentifier + System.currentTimeMillis() % 10000;
                break;
            }
        }
        
        return candidate;
    }

    private SocialUserAccount findOrCreateSocialAccount(User user, SocialLoginResult socialResult) {
        Optional<SocialUserAccount> existing = socialUserAccountRepository
                .findByProviderAndProviderIdAndIsDeletedFalse(
                        socialResult.getProvider(), socialResult.getProviderId());
        
        if (existing.isPresent()) {
            SocialUserAccount account = existing.get();
            // Update provider data
            account.setProviderUsername(socialResult.getUsername());
            account.setProviderEmail(socialResult.getEmail());
            account.setProviderName(socialResult.getName());
            account.setProviderPictureUrl(socialResult.getPictureUrl());
            account.setProviderData(socialResult.getProviderData());
            return account;
        }

        // Create new social account
        SocialUserAccount socialAccount = new SocialUserAccount();
        socialAccount.setUserId(user.getId());
        socialAccount.setProvider(socialResult.getProvider());
        socialAccount.setProviderId(socialResult.getProviderId());
        socialAccount.setProviderUsername(socialResult.getUsername());
        socialAccount.setProviderEmail(socialResult.getEmail());
        socialAccount.setProviderName(socialResult.getName());
        socialAccount.setProviderPictureUrl(socialResult.getPictureUrl());
        socialAccount.setProviderData(socialResult.getProviderData());
        
        // Set as primary if user has no other social accounts
        boolean hasOtherAccounts = socialUserAccountRepository.existsByUserIdAndIsDeletedFalse(user.getId());
        socialAccount.setIsPrimary(!hasOtherAccounts);
        
        return socialUserAccountRepository.save(socialAccount);
    }

    private String generateTokenForUser(User user) {
        List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName().name()))
                .collect(Collectors.toList());

        Authentication auth = new UsernamePasswordAuthenticationToken(
                user.getUserIdentifier(), null, authorities);
        
        return jwtGenerator.generateAccessToken(auth);
    }

    private SocialLoginResponse buildSocialLoginResponse(User user, SocialUserAccount socialAccount, 
                                                       String token, boolean isNewUser) {
        return SocialLoginResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .userIdentifier(user.getUserIdentifier())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .profileImageUrl(socialAccount.getProviderPictureUrl())
                .userType(user.getUserType())
                .roles(user.getRoles().stream()
                        .map(role -> role.getName().name())
                        .collect(Collectors.toList()))
                .businessId(user.getBusinessId())
                .businessName(user.getBusiness() != null ? user.getBusiness().getName() : null)
                .welcomeMessage("Welcome back, " + socialAccount.getDisplayName() + "!")
                .provider(socialAccount.getProvider())
                .isNewUser(isNewUser)
                .accountLinked(true)
                .socialUsername(socialAccount.getProviderUsername())
                .build();
    }

    private SocialAccountResponse mapToSocialAccountResponse(SocialUserAccount account) {
        SocialAccountResponse response = new SocialAccountResponse();
        response.setId(account.getId());
        response.setProvider(account.getProvider());
        response.setProviderId(account.getProviderId());
        response.setProviderUsername(account.getProviderUsername());
        response.setProviderEmail(account.getProviderEmail());
        response.setProviderName(account.getProviderName());
        response.setProviderPictureUrl(account.getProviderPictureUrl());
        response.setIsPrimary(account.getIsPrimary());
        response.setLastLoginAt(account.getLastLoginAt());
        response.setCreatedAt(account.getCreatedAt());
        return response;
    }
}