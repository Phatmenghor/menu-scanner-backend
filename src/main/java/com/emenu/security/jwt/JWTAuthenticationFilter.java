package com.emenu.security.jwt;

import com.emenu.security.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JWTAuthenticationFilter extends OncePerRequestFilter {

    private final JWTGenerator tokenGenerator;
    private final CustomUserDetailsService userDetailsService;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String token = getJWTFromRequest(request);
        
        if (StringUtils.hasText(token)) {
            try {
                // First check if token is valid
                if (!tokenGenerator.validateToken(token)) {
                    log.debug("Invalid JWT token for request: {}", request.getRequestURI());
                    filterChain.doFilter(request, response);
                    return;
                }

                // Check if token is blacklisted
                if (tokenBlacklistService.isTokenBlacklisted(token)) {
                    log.debug("Rejected blacklisted token for request: {}", request.getRequestURI());
                    filterChain.doFilter(request, response);
                    return;
                }

                // Extract username and set authentication
                String username = tokenGenerator.getUsernameFromJWT(token);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                
                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                log.debug("Authentication set for user: {} on request: {}", username, request.getRequestURI());
                
            } catch (Exception e) {
                log.warn("Failed to set authentication for token on request {}: {}", 
                        request.getRequestURI(), e.getMessage());
                SecurityContextHolder.clearContext();
            }
        }
        
        filterChain.doFilter(request, response);
    }

    private String getJWTFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}