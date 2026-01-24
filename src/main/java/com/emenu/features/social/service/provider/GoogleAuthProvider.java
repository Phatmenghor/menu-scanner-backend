package com.emenu.features.social.service.provider;

import com.emenu.exception.custom.ValidationException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleAuthProvider {

    @Value("${app.social.google.client-id:}")
    private String clientId;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper;

    public SocialUserInfo getUserInfo(String accessToken) {
        try {
            String url = "https://www.googleapis.com/oauth2/v2/userinfo";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            JsonNode data = objectMapper.readTree(response.getBody());
            
            String id = data.get("id").asText();
            String email = data.has("email") ? data.get("email").asText() : null;
            String givenName = data.has("given_name") ? data.get("given_name").asText() : null;
            String familyName = data.has("family_name") ? data.get("family_name").asText() : null;
            
            return new SocialUserInfo(id, email, email, givenName, familyName);
        } catch (Exception e) {
            log.error("Failed to fetch Google user info", e);
            throw new ValidationException("Invalid Google access token");
        }
    }
}
