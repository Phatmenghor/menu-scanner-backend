package com.emenu.features.auth.repository;

import com.emenu.features.auth.models.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for managing RefreshToken entities.
 *
 * @author Cambodia E-Menu Platform
 * @version 1.0.0
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    /**
     * Find a valid refresh token by token string
     *
     * @param token the token string
     * @return Optional containing the token if found and valid
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.token = :token AND rt.isRevoked = false AND rt.isDeleted = false")
    Optional<RefreshToken> findByTokenAndIsValidTrue(@Param("token") String token);

    /**
     * Find all valid refresh tokens for a user
     *
     * @param userId the user ID
     * @return list of valid refresh tokens
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.userId = :userId AND rt.isRevoked = false AND rt.isDeleted = false")
    List<RefreshToken> findAllValidByUserId(@Param("userId") UUID userId);

    /**
     * Revoke all refresh tokens for a user (e.g., on password change)
     *
     * @param userId the user ID
     * @param reason the reason for revocation
     * @return number of tokens revoked
     */
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.isRevoked = true, rt.revokedAt = :revokedAt, rt.revocationReason = :reason WHERE rt.userId = :userId AND rt.isRevoked = false")
    int revokeAllByUserId(@Param("userId") UUID userId, @Param("revokedAt") LocalDateTime revokedAt, @Param("reason") String reason);

    /**
     * Delete expired refresh tokens
     *
     * @param now current date time
     * @return number of tokens deleted
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiryDate < :now OR (rt.isRevoked = true AND rt.revokedAt < :cutoffDate)")
    int deleteExpiredAndRevokedTokens(@Param("now") LocalDateTime now, @Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Count active refresh tokens for a user
     *
     * @param userId the user ID
     * @return count of active tokens
     */
    @Query("SELECT COUNT(rt) FROM RefreshToken rt WHERE rt.userId = :userId AND rt.isRevoked = false AND rt.isDeleted = false AND rt.expiryDate > :now")
    long countActiveTokensByUserId(@Param("userId") UUID userId, @Param("now") LocalDateTime now);

    /**
     * Find refresh token by token string (including revoked/expired)
     *
     * @param token the token string
     * @return Optional containing the token if found
     */
    Optional<RefreshToken> findByToken(String token);
}
