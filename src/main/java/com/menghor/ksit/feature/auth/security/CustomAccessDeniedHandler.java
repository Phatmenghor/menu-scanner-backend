package com.menghor.ksit.feature.auth.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.menghor.ksit.feature.auth.models.UserEntity;
import com.menghor.ksit.feature.auth.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
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
        // Get current authentication
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Prepare response
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Default error response
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("code", HttpStatus.FORBIDDEN.value());
        errorResponse.put("status", "failed");

        // Check if we have a user in the context
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            // Get username
            String username = authentication.getName();

            try {
                // Find the user entity with roles
                Optional<UserEntity> userOpt = userRepository.findByUsername(username);

                if (userOpt.isPresent()) {
                    UserEntity user = userOpt.get();
                    log.warn("Access denied to user {} (roles: {}) for path: {}",
                            username,
                            user.getRoles().stream().map(r -> r.getName().name()).toList(),
                            request.getRequestURI());

                    errorResponse.put("message", "You don't have permission to access this resource");
                } else {
                    errorResponse.put("message", "User not found");
                }

                response.setStatus(HttpStatus.FORBIDDEN.value());

            } catch (Exception e) {
                // Error finding user
                errorResponse.put("message", "Authentication error");
                response.setStatus(HttpStatus.FORBIDDEN.value());
                log.error("Error processing access denied for user {}", username, e);
            }
        } else {
            // No authentication details
            errorResponse.put("message", "Authentication required");
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
        }

        // Write the response
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}