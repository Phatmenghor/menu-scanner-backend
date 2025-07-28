package com.emenu.features.auth.service.social;

import com.emenu.enums.auth.SocialProvider;
import com.emenu.features.auth.dto.request.SocialLoginResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleOAuth2Service {

    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String googleClientSecret;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String googleRedirectUri;

    private static final String GOOGLE_TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String GOOGLE_USERINFO_URL = "https://www.googleapis.com/oauth2/v2/userinfo";

    /**
     * Exchange authorization code for access token and get user info
     */
    public SocialLoginResult processGoogleLogin(String code, String redirectUri) {
        log.info("🔐 Processing Google OAuth2 login with code");

        try {
            // Step 1: Exchange code for access token
            String accessToken = exchangeCodeForToken(code, redirectUri);
            if (accessToken == null) {
                return SocialLoginResult.failure("Failed to exchange code for access token");
            }

            // Step 2: Get user information
            JsonNode userInfo = getUserInfo(accessToken);
            if (userInfo == null) {
                return SocialLoginResult.failure("Failed to get user information from Google");
            }

            // Step 3: Extract user data
            String providerId = userInfo.get("id").asText();
            String email = userInfo.has("email") ? userInfo.get("email").asText() : null;
            String name = userInfo.has("name") ? userInfo.get("name").asText() : null;
            String pictureUrl = userInfo.has("picture") ? userInfo.get("picture").asText() : null;
            
            // Build full name from given_name and family_name if name is not available
            if (name == null) {
                String givenName = userInfo.has("given_name") ? userInfo.get("given_name").asText() : "";
                String familyName = userInfo.has("family_name") ? userInfo.get("family_name").asText() : "";
                name = (givenName + " " + familyName).trim();
                if (name.isEmpty()) name = email;
            }

            SocialLoginResult result = SocialLoginResult.builder()
                    .success(true)
                    .provider(SocialProvider.GOOGLE)
                    .providerId(providerId)
                    .email(email)
                    .name(name)
                    .username(email) // Use email as username for Google
                    .pictureUrl(pictureUrl)
                    .providerData(userInfo.toString())
                    .build();

            log.info("✅ Google login processed successfully for user: {} ({})", name, email);
            return result;

        } catch (Exception e) {
            log.error("❌ Failed to process Google login: {}", e.getMessage(), e);
            return SocialLoginResult.failure("Google authentication failed: " + e.getMessage());
        }
    }

    /**
     * Exchange authorization code for access token
     */
    private String exchangeCodeForToken(String code, String redirectUri) {
        try {
            WebClient webClient = webClientBuilder.build();

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("code", code);
            params.add("client_id", googleClientId);
            params.add("client_secret", googleClientSecret);
            params.add("redirect_uri", redirectUri != null ? redirectUri : googleRedirectUri);
            params.add("grant_type", "authorization_code");

            Mono<String> response = webClient
                    .post()
                    .uri(GOOGLE_TOKEN_URL)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                    .body(BodyInserters.fromFormData(params))
                    .retrieve()
                    .bodyToMono(String.class);

            String responseBody = response.block();
            
            if (responseBody == null) {
                log.error("Empty response from Google token endpoint");
                return null;
            }

            JsonNode tokenResponse = objectMapper.readTree(responseBody);
            
            if (tokenResponse.has("access_token")) {
                return tokenResponse.get("access_token").asText();
            } else {
                log.error("No access token in Google response: {}", responseBody);
                return null;
            }

        } catch (Exception e) {
            log.error("Failed to exchange code for token: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Get user information using access token
     */
    private JsonNode getUserInfo(String accessToken) {
        try {
            WebClient webClient = webClientBuilder.build();

            Mono<String> response = webClient
                    .get()
                    .uri(GOOGLE_USERINFO_URL)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(String.class);

            String responseBody = response.block();
            
            if (responseBody == null) {
                log.error("Empty response from Google userinfo endpoint");
                return null;
            }

            return objectMapper.readTree(responseBody);

        } catch (Exception e) {
            log.error("Failed to get user info from Google: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Get Google OAuth2 authorization URL
     */
    public String getGoogleAuthUrl(String state, String redirectUri) {
        String actualRedirectUri = redirectUri != null ? redirectUri : googleRedirectUri;
        
        return "https://accounts.google.com/o/oauth2/auth?" +
               "client_id=" + googleClientId +
               "&redirect_uri=" + actualRedirectUri +
               "&scope=openid email profile" +
               "&response_type=code" +
               "&access_type=offline" +
               "&state=" + (state != null ? state : "") +
               "&prompt=select_account";
    }
}