package com.emenu.features.auth.repository;

import com.emenu.features.auth.models.BlacklistedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BlacklistedTokenRepository extends JpaRepository<BlacklistedToken, UUID> {
    
    /**
     * Check if token hash exists in blacklist
     */
    boolean existsByTokenHash(String tokenHash);
    
    /**
     * Delete expired tokens (cleanup job)
     * Tokens older than 1 week will be deleted
     */
    @Modifying
    @Query("DELETE FROM BlacklistedToken bt WHERE bt.expiresAt < :cutoffTime")
    int deleteExpiredTokens(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    /**
     * Count expired tokens (for monitoring)
     */
    @Query("SELECT COUNT(bt) FROM BlacklistedToken bt WHERE bt.expiresAt < :cutoffTime")
    long countExpiredTokens(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    /**
     * Delete all tokens for a specific user (for admin operations)
     */
    @Modifying
    @Query("DELETE FROM BlacklistedToken bt WHERE bt.userEmail = :userEmail")
    int deleteAllTokensForUser(@Param("userEmail") String userEmail);
}