package com.emenu.feature.auth.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.emenu.feature.auth.models.UserEntity;
import com.emenu.feature.auth.repository.BlacklistedTokenRepository;
import com.emenu.feature.auth.repository.UserRepository;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class JWTAuthenticationFilter extends OncePerRequestFilter {

    private final JWTGenerator tokenGenerator;
    private final CustomUserDetailsService customUserDetailsService;
    private final UserRepository userRepository;
    private final BlacklistedTokenRepository blacklistedTokenRepository;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = getJWTFromRequest(request);
            log.debug("Processing request to: {} with token: {}", request.getRequestURI(),
                    token != null ? "present" : "absent");

            if (StringUtils.hasText(token)) {
                // Check if token is blacklisted
                if (blacklistedTokenRepository.existsByToken(token)) {
                    log.warn("Attempted to use blacklisted token");
                    handleAuthenticationError(response,
                            "Your session has been invalidated. Please login again to continue.",
                            HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }

                // Validate and process token
                if (tokenGenerator.validateToken(token)) {
                    String username = tokenGenerator.getUsernameFromJWT(token);
                    log.debug("Token valid for username: {}", username);

                    authenticateUser(username, token, request);
                }
            }

            filterChain.doFilter(request, response);

        } catch (ExpiredJwtException ex) {
            log.warn("JWT token expired for request to: {}", request.getRequestURI());
            handleAuthenticationError(response,
                    "Your session has expired. Please login again to continue accessing the system.",
                    HttpServletResponse.SC_UNAUTHORIZED);
        } catch (MalformedJwtException ex) {
            log.warn("Malformed JWT token for request to: {}", request.getRequestURI());
            handleAuthenticationError(response,
                    "Invalid authentication token format. Please login again.",
                    HttpServletResponse.SC_UNAUTHORIZED);
        } catch (UnsupportedJwtException ex) {
            log.warn("Unsupported JWT token for request to: {}", request.getRequestURI());
            handleAuthenticationError(response,
                    "Unsupported authentication token type. Please login again.",
                    HttpServletResponse.SC_UNAUTHORIZED);
        } catch (JwtException ex) {
            log.warn("JWT error for request to {}: {}", request.getRequestURI(), ex.getMessage());
            handleAuthenticationError(response,
                    "Authentication token is invalid or corrupted. Please login again.",
                    HttpServletResponse.SC_UNAUTHORIZED);
        } catch (Exception ex) {
            log.error("Authentication processing failed for request to {}: {}",
                    request.getRequestURI(), ex.getMessage());
            handleAuthenticationError(response,
                    "An authentication error occurred. Please try again or contact support if the problem persists.",
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void authenticateUser(String username, String token, HttpServletRequest request) {
        try {
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

            authenticationToken.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
            );

            SecurityContextHolder.getContext().setAuthentication(authenticationToken);

            // Store current user in request for reference by other components
            Optional<UserEntity> userOpt = userRepository.findByUsername(username);
            userOpt.ifPresent(user -> request.setAttribute("currentUser", user));

            log.debug("Successfully authenticated user: {}", username);

        } catch (UsernameNotFoundException ex) {
            log.warn("User not found during token authentication: {}", username);
            throw new UsernameNotFoundException("User account no longer exists. Please login again.");
        } catch (DisabledException ex) {
            log.warn("Account disabled for user {}: {}", username, ex.getMessage());
            throw ex; // Re-throw with specific message from CustomUserDetailsService
        } catch (LockedException ex) {
            log.warn("Account locked for user {}: {}", username, ex.getMessage());
            throw ex; // Re-throw with specific message from CustomUserDetailsService
        } catch (Exception ex) {
            log.error("Authentication failed for user {}: {}", username, ex.getMessage());
            SecurityContextHolder.clearContext();
            throw new RuntimeException("Authentication processing failed: " + ex.getMessage());
        }
    }

    private void handleAuthenticationError(HttpServletResponse response, String message, int statusCode)
            throws IOException {
        response.setStatus(statusCode);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", "error");
        errorResponse.put("message", message);
        errorResponse.put("timestamp", LocalDateTime.now().toString());
        errorResponse.put("statusCode", statusCode);

        // Add additional context for different error types
        if (statusCode == HttpServletResponse.SC_UNAUTHORIZED) {
            errorResponse.put("action", "Please login again to continue");
        } else if (statusCode == HttpServletResponse.SC_INTERNAL_SERVER_ERROR) {
            errorResponse.put("action", "Please try again or contact support");
        }

        String jsonResponse = objectMapper.writeValueAsString(errorResponse);
        response.getWriter().write(jsonResponse);
    }

    private String getJWTFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        log.debug("Authorization header: {}", bearerToken != null ? "Bearer ***" : "null");

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        // ONLY skip filter for PUBLIC endpoints that don't need authentication
        // DO NOT skip the token endpoints - they need authentication!
        return path.equals("/api/v1/auth/login") ||
                path.equals("/api/v1/auth/refresh-token") ||
                path.startsWith("/v3/api-docs") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/webjars/") ||
                path.equals("/favicon.ico") ||
                path.startsWith("/static/") ||
                path.equals("/error");
    }
}