package com.emenu.features.notification.repository;

import com.emenu.enums.notification.MessageStatus;
import com.emenu.features.notification.models.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {
    
    Optional<Message> findByIdAndIsDeletedFalse(UUID id);
    
    List<Message> findByThreadIdAndIsDeletedFalseOrderByCreatedAtAsc(UUID threadId);
    
    List<Message> findBySenderIdAndIsDeletedFalse(UUID senderId);
    
    List<Message> findByParentMessageIdAndIsDeletedFalse(UUID parentMessageId);
    
    @Query("SELECT m FROM Message m WHERE m.threadId = :threadId AND m.isDeleted = false AND " +
           "m.status != 'READ' ORDER BY m.createdAt DESC")
    List<Message> findUnreadMessagesByThread(@Param("threadId") UUID threadId);
    
    @Query("SELECT COUNT(m) FROM Message m WHERE m.threadId = :threadId AND m.isDeleted = false")
    long countByThreadId(@Param("threadId") UUID threadId);
    
    @Query("SELECT COUNT(m) FROM Message m WHERE m.senderId = :senderId AND m.isDeleted = false AND " +
           "m.status != 'read'")
    long countUnreadBySender(@Param("senderId") UUID senderId);
    
    @Query("SELECT m FROM Message m WHERE m.isDeleted = false AND " +
           "m.createdAt BETWEEN :start AND :end")
    List<Message> findByCreatedAtBetween(@Param("start") LocalDateTime start,
                                        @Param("end") LocalDateTime end);
    
    @Query("SELECT m FROM Message m WHERE m.isDeleted = false AND m.isSystemMessage = true")
    List<Message> findSystemMessages();
    
    @Query("SELECT m FROM Message m WHERE m.isDeleted = false AND m.status = :status")
    List<Message> findByStatus(@Param("status") MessageStatus status);
    
    @Query("SELECT m FROM Message m WHERE m.isDeleted = false AND " +
           "(LOWER(m.content) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Message> findBySearchAndIsDeletedFalse(@Param("search") String search, Pageable pageable);
}
