package com.emenu.features.auth.repository;

import com.emenu.features.auth.models.UserSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    Optional<UserSession> findByIdAndUserIdAndIsDeletedFalse(UUID id, UUID userId);

    Optional<UserSession> findByIdAndIsDeletedFalse(UUID id);

    @Query("SELECT s FROM UserSession s WHERE s.userId = :userId AND s.isDeleted = false " +
            "ORDER BY s.isCurrentSession DESC, CASE WHEN s.status = 'ACTIVE' THEN 0 ELSE 1 END, s.lastActiveAt DESC")
    List<UserSession> findAllSessionsByUserId(@Param("userId") UUID userId);

    @Query("SELECT s FROM UserSession s WHERE s.userId = :userId AND s.status = 'ACTIVE' AND s.isDeleted = false")
    List<UserSession> findActiveSessionsByUserId(@Param("userId") UUID userId);

    @Query("SELECT COUNT(s) FROM UserSession s WHERE s.userId = :userId AND s.status = 'ACTIVE' AND s.isDeleted = false")
    Long countActiveSessionsByUserId(@Param("userId") UUID userId);

    @Modifying
    @Query("UPDATE UserSession s SET s.status = 'LOGGED_OUT', s.loggedOutAt = :loggedOutAt, s.logoutReason = :reason, s.isCurrentSession = false " +
            "WHERE s.userId = :userId AND s.status = 'ACTIVE' AND s.isDeleted = false")
    int logoutAllSessionsByUserId(@Param("userId") UUID userId, @Param("loggedOutAt") LocalDateTime loggedOutAt, @Param("reason") String reason);

    @Modifying
    @Query("UPDATE UserSession s SET s.isCurrentSession = false WHERE s.userId = :userId AND s.id != :sessionId")
    void markOtherSessionsAsNotCurrent(@Param("userId") UUID userId, @Param("sessionId") UUID sessionId);

    @Query("SELECT s FROM UserSession s LEFT JOIN FETCH s.user u WHERE s.isDeleted = false " +
            "AND (:userId IS NULL OR s.userId = :userId) " +
            "AND (:statuses IS NULL OR s.status IN :statuses) " +
            "AND (:deviceTypes IS NULL OR s.deviceType IN :deviceTypes) " +
            "AND (:search IS NULL OR :search = '' OR " +
            "LOWER(s.deviceName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(s.browser) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(s.ipAddress) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.userIdentifier) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<UserSession> findAllWithFilters(
            @Param("userId") UUID userId,
            @Param("statuses") List<String> statuses,
            @Param("deviceTypes") List<String> deviceTypes,
            @Param("search") String search,
            Pageable pageable);
}
