package com.menghor.ksit.feature.auth.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Enhanced JWT Authentication Entry Point
 * Handles unauthenticated requests to protected resources with detailed error responses
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        String requestURI = request.getRequestURI();
        String method = request.getMethod();

        log.warn("Unauthorized access attempt to: {} {} - {}", method, requestURI, authException.getMessage());

        // Set response properties
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        // Determine appropriate error message based on the exception and request
        String errorMessage = determineErrorMessage(authException, request);

        // Create detailed JSON response
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", "error");
        errorResponse.put("message", errorMessage);
        errorResponse.put("timestamp", LocalDateTime.now().toString());
        errorResponse.put("path", requestURI);
        errorResponse.put("statusCode", HttpServletResponse.SC_UNAUTHORIZED);

        // Add additional context for debugging (only in development)
        if (isDevelopmentMode()) {
            errorResponse.put("method", method);
            errorResponse.put("exception", authException.getClass().getSimpleName());
        }

        String jsonResponse = objectMapper.writeValueAsString(errorResponse);
        response.getWriter().write(jsonResponse);
    }

    private String determineErrorMessage(AuthenticationException authException, HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        // No Authorization header provided
        if (authHeader == null || authHeader.trim().isEmpty()) {
            return "Authentication required. Please provide a valid authentication token to access this resource.";
        }

        // Invalid Authorization header format
        if (!authHeader.startsWith("Bearer ")) {
            return "Invalid authentication format. Please provide a Bearer token in the Authorization header.";
        }

        // Authorization header present but token is invalid/expired
        String token = authHeader.substring(7).trim();
        if (token.isEmpty()) {
            return "Authentication token is missing. Please provide a valid token.";
        }

        // Token present but authentication failed
        return "Authentication failed. Your session may have expired or the token is invalid. Please login again.";
    }

    private boolean isDevelopmentMode() {
        // Check if we're in development mode
        String profile = System.getProperty("spring.profiles.active");
        return "dev".equals(profile) || "development".equals(profile);
    }
}