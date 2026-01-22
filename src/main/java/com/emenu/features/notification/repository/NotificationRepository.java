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

    /**
     * Finds a non-deleted notification by ID and user ID
     */
    Optional<Notification> findByIdAndUserIdAndIsDeletedFalse(UUID id, UUID userId);

    /**
     * Finds a non-deleted notification by ID
     */
    Optional<Notification> findByIdAndIsDeletedFalse(UUID id);

    /**
     * Finds all non-deleted notifications for a user, ordered by creation date descending
     */
    @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.isDeleted = false ORDER BY n.createdAt DESC")
    Page<Notification> findByUserId(@Param("userId") UUID userId, Pageable pageable);

    /**
     * Finds all unread non-deleted notifications for a user, ordered by creation date descending
     */
    @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.isRead = false AND n.isDeleted = false ORDER BY n.createdAt DESC")
    Page<Notification> findUnreadByUserId(@Param("userId") UUID userId, Pageable pageable);

    /**
     * Counts unread non-deleted notifications for a user
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.isRead = false AND n.isDeleted = false")
    long countUnreadByUserId(@Param("userId") UUID userId);

    // ===== SEEN STATUS QUERIES (For Badge Count) =====

    /**
     * Counts unseen non-deleted notifications for a user (for badge count)
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.isSeen = false AND n.isDeleted = false")
    long countUnseenByUserId(@Param("userId") UUID userId);

    /**
     * Finds all unseen non-deleted notifications for a user, ordered by creation date descending
     */
    @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.isSeen = false AND n.isDeleted = false ORDER BY n.createdAt DESC")
    Page<Notification> findUnseenByUserId(@Param("userId") UUID userId, Pageable pageable);

    // ===== COMPREHENSIVE FILTER QUERY =====

    /**
     * Searches notifications with filters for user, business, message type, priority, read status, recipient type, and text search
     */
    @Query("SELECT n FROM Notification n WHERE n.isDeleted = false " +
           "AND (:userId IS NULL OR n.userId = :userId) " +
           "AND (:businessId IS NULL OR n.businessId = :businessId) " +
           "AND (:messageType IS NULL OR n.messageType = :messageType) " +
           "AND (:priority IS NULL OR n.priority = :priority) " +
           "AND (:isRead IS NULL OR n.isRead = :isRead) " +
           "AND (:recipientType IS NULL OR n.recipientType = :recipientType) " +
           "AND (:search IS NULL OR LOWER(n.title) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "    OR LOWER(n.message) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY n.createdAt DESC")
    Page<Notification> searchNotifications(
        @Param("userId") UUID userId,
        @Param("businessId") UUID businessId,
        @Param("messageType") MessageType messageType,
        @Param("priority") NotificationPriority priority,
        @Param("isRead") Boolean isRead,
        @Param("recipientType") NotificationRecipientType recipientType,
        @Param("search") String search,
        Pageable pageable
    );

    // ===== GROUP QUERIES =====

    /**
     * Finds all non-deleted notifications in a group, ordered by creation date descending
     */
    @Query("SELECT n FROM Notification n WHERE n.groupId = :groupId AND n.isDeleted = false ORDER BY n.createdAt DESC")
    List<Notification> findByGroupId(@Param("groupId") UUID groupId);

    /**
     * Finds non-deleted notifications by business ID and recipient type, ordered by creation date descending
     */
    @Query("SELECT n FROM Notification n WHERE n.businessId = :businessId AND n.recipientType = :recipientType AND n.isDeleted = false ORDER BY n.createdAt DESC")
    Page<Notification> findByBusinessIdAndRecipientTypeAndIsDeletedFalse(
        @Param("businessId") UUID businessId,
        @Param("recipientType") NotificationRecipientType recipientType,
        Pageable pageable
    );

    /**
     * Finds non-deleted notifications by recipient type, ordered by creation date descending
     */
    @Query("SELECT n FROM Notification n WHERE n.recipientType = :recipientType AND n.isDeleted = false ORDER BY n.createdAt DESC")
    Page<Notification> findByRecipientTypeAndIsDeletedFalse(
        @Param("recipientType") NotificationRecipientType recipientType,
        Pageable pageable
    );

    // ===== SPECIFIC FILTER QUERIES =====

    /**
     * Finds non-deleted notifications by user ID and message type, ordered by creation date descending
     */
    @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.messageType = :type AND n.isDeleted = false ORDER BY n.createdAt DESC")
    Page<Notification> findByUserIdAndType(
        @Param("userId") UUID userId,
        @Param("type") MessageType type,
        Pageable pageable
    );

    /**
     * Finds non-deleted notifications by user ID and priority, ordered by creation date descending
     */
    @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.priority = :priority AND n.isDeleted = false ORDER BY n.createdAt DESC")
    Page<Notification> findByUserIdAndPriority(
        @Param("userId") UUID userId,
        @Param("priority") NotificationPriority priority,
        Pageable pageable
    );

    // ===== UPDATE OPERATIONS =====

    /**
     * Marks all unread notifications for a user as read
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = :readAt, n.status = :status WHERE n.userId = :userId AND n.isRead = false")
    int markAllAsReadForUser(
        @Param("userId") UUID userId,
        @Param("readAt") LocalDateTime readAt,
        @Param("status") MessageStatus status
    );

    /**
     * Marks all unseen notifications for a user as seen
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isSeen = true, n.seenAt = :seenAt WHERE n.userId = :userId AND n.isSeen = false")
    int markAllAsSeenForUser(
        @Param("userId") UUID userId,
        @Param("seenAt") LocalDateTime seenAt
    );

    /**
     * Marks all unread notifications in a group as read
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = :readAt, n.status = :status WHERE n.groupId = :groupId AND n.isRead = false")
    int markGroupAsRead(
        @Param("groupId") UUID groupId,
        @Param("readAt") LocalDateTime readAt,
        @Param("status") MessageStatus status
    );

    // ===== DELETE OPERATIONS =====

    /**
     * Soft deletes old read notifications that were read before the specified date
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isDeleted = true WHERE n.isRead = true AND n.readAt < :beforeDate")
    int softDeleteOldReadNotifications(@Param("beforeDate") LocalDateTime beforeDate);

    /**
     * Soft deletes all notifications in a group
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isDeleted = true WHERE n.groupId = :groupId")
    int softDeleteGroupNotifications(@Param("groupId") UUID groupId);

    /**
     * Soft deletes all non-deleted notifications for a user
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isDeleted = true WHERE n.userId = :userId AND n.isDeleted = false")
    int softDeleteAllUserNotifications(@Param("userId") UUID userId);

    // ===== STATISTICS =====

    /**
     * Counts unread non-deleted notifications for a user by priority
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.priority = :priority AND n.isRead = false AND n.isDeleted = false")
    long countUnreadByUserIdAndPriority(
        @Param("userId") UUID userId,
        @Param("priority") NotificationPriority priority
    );

    /**
     * Counts non-deleted group notifications for a user
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.groupId IS NOT NULL AND n.isDeleted = false")
    long countGroupNotificationsByUserId(@Param("userId") UUID userId);

    /**
     * Counts unread non-deleted notifications for a business
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.businessId = :businessId AND n.isRead = false AND n.isDeleted = false")
    long countUnreadByBusinessId(@Param("businessId") UUID businessId);
}