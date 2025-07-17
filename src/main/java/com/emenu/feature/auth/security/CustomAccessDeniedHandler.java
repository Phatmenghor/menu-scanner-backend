package com.emenu.feature.auth.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.emenu.feature.auth.models.UserEntity;
import com.emenu.feature.auth.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {

        String requestURI = request.getRequestURI();
        String method = request.getMethod();

        // Get current authentication
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Prepare response
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpStatus.FORBIDDEN.value());

        // Default error response
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", "error");
        errorResponse.put("statusCode", HttpStatus.FORBIDDEN.value());
        errorResponse.put("timestamp", LocalDateTime.now().toString());
        errorResponse.put("path", requestURI);
        errorResponse.put("method", method);

        // Check if we have a user in the context
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            // Get username
            String username = authentication.getName();

            try {
                // Find the user entity with roles
                Optional<UserEntity> userOpt = userRepository.findByUsername(username);

                if (userOpt.isPresent()) {
                    UserEntity user = userOpt.get();
                    List<String> userRoles = user.getRoles().stream()
                            .map(r -> r.getName().name())
                            .toList();

                    log.warn("Access denied for user {} (roles: {}) accessing {} {}",
                            username, userRoles, method, requestURI);

                    errorResponse.put("message",
                            "Access denied. You don't have sufficient permissions to access this resource.");

                    // Add user context for better debugging (only in development)
                    if (isDevelopmentMode()) {
                        errorResponse.put("user", username);
                        errorResponse.put("userRoles", userRoles);
                        errorResponse.put("requiredPermissions", "Contact administrator for required permissions");
                    }
                } else {
                    log.warn("Access denied for unknown user: {}", username);
                    errorResponse.put("message", "Access denied. User authentication invalid.");
                }

            } catch (Exception e) {
                // Error finding user
                log.error("Error processing access denied for user {}: {}", username, e.getMessage());
                errorResponse.put("message", "Access denied. Unable to verify user permissions.");
            }
        } else {
            // No authentication details or wrong principal type
            log.warn("Access denied for unauthenticated request to {} {}", method, requestURI);
            errorResponse.put("message", "Access denied. Authentication required to access this resource.");
        }

        // Write the response
        String jsonResponse = objectMapper.writeValueAsString(errorResponse);
        response.getWriter().write(jsonResponse);
    }

    private boolean isDevelopmentMode() {
        // Check if we're in development mode
        String profile = System.getProperty("spring.profiles.active");
        return "dev".equals(profile) || "development".equals(profile);
    }
}