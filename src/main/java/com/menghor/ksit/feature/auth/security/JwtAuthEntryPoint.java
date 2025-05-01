package com.menghor.ksit.feature.auth.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Enumeration;

@Component
@Slf4j
public class JwtAuthEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        // Log detailed request information
        log.error("Unauthorized access attempt", authException);
        log.error("Request URL: {}", request.getRequestURL());
        log.error("Request Method: {}", request.getMethod());

        // Log all headers
        log.error("Request Headers:");
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            log.error("{}: {}", headerName, request.getHeader(headerName));
        }

        // Prepare response
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        String errorMessage = "Authentication failed";

        // Provide more specific error messages based on exception type
        if (authException instanceof UsernameNotFoundException) {
            errorMessage = "User not found or subscription expired";
        } else if (authException instanceof BadCredentialsException) {
            errorMessage = "Invalid credentials";
        } else if (authException instanceof InsufficientAuthenticationException) {
            errorMessage = "Insufficient authentication details";
        }

        // Include the full exception message if available
        if (authException.getMessage() != null) {
            errorMessage += ": " + authException.getMessage();
        }

        String jsonResponse = String.format(
                "{\"code\": 401, \"status\": \"failed\", \"message\": \"%s\"}",
                errorMessage.replace("\"", "'")
        );

        log.error("Sending error response: {}", jsonResponse);
        response.getWriter().write(jsonResponse);
    }
}