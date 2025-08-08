package com.emenu.features.notification.repository;

import com.emenu.features.notification.models.TelegramUserSession;
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
public interface TelegramUserSessionRepository extends JpaRepository<TelegramUserSession, UUID> {

    // ===== BASIC SESSION QUERIES =====
    Optional<TelegramUserSession> findByTelegramUserIdAndIsDeletedFalse(Long telegramUserId);
    Optional<TelegramUserSession> findByChatIdAndIsDeletedFalse(String chatId);
    
    // ===== USER LINKING QUERIES =====
    Optional<TelegramUserSession> findByUserIdAndIsDeletedFalse(UUID userId);
    List<TelegramUserSession> findByIsRegisteredTrueAndIsDeletedFalse();
    List<TelegramUserSession> findByIsRegisteredFalseAndIsDeletedFalse();
    
    // ===== ACTIVITY TRACKING =====
    @Modifying
    @Query("UPDATE TelegramUserSession t SET t.lastActivity = :activity, t.totalInteractions = t.totalInteractions + 1 WHERE t.telegramUserId = :telegramUserId")
    void updateActivity(@Param("telegramUserId") Long telegramUserId, @Param("activity") LocalDateTime activity);
    
    @Query("SELECT t FROM TelegramUserSession t WHERE t.lastActivity < :cutoff AND t.isDeleted = false")
    List<TelegramUserSession> findInactiveSessions(@Param("cutoff") LocalDateTime cutoff);
    
    @Query("SELECT t FROM TelegramUserSession t WHERE t.lastActivity > :since AND t.isDeleted = false ORDER BY t.lastActivity DESC")
    List<TelegramUserSession> findRecentActiveSessions(@Param("since") LocalDateTime since);

    // ===== STATE MANAGEMENT =====
    @Query("SELECT t FROM TelegramUserSession t WHERE t.currentState = :state AND t.isDeleted = false")
    List<TelegramUserSession> findByCurrentState(@Param("state") String state);
    
    @Modifying
    @Query("UPDATE TelegramUserSession t SET t.currentState = :state, t.stateData = :data WHERE t.telegramUserId = :telegramUserId")
    void updateSessionState(@Param("telegramUserId") Long telegramUserId, @Param("state") String state, @Param("data") String data);
    
    @Modifying
    @Query("UPDATE TelegramUserSession t SET t.currentState = NULL, t.stateData = NULL WHERE t.telegramUserId = :telegramUserId")
    void clearSessionState(@Param("telegramUserId") Long telegramUserId);

    // ===== NOTIFICATION QUERIES =====
    @Query("SELECT t FROM TelegramUserSession t WHERE t.notificationsEnabled = true AND t.isDeleted = false")
    List<TelegramUserSession> findAllWithNotificationsEnabled();
    
    @Query("SELECT t FROM TelegramUserSession t WHERE t.isRegistered = true AND t.notificationsEnabled = true AND t.isDeleted = false")
    List<TelegramUserSession> findRegisteredWithNotifications();

    // ===== STATISTICS QUERIES =====
    @Query("SELECT COUNT(t) FROM TelegramUserSession t WHERE t.isRegistered = true AND t.isDeleted = false")
    long countRegisteredSessions();
    
    @Query("SELECT COUNT(t) FROM TelegramUserSession t WHERE t.isRegistered = false AND t.isDeleted = false")
    long countUnregisteredSessions();
    
    @Query("SELECT COUNT(t) FROM TelegramUserSession t WHERE t.lastActivity > :since AND t.isDeleted = false")
    long countActiveSince(@Param("since") LocalDateTime since);
    
    @Query("SELECT COUNT(t) FROM TelegramUserSession t WHERE t.notificationsEnabled = true AND t.isDeleted = false")
    long countWithNotificationsEnabled();

    // ===== CLEANUP QUERIES =====
    @Modifying
    @Query("DELETE FROM TelegramUserSession t WHERE t.lastActivity < :cutoff AND t.isRegistered = false")
    int deleteInactiveUnregisteredSessions(@Param("cutoff") LocalDateTime cutoff);
    
    @Modifying
    @Query("UPDATE TelegramUserSession t SET t.currentState = NULL, t.stateData = NULL WHERE t.lastActivity < :cutoff")
    int clearOldSessionStates(@Param("cutoff") LocalDateTime cutoff);

    // ===== USERNAME QUERIES =====
    List<TelegramUserSession> findByTelegramUsernameIgnoreCaseAndIsDeletedFalse(String telegramUsername);
    
    @Query("SELECT t FROM TelegramUserSession t WHERE LOWER(t.telegramFirstName) LIKE LOWER(CONCAT('%', :name, '%')) OR LOWER(t.telegramLastName) LIKE LOWER(CONCAT('%', :name, '%')) AND t.isDeleted = false")
    List<TelegramUserSession> findByNameContainingIgnoreCase(@Param("name") String name);
}