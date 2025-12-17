package com.emenu.features.notification.repository;

import com.emenu.enums.notification.MessageStatus;
import com.emenu.enums.notification.MessageType;
import com.emenu.features.notification.models.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    // Find by ID for specific user
    Optional<Notification> findByIdAndUserIdAndIsDeletedFalse(UUID id, UUID userId);

    // Get all notifications for a user
    @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.isDeleted = false ORDER BY n.createdAt DESC")
    Page<Notification> findByUserId(@Param("userId") UUID userId, Pageable pageable);

    // Get unread notifications for a user
    @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.isRead = false AND n.isDeleted = false ORDER BY n.createdAt DESC")
    Page<Notification> findUnreadByUserId(@Param("userId") UUID userId, Pageable pageable);

    // Count unread notifications
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.isRead = false AND n.isDeleted = false")
    long countUnreadByUserId(@Param("userId") UUID userId);

    // Get system notifications (for platform owner)
    @Query("SELECT n FROM Notification n WHERE n.isSystemCopy = true AND n.isDeleted = false ORDER BY n.createdAt DESC")
    Page<Notification> findSystemNotifications(Pageable pageable);

    // Get notifications by type
    @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.messageType = :type AND n.isDeleted = false ORDER BY n.createdAt DESC")
    Page<Notification> findByUserIdAndType(@Param("userId") UUID userId, @Param("type") MessageType type, Pageable pageable);

    // Mark all as read for user
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = :readAt, n.status = :status WHERE n.userId = :userId AND n.isRead = false")
    int markAllAsReadForUser(@Param("userId") UUID userId, @Param("readAt") LocalDateTime readAt, @Param("status") MessageStatus status);

    // Delete old read notifications
    @Modifying
    @Query("UPDATE Notification n SET n.isDeleted = true WHERE n.isRead = true AND n.readAt < :beforeDate")
    int softDeleteOldReadNotifications(@Param("beforeDate") LocalDateTime beforeDate);

    // Get notifications by business
    @Query("SELECT n FROM Notification n WHERE n.businessId = :businessId AND n.isDeleted = false ORDER BY n.createdAt DESC")
    Page<Notification> findByBusinessId(@Param("businessId") UUID businessId, Pageable pageable);

    // Search notifications
    @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.isDeleted = false " +
           "AND (:search IS NULL OR LOWER(n.title) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(n.message) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY n.createdAt DESC")
    Page<Notification> searchUserNotifications(@Param("userId") UUID userId, @Param("search") String search, Pageable pageable);
}
