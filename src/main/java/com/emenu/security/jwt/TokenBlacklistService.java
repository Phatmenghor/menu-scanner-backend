package com.emenu.security.jwt;

import com.emenu.features.auth.models.BlacklistedToken;
import com.emenu.features.auth.repository.BlacklistedTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenBlacklistService {

    private final BlacklistedTokenRepository blacklistedTokenRepository;
    private final JWTGenerator jwtGenerator;

    @Value("${jwt.expiration:86400000}")
    private long jwtExpirationTime;

    /**
     * Add token to blacklist with 1 week expiry
     */
    @Transactional
    public void blacklistToken(String token, String userEmail, String reason) {
        try {
            String tokenHash = hashToken(token);
            
            // Check if token is already blacklisted
            if (blacklistedTokenRepository.existsByTokenHash(tokenHash)) {
                log.debug("Token already blacklisted for user: {}", userEmail);
                return;
            }

            // Calculate expiry time (1 week from now)
            LocalDateTime expiresAt = LocalDateTime.now().plusWeeks(1);

            BlacklistedToken blacklistedToken = new BlacklistedToken(
                tokenHash, 
                userEmail, 
                expiresAt, 
                reason != null ? reason : "LOGOUT"
            );

            blacklistedTokenRepository.save(blacklistedToken);
            
            log.info("Token blacklisted for user: {} with reason: {}", userEmail, reason);
            
        } catch (Exception e) {
            log.error("Failed to blacklist token for user: {}", userEmail, e);
            throw new RuntimeException("Failed to blacklist token", e);
        }
    }

    /**
     * Check if token is blacklisted
     */
    @Transactional(readOnly = true)
    public boolean isTokenBlacklisted(String token) {
        try {
            String tokenHash = hashToken(token);
            boolean isBlacklisted = blacklistedTokenRepository.existsByTokenHash(tokenHash);
            
            if (isBlacklisted) {
                log.debug("Blocked blacklisted token: {}...", token.substring(0, Math.min(token.length(), 20)));
            }
            
            return isBlacklisted;
        } catch (Exception e) {
            log.error("Error checking token blacklist status", e);
            return false; // Allow access if there's an error checking
        }
    }

    /**
     * Cleanup expired tokens (older than 1 week)
     */
    @Transactional
    public int cleanupExpiredTokens() {
        try {
            LocalDateTime cutoffTime = LocalDateTime.now().minusWeeks(1);
            
            long expiredCount = blacklistedTokenRepository.countExpiredTokens(cutoffTime);
            if (expiredCount == 0) {
                log.debug("No expired tokens to cleanup");
                return 0;
            }

            int deletedCount = blacklistedTokenRepository.deleteExpiredTokens(cutoffTime);
            log.info("Cleaned up {} expired blacklisted tokens (older than 1 week)", deletedCount);
            
            return deletedCount;
        } catch (Exception e) {
            log.error("Failed to cleanup expired tokens", e);
            return 0;
        }
    }

    /**
     * Blacklist all tokens for a user (admin operation)
     */
    @Transactional
    public void blacklistAllUserTokens(String userEmail, String reason) {
        try {
            int deletedCount = blacklistedTokenRepository.deleteAllTokensForUser(userEmail);
            log.info("Blacklisted all tokens for user: {} (count: {}) with reason: {}", 
                    userEmail, deletedCount, reason);
        } catch (Exception e) {
            log.error("Failed to blacklist all tokens for user: {}", userEmail, e);
            throw new RuntimeException("Failed to blacklist user tokens", e);
        }
    }

    /**
     * Get blacklist statistics
     */
    @Transactional(readOnly = true)
    public BlacklistStats getBlacklistStats() {
        try {
            long totalTokens = blacklistedTokenRepository.count();
            long expiredTokens = blacklistedTokenRepository.countExpiredTokens(LocalDateTime.now().minusWeeks(1));
            long activeTokens = totalTokens - expiredTokens;

            return new BlacklistStats(totalTokens, activeTokens, expiredTokens);
        } catch (Exception e) {
            log.error("Failed to get blacklist statistics", e);
            return new BlacklistStats(0, 0, 0);
        }
    }

    /**
     * Hash token using SHA-256 for security
     */
    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            log.error("SHA-256 algorithm not available", e);
            throw new RuntimeException("Failed to hash token", e);
        }
    }

    /**
     * Statistics holder class
     */
    public static class BlacklistStats {
        public final long totalTokens;
        public final long activeTokens;
        public final long expiredTokens;

        public BlacklistStats(long totalTokens, long activeTokens, long expiredTokens) {
            this.totalTokens = totalTokens;
            this.activeTokens = activeTokens;
            this.expiredTokens = expiredTokens;
        }

        @Override
        public String toString() {
            return String.format("BlacklistStats{total=%d, active=%d, expired=%d}", 
                    totalTokens, activeTokens, expiredTokens);
        }
    }
}