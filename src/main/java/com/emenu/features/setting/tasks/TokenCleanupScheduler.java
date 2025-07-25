package com.emenu.features.setting.tasks;

import com.emenu.security.jwt.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(value = "app.security.token-cleanup.enabled", havingValue = "true", matchIfMissing = true)
public class TokenCleanupScheduler {

    private final TokenBlacklistService tokenBlacklistService;

    /**
     * Clean up expired tokens every day at 2 AM
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupExpiredTokens() {
        log.info("Starting scheduled cleanup of expired blacklisted tokens");
        
        try {
            TokenBlacklistService.BlacklistStats statsBefore = tokenBlacklistService.getBlacklistStats();
            log.info("Blacklist stats before cleanup: {}", statsBefore);
            
            int cleanedCount = tokenBlacklistService.cleanupExpiredTokens();
            
            TokenBlacklistService.BlacklistStats statsAfter = tokenBlacklistService.getBlacklistStats();
            log.info("Blacklist stats after cleanup: {}", statsAfter);
            
            if (cleanedCount > 0) {
                log.info("Successfully cleaned up {} expired blacklisted tokens", cleanedCount);
            } else {
                log.debug("No expired tokens found for cleanup");
            }
            
        } catch (Exception e) {
            log.error("Failed to execute scheduled token cleanup", e);
        }
    }

    /**
     * Log blacklist statistics every hour for monitoring
     */
    @Scheduled(fixedRate = 3600000) // Every hour
    public void logBlacklistStats() {
        try {
            TokenBlacklistService.BlacklistStats stats = tokenBlacklistService.getBlacklistStats();
            
            if (stats.totalTokens > 0) {
                log.info("Blacklist monitoring - {}", stats);
                
                // Alert if too many tokens are accumulating
                if (stats.totalTokens > 10000) {
                    log.warn("High number of blacklisted tokens detected: {}. Consider checking cleanup process.", 
                            stats.totalTokens);
                }
            }
        } catch (Exception e) {
            log.error("Failed to log blacklist statistics", e);
        }
    }
}