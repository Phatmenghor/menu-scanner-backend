package com.menghor.ksit.feature.auth.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * JWT Authentication Entry Point
 * Handles unauthenticated requests to protected resources
 */
@Component
@Slf4j
public class JwtAuthEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        log.error("Unauthorized access attempt to: {} - {}",
                request.getRequestURI(), authException.getMessage());

        // Set response properties
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        // Simple JSON response for JWT authentication failures
        String jsonResponse = String.format(
                "{\"statusCode\": 401, \"message\": \"Authentication required. Please provide a valid token\", \"timestamp\": \"%s\", \"path\": \"%s\"}",
                new java.util.Date().toString(),
                request.getRequestURI()
        );

        response.getWriter().write(jsonResponse);
    }
}