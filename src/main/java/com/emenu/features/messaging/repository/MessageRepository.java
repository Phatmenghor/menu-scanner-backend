package com.emenu.features.messaging.repository;

import com.emenu.features.messaging.models.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID>, JpaSpecificationExecutor<Message> {

    Optional<Message> findByIdAndIsDeletedFalse(UUID id);

    List<Message> findByIsDeletedFalse();

    Page<Message> findByIsDeletedFalse(Pageable pageable);

    Page<Message> findByRecipientIdAndIsDeletedFalse(UUID recipientId, Pageable pageable);

    Page<Message> findBySenderIdAndIsDeletedFalse(UUID senderId, Pageable pageable);

    Page<Message> findByBusinessIdAndIsDeletedFalse(UUID businessId, Pageable pageable);

    Page<Message> findByMessageTypeAndIsDeletedFalse(MessageType messageType, Pageable pageable);

    Page<Message> findByPriorityAndIsDeletedFalse(String priority, Pageable pageable);

    @Query("SELECT m FROM Message m WHERE m.recipientId = :recipientId AND m.readAt IS NULL AND m.isDeleted = false")
    Page<Message> findUnreadByRecipientIdAndIsDeletedFalse(@Param("recipientId") UUID recipientId, Pageable pageable);

    @Query("SELECT m FROM Message m WHERE m.businessId = :businessId AND m.readAt IS NULL AND m.isDeleted = false")
    Page<Message> findUnreadByBusinessIdAndIsDeletedFalse(@Param("businessId") UUID businessId, Pageable pageable);

    // Basic count methods
    @Query("SELECT COUNT(m) FROM Message m WHERE m.isDeleted = false")
    long countByIsDeletedFalse();

    @Query("SELECT COUNT(m) FROM Message m WHERE m.status = :status AND m.isDeleted = false")
    long countByStatusAndIsDeletedFalse(@Param("status") MessageStatus status);

    // Business-related count methods (MISSING - NOW ADDED)
    @Query("SELECT COUNT(m) FROM Message m WHERE m.businessId = :businessId AND m.isDeleted = false")
    long countByBusinessIdAndIsDeletedFalse(@Param("businessId") UUID businessId);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.businessId = :businessId AND m.readAt IS NULL AND m.isDeleted = false")
    long countUnreadByBusinessIdAndIsDeletedFalse(@Param("businessId") UUID businessId);

    // User-related count methods
    @Query("SELECT COUNT(m) FROM Message m WHERE m.recipientId = :recipientId AND m.readAt IS NULL AND m.isDeleted = false")
    long countUnreadByRecipientIdAndIsDeletedFalse(@Param("recipientId") UUID recipientId);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.senderId = :senderId AND m.isDeleted = false")
    long countBySenderIdAndIsDeletedFalse(@Param("senderId") UUID senderId);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.recipientId = :recipientId AND m.isDeleted = false")
    long countByRecipientIdAndIsDeletedFalse(@Param("recipientId") UUID recipientId);

    // Message type and priority counts
    @Query("SELECT COUNT(m) FROM Message m WHERE m.messageType = :messageType AND m.isDeleted = false")
    long countByMessageTypeAndIsDeletedFalse(@Param("messageType") MessageType messageType);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.priority = :priority AND m.isDeleted = false")
    long countByPriorityAndIsDeletedFalse(@Param("priority") String priority);

    // Date-based counts
    @Query("SELECT COUNT(m) FROM Message m WHERE m.createdAt >= :date AND m.isDeleted = false")
    long countByCreatedAtAfterAndIsDeletedFalse(@Param("date") LocalDateTime date);

    // Search methods
    @Query("SELECT m FROM Message m WHERE m.isDeleted = false AND " +
            "(LOWER(m.subject) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(m.content) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Message> findBySearchAndIsDeletedFalse(@Param("search") String search, Pageable pageable);

    @Query("SELECT m FROM Message m WHERE m.recipientId = :recipientId AND m.isDeleted = false AND " +
            "(LOWER(m.subject) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(m.content) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Message> findByRecipientIdAndSearchAndIsDeletedFalse(@Param("recipientId") UUID recipientId,
                                                              @Param("search") String search,
                                                              Pageable pageable);

    @Query("SELECT m FROM Message m WHERE m.senderId = :senderId AND m.isDeleted = false AND " +
            "(LOWER(m.subject) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(m.content) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Message> findBySenderIdAndSearchAndIsDeletedFalse(@Param("senderId") UUID senderId,
                                                           @Param("search") String search,
                                                           Pageable pageable);

    // Bulk operations
    List<Message> findByIdInAndIsDeletedFalse(List<UUID> ids);

    @Query("SELECT m FROM Message m WHERE m.recipientId = :recipientId AND m.id IN :ids AND m.isDeleted = false")
    List<Message> findByRecipientIdAndIdInAndIsDeletedFalse(@Param("recipientId") UUID recipientId,
                                                            @Param("ids") List<UUID> ids);
}