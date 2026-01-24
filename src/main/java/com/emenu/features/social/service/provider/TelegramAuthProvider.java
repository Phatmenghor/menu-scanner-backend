package com.emenu.features.social.service.provider;

import com.emenu.exception.custom.ValidationException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelegramAuthProvider {

    @Value("${app.social.telegram.bot-token:}")
    private String botToken;

    private final ObjectMapper objectMapper;

    public SocialUserInfo getUserInfo(String authData) {
        try {
            JsonNode data = objectMapper.readTree(austhData);
            
            String id = data.get("id").asText();
            String username = data.has("username") ? data.get("username").asText() : null;
            String firstName = data.has("first_name") ? data.get("first_name").asText() : null;
            String lastName = data.has("last_name") ? data.get("last_name").asText() : null;
            
            String hash = data.has("hash") ? data.get("hash").asText() : null;
            if (hash != null && !botToken.isEmpty()) {
                verifyTelegramAuth(data, hash);
            }

            return new SocialUserInfo(id, username, null, firstName, lastName);
        } catch (Exception e) {
            log.error("Failed to parse Telegram auth data", e);
            throw new ValidationException("Invalid Telegram authentication data");
        }
    }

    private void verifyTelegramAuth(JsonNode data, String hash) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] secretKey = digest.digest(botToken.getBytes(StandardCharsets.UTF_8));
            
            StringBuilder checkString = new StringBuilder();
            data.fields().forEachRemaining(entry -> {
                if (!"hash".equals(entry.getKey())) {
                    checkString.append(entry.getKey()).append("=").append(entry.getValue().asText()).append("\n");
                }
            });
            
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secretKey, "HmacSHA256"));
            byte[] hmac = mac.doFinal(checkString.toString().getBytes(StandardCharsets.UTF_8));
            String computedHash = bytesToHex(hmac);
            
            if (!computedHash.equals(hash)) {
                throw new ValidationException("Invalid Telegram authentication hash");
            }
        } catch (Exception e) {
            log.error("Failed to verify Telegram auth", e);
            throw new ValidationException("Failed to verify Telegram authentication");
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}
