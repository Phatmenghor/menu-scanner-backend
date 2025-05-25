package com.menghor.ksit.feature.auth.security;

import com.menghor.ksit.feature.auth.models.UserEntity;
import com.menghor.ksit.feature.auth.repository.BlacklistedTokenRepository;
import com.menghor.ksit.feature.auth.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class JWTAuthenticationFilter extends OncePerRequestFilter {

    private final JWTGenerator tokenGenerator;
    private final CustomUserDetailsService customUserDetailsService;
    private final UserRepository userRepository;
    private final BlacklistedTokenRepository blacklistedTokenRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = getJWTFromRequest(request);
            log.debug("Token from request: {}", token);

            // Check if token is blacklisted
            if (StringUtils.hasText(token) &&
                    !blacklistedTokenRepository.existsByToken(token) &&
                    tokenGenerator.validateToken(token)) {

                String username = tokenGenerator.getUsernameFromJWT(token);
                log.debug("Username from token: {}", username);

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

                } catch (org.springframework.security.core.userdetails.UsernameNotFoundException ex) {
                    log.error("Authentication failed for user {}: {}", username, ex.getMessage());
                    SecurityContextHolder.clearContext();
                } catch (DisabledException ex) {
                    log.error("Account disabled for user {}: {}", username, ex.getMessage());
                    SecurityContextHolder.clearContext();
                } catch (LockedException ex) {
                    log.error("Account locked for user {}: {}", username, ex.getMessage());
                    SecurityContextHolder.clearContext();
                }
            }

            filterChain.doFilter(request, response);

        } catch (Exception e) {
            log.error("Authentication process failed", e);
            SecurityContextHolder.clearContext();
            throw e;
        }
    }

    private String getJWTFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        log.debug("Authorization header: {}", bearerToken);

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}