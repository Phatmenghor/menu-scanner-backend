package com.menghor.ksit.feature.auth.service.impl;

import com.menghor.ksit.feature.auth.models.BlacklistedTokenEntity;
import com.menghor.ksit.feature.auth.repository.BlacklistedTokenRepository;
import com.menghor.ksit.feature.auth.security.JWTGenerator;
import com.menghor.ksit.feature.auth.service.LogoutService;
import com.menghor.ksit.utils.database.SecurityUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
public class LogoutServiceImpl implements LogoutService {

    private final BlacklistedTokenRepository blacklistedTokenRepository;
    private final SecurityUtils securityUtils;
    private final JWTGenerator jwtGenerator;

    @Value("${jwt.secret.key}")
    private String secretKey;

    @Override
    @Transactional
    public void logout(String token) {
        log.info("Initiating logout process");

        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(jwtGenerator.getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            Date expirationDate = claims.getExpiration();
            String username = claims.getSubject();

            BlacklistedTokenEntity blacklistedTokenEntity = BlacklistedTokenEntity.builder()
                    .token(token)
                    .username(username)
                    .createdAt(LocalDateTime.now())
                    .expirationDate(LocalDateTime.ofInstant(expirationDate.toInstant(), ZoneId.systemDefault()))
                    .build();

            blacklistedTokenRepository.save(blacklistedTokenEntity);
            SecurityContextHolder.clearContext();

            log.info("Logout successful. Token blacklisted for user: {}", username);
        } catch (Exception e) {
            log.error("Logout failed: Invalid or malformed token", e);
        }
    }

    /**
     * Scheduled task to remove expired blacklisted tokens
     * Runs every day at midnight
     */
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void removeExpiredBlacklistedTokens() {
        LocalDateTime now = LocalDateTime.now();
        int deletedCount = blacklistedTokenRepository.deleteExpiredTokens(now);
        log.info("Scheduled cleanup: {} expired blacklisted tokens removed at {}", deletedCount, now);
    }
}
