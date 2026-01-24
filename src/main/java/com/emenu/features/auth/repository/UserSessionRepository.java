package com.emenu.features.auth.repository;

import com.emenu.features.auth.models.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {

    /**
     * Find all active sessions for a user
     */
    @Query("SELECT s FROM UserSession s WHERE s.userId = :userId AND s.status = 'ACTIVE' AND s.isDeleted = false ORDER BY s.lastActiveAt DESC")
    List<UserSession> findActiveSessionsByUserId(@Param("userId") UUID userId);

    /**
     * Find all sessions for a user (active and inactive)
     */
    @Query("SELECT s FROM UserSession s WHERE s.userId = :userId AND s.isDeleted = false ORDER BY s.loginAt DESC")
    List<UserSession> findAllSessionsByUserId(@Param("userId") UUID userId);

    /**
     * Find session by device ID
     */
    Optional<UserSession> findByUserIdAndDeviceIdAndStatusAndIsDeletedFalse(UUID userId, String deviceId, String status);

    /**
     * Find session by refresh token ID
     */
    Optional<UserSession> findByRefreshTokenIdAndIsDeletedFalse(UUID refreshTokenId);

    /**
     * Count active sessions for a user
     */
    @Query("SELECT COUNT(s) FROM UserSession s WHERE s.userId = :userId AND s.status = 'ACTIVE' AND s.isDeleted = false")
    Long countActiveSessionsByUserId(@Param("userId") UUID userId);

    /**
     * Logout all sessions for a user
     */
    @Modifying
    @Query("UPDATE UserSession s SET s.status = 'LOGGED_OUT', s.loggedOutAt = :loggedOutAt, s.logoutReason = :reason, s.isCurrentSession = false WHERE s.userId = :userId AND s.status = 'ACTIVE' AND s.isDeleted = false")
    int logoutAllSessionsByUserId(@Param("userId") UUID userId, @Param("loggedOutAt") LocalDateTime loggedOutAt, @Param("reason") String reason);

    /**
     * Logout specific session
     */
    @Modifying
    @Query("UPDATE UserSession s SET s.status = 'LOGGED_OUT', s.loggedOutAt = :loggedOutAt, s.logoutReason = :reason, s.isCurrentSession = false WHERE s.id = :sessionId AND s.userId = :userId AND s.status = 'ACTIVE'")
    int logoutSession(@Param("sessionId") UUID sessionId, @Param("userId") UUID userId, @Param("loggedOutAt") LocalDateTime loggedOutAt, @Param("reason") String reason);

    /**
     * Mark session as current
     */
    @Modifying
    @Query("UPDATE UserSession s SET s.isCurrentSession = false WHERE s.userId = :userId AND s.id != :sessionId")
    void markOtherSessionsAsNotCurrent(@Param("userId") UUID userId, @Param("sessionId") UUID sessionId);

    /**
     * Find expired sessions
     */
    @Query("SELECT s FROM UserSession s WHERE s.status = 'ACTIVE' AND s.expiresAt < :now AND s.isDeleted = false")
    List<UserSession> findExpiredSessions(@Param("now") LocalDateTime now);

    /**
     * Clean up old logged out sessions
     */
    @Modifying
    @Query("UPDATE UserSession s SET s.isDeleted = true, s.deletedAt = :deletedAt WHERE s.status IN ('LOGGED_OUT', 'EXPIRED', 'REVOKED') AND s.loggedOutAt < :cutoffDate")
    int cleanupOldSessions(@Param("deletedAt") LocalDateTime deletedAt, @Param("cutoffDate") LocalDateTime cutoffDate);
}
