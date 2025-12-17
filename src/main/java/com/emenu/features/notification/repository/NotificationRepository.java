package com.emenu.features.notification.repository;

import com.emenu.enums.notification.MessageStatus;
import com.emenu.enums.notification.MessageType;
import com.emenu.enums.notification.NotificationPriority;
import com.emenu.enums.notification.NotificationRecipientType;
import com.emenu.features.notification.models.Notification;
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
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    // ===== INDIVIDUAL QUERIES =====
    Optional<Notification> findByIdAndUserIdAndIsDeletedFalse(UUID id, UUID userId);

    @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.isDeleted = false ORDER BY n.createdAt DESC")
    Page<Notification> findByUserId(@Param("userId") UUID userId, Pageable pageable);

    @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.isRead = false AND n.isDeleted = false ORDER BY n.createdAt DESC")
    Page<Notification> findUnreadByUserId(@Param("userId") UUID userId, Pageable pageable);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.isRead = false AND n.isDeleted = false")
    long countUnreadByUserId(@Param("userId") UUID userId);

    // ===== GROUP QUERIES =====
    @Query("SELECT n FROM Notification n WHERE n.groupId = :groupId AND n.isDeleted = false ORDER BY n.createdAt DESC")
    List<Notification> findByGroupId(@Param("groupId") UUID groupId);

    @Query("SELECT n FROM Notification n WHERE n.businessId = :businessId AND n.recipientType = :recipientType AND n.isDeleted = false ORDER BY n.createdAt DESC")
    Page<Notification> findByBusinessIdAndRecipientTypeAndIsDeletedFalse(
        @Param("businessId") UUID businessId,
        @Param("recipientType") NotificationRecipientType recipientType,
        Pageable pageable
    );

    @Query("SELECT n FROM Notification n WHERE n.recipientType = :recipientType AND n.isDeleted = false ORDER BY n.createdAt DESC")
    Page<Notification> findByRecipientTypeAndIsDeletedFalse(
        @Param("recipientType") NotificationRecipientType recipientType,
        Pageable pageable
    );

    // ===== FILTER QUERIES =====
    @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.messageType = :type AND n.isDeleted = false ORDER BY n.createdAt DESC")
    Page<Notification> findByUserIdAndType(
        @Param("userId") UUID userId,
        @Param("type") MessageType type,
        Pageable pageable
    );

    @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.priority = :priority AND n.isDeleted = false ORDER BY n.createdAt DESC")
    Page<Notification> findByUserIdAndPriority(
        @Param("userId") UUID userId,
        @Param("priority") NotificationPriority priority,
        Pageable pageable
    );

    @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.isDeleted = false " +
           "AND (:search IS NULL OR LOWER(n.title) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(n.message) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY n.createdAt DESC")
    Page<Notification> searchUserNotifications(
        @Param("userId") UUID userId,
        @Param("search") String search,
        Pageable pageable
    );

    // ===== UPDATE OPERATIONS =====
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = :readAt, n.status = :status WHERE n.userId = :userId AND n.isRead = false")
    int markAllAsReadForUser(
        @Param("userId") UUID userId,
        @Param("readAt") LocalDateTime readAt,
        @Param("status") MessageStatus status
    );

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = :readAt, n.status = :status WHERE n.groupId = :groupId AND n.isRead = false")
    int markGroupAsRead(
        @Param("groupId") UUID groupId,
        @Param("readAt") LocalDateTime readAt,
        @Param("status") MessageStatus status
    );

    // ===== DELETE OPERATIONS =====
    @Modifying
    @Query("UPDATE Notification n SET n.isDeleted = true WHERE n.isRead = true AND n.readAt < :beforeDate")
    int softDeleteOldReadNotifications(@Param("beforeDate") LocalDateTime beforeDate);

    @Modifying
    @Query("UPDATE Notification n SET n.isDeleted = true WHERE n.groupId = :groupId")
    int softDeleteGroupNotifications(@Param("groupId") UUID groupId);

    // ===== STATISTICS =====
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.priority = :priority AND n.isRead = false AND n.isDeleted = false")
    long countUnreadByUserIdAndPriority(
        @Param("userId") UUID userId,
        @Param("priority") NotificationPriority priority
    );

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.groupId IS NOT NULL AND n.isDeleted = false")
    long countGroupNotificationsByUserId(@Param("userId") UUID userId);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.businessId = :businessId AND n.isRead = false AND n.isDeleted = false")
    long countUnreadByBusinessId(@Param("businessId") UUID businessId);
}