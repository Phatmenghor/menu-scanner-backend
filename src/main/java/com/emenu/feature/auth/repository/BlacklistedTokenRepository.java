package com.emenu.feature.auth.repository;

import com.emenu.feature.auth.models.BlacklistedTokenEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface BlacklistedTokenRepository extends JpaRepository<BlacklistedTokenEntity, Long> {
    
    /**
     * Check if a token is blacklisted
     */
    boolean existsByToken(String token);
    
    /**
     * Delete expired tokens
     *
     * @return
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM BlacklistedTokenEntity t WHERE t.expirationDate < :currentTime")
    int deleteExpiredTokens(LocalDateTime currentTime);
}