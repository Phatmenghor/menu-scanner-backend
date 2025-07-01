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
        String actionMessage = determineActionMessage(request);

        // Create detailed JSON response
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", "error");
        errorResponse.put("message", errorMessage);
        errorResponse.put("action", actionMessage);
        errorResponse.put("timestamp", LocalDateTime.now().toString());
        errorResponse.put("path", requestURI);
        errorResponse.put("statusCode", HttpServletResponse.SC_UNAUTHORIZED);

        // Add additional context for debugging (only in development)
        if (isDevelopmentMode()) {
            errorResponse.put("method", method);
            errorResponse.put("exception", authException.getClass().getSimpleName());
            errorResponse.put("exceptionMessage", authException.getMessage());
        }

        String jsonResponse = objectMapper.writeValueAsString(errorResponse);
        response.getWriter().write(jsonResponse);
    }

    private String determineErrorMessage(AuthenticationException authException, HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        // No Authorization header provided
        if (authHeader == null || authHeader.trim().isEmpty()) {
            return "Authentication required. You must be logged in to access this resource.";
        }

        // Invalid Authorization header format
        if (!authHeader.startsWith("Bearer ")) {
            return "Invalid authentication format. Please provide a valid Bearer token in the Authorization header.";
        }

        // Authorization header present but token is invalid/expired
        String token = authHeader.substring(7).trim();
        if (token.isEmpty()) {
            return "Authentication token is missing from the request.";
        }

        // Token present but authentication failed - could be expired, invalid, etc.
        if (authException.getMessage() != null) {
            String message = authException.getMessage().toLowerCase();

            if (message.contains("expired")) {
                return "Your session has expired. Please login again to continue.";
            } else if (message.contains("invalid") || message.contains("malformed")) {
                return "Your authentication token is invalid or corrupted. Please login again.";
            } else if (message.contains("signature")) {
                return "Authentication token signature is invalid. Please login again.";
            }
        }

        // Generic token authentication failure
        return "Authentication failed. Your session may have expired or the token is invalid. Please login again.";
    }

    private String determineActionMessage(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || authHeader.trim().isEmpty()) {
            return "Please login to access this resource";
        } else if (!authHeader.startsWith("Bearer ")) {
            return "Please provide a valid authentication token";
        } else {
            return "Please login again to continue";
        }
    }

    private boolean isDevelopmentMode() {
        // Check if we're in development mode
        String profile = System.getProperty("spring.profiles.active");
        return "dev".equals(profile) || "development".equals(profile);
    }
}