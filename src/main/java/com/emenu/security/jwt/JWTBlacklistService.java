package com.emenu.security.jwt;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class JWTBlacklistService {
    
    private final ConcurrentHashMap<String, LocalDateTime> blacklistedTokens = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public JWTBlacklistService() {
        // Clean up expired tokens every hour
        scheduler.scheduleAtFixedRate(this::cleanupExpiredTokens, 1, 1, TimeUnit.HOURS);
    }
    
    /**
     * Add token to blacklist
     */
    public void blacklistToken(String token) {
        try {
            String tokenHash = hashToken(token);
            blacklistedTokens.put(tokenHash, LocalDateTime.now());
            log.info("Token blacklisted successfully");
        } catch (Exception e) {
            log.error("Error blacklisting token: {}", e.getMessage());
        }
    }
    
    /**
     * Check if token is blacklisted
     */
    public boolean isTokenBlacklisted(String token) {
        try {
            String tokenHash = hashToken(token);
            return blacklistedTokens.containsKey(tokenHash);
        } catch (Exception e) {
            log.error("Error checking token blacklist status: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Clean up expired tokens (older than 24 hours)
     */
    private void cleanupExpiredTokens() {
        try {
            LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
            blacklistedTokens.entrySet().removeIf(entry -> entry.getValue().isBefore(cutoff));
            log.debug("Cleaned up expired blacklisted tokens");
        } catch (Exception e) {
            log.error("Error cleaning up expired tokens: {}", e.getMessage());
        }
    }
    
    /**
     * Hash token for storage (simple implementation)
     */
    private String hashToken(String token) {
        return String.valueOf(token.hashCode());
    }
    
    /**
     * Get total blacklisted tokens count
     */
    public int getBlacklistedTokensCount() {
        return blacklistedTokens.size();
    }
    
    /**
     * Clear all blacklisted tokens (for admin use)
     */
    public void clearAllBlacklistedTokens() {
        blacklistedTokens.clear();
        log.info("All blacklisted tokens cleared");
    }
}