package com.emenu.features.auth.service.social;

import com.emenu.enums.auth.SocialProvider;
import com.emenu.features.auth.dto.request.SocialLoginResult;
import com.emenu.features.auth.dto.request.TelegramLoginData;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Map;
import java.util.TreeMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelegramLoginService {

    private final ObjectMapper objectMapper;

    @Value("${app.social.telegram.bot-token}")
    private String telegramBotToken;

    @Value("${app.social.telegram.bot-username}")
    private String telegramBotUsername;

    /**
     * Verify Telegram login data
     */
    public SocialLoginResult verifyTelegramLogin(TelegramLoginData loginData) {
        log.info("🔐 Verifying Telegram login for user: {}", loginData.getUsername());

        try {
            // Validate required fields
            if (!isValidTelegramData(loginData)) {
                return SocialLoginResult.failure("Invalid Telegram login data");
            }

            // Verify the hash
            if (!verifyTelegramHash(loginData)) {
                return SocialLoginResult.failure("Invalid Telegram hash verification");
            }

            // Check auth date (not older than 1 day)
            if (!isAuthDateValid(loginData.getAuthDate())) {
                return SocialLoginResult.failure("Telegram auth data is too old");
            }

            // Create social login result
            SocialLoginResult result = SocialLoginResult.builder()
                    .success(true)
                    .provider(SocialProvider.TELEGRAM)
                    .providerId(String.valueOf(loginData.getId()))
                    .email(null) // Telegram doesn't provide email
                    .name(buildFullName(loginData.getFirstName(), loginData.getLastName()))
                    .username(loginData.getUsername())
                    .pictureUrl(loginData.getPhotoUrl())
                    .providerData(serializeProviderData(loginData))
                    .build();

            log.info("✅ Telegram login verified successfully for user: {} (ID: {})", 
                    loginData.getUsername(), loginData.getId());

            return result;

        } catch (Exception e) {
            log.error("❌ Failed to verify Telegram login: {}", e.getMessage(), e);
            return SocialLoginResult.failure("Telegram verification failed: " + e.getMessage());
        }
    }

    /**
     * Validate required Telegram data fields
     */
    private boolean isValidTelegramData(TelegramLoginData data) {
        return data.getId() != null && 
               data.getAuthDate() != null && 
               data.getHash() != null && 
               !data.getHash().trim().isEmpty();
    }

    /**
     * Verify Telegram login hash according to their documentation
     * https://core.telegram.org/widgets/login#checking-authorization
     */
    private boolean verifyTelegramHash(TelegramLoginData data) {
        try {
            // Create data check string (alphabetically sorted parameters except hash)
            TreeMap<String, String> authData = new TreeMap<>();
            
            if (data.getId() != null) {
                authData.put("id", String.valueOf(data.getId()));
            }
            if (data.getFirstName() != null) {
                authData.put("first_name", data.getFirstName());
            }
            if (data.getLastName() != null) {
                authData.put("last_name", data.getLastName());
            }
            if (data.getUsername() != null) {
                authData.put("username", data.getUsername());
            }
            if (data.getPhotoUrl() != null) {
                authData.put("photo_url", data.getPhotoUrl());
            }
            if (data.getAuthDate() != null) {
                authData.put("auth_date", String.valueOf(data.getAuthDate()));
            }

            // Build data check string
            StringBuilder dataCheckString = new StringBuilder();
            for (Map.Entry<String, String> entry : authData.entrySet()) {
                if (dataCheckString.length() > 0) {
                    dataCheckString.append("\n");
                }
                dataCheckString.append(entry.getKey()).append("=").append(entry.getValue());
            }

            // Generate secret key from bot token
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] secretKey = sha256.digest(telegramBotToken.getBytes(StandardCharsets.UTF_8));

            // Generate HMAC-SHA256 hash
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey, "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hashBytes = mac.doFinal(dataCheckString.toString().getBytes(StandardCharsets.UTF_8));

            // Convert to hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            String calculatedHash = hexString.toString();
            boolean isValid = calculatedHash.equals(data.getHash().toLowerCase());

            if (!isValid) {
                log.warn("🔍 Telegram hash verification failed. Expected: {}, Got: {}", 
                        calculatedHash, data.getHash());
                log.debug("🔍 Data check string: {}", dataCheckString.toString());
            }

            return isValid;

        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("❌ Error verifying Telegram hash: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Check if auth date is valid (not older than 24 hours)
     */
    private boolean isAuthDateValid(Long authDate) {
        if (authDate == null) return false;

        long currentTime = Instant.now().getEpochSecond();
        long maxAge = 24 * 60 * 60; // 24 hours in seconds

        return (currentTime - authDate) <= maxAge;
    }

    /**
     * Build full name from first and last name
     */
    private String buildFullName(String firstName, String lastName) {
        if (firstName == null && lastName == null) return null;
        if (firstName == null) return lastName.trim();
        if (lastName == null) return firstName.trim();
        return (firstName.trim() + " " + lastName.trim()).trim();
    }

    /**
     * Serialize provider data as JSON
     */
    private String serializeProviderData(TelegramLoginData data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            log.warn("Failed to serialize Telegram provider data: {}", e.getMessage());
            return "{}";
        }
    }

    /**
     * Get Telegram login widget URL for frontend
     */
    public String getTelegramLoginWidgetUrl(String redirectUrl) {
        return String.format(
            "https://oauth.telegram.org/auth?bot_id=%s&origin=%s&return_to=%s&request_access=write",
            telegramBotUsername, 
            redirectUrl,
            redirectUrl
        );
    }
}