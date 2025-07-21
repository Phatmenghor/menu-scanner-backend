package com.emenu.features.notification.repository;

import com.emenu.enums.notification.MessageType;
import com.emenu.features.notification.models.MessageThread;
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
public interface MessageThreadRepository extends JpaRepository<MessageThread, UUID>, JpaSpecificationExecutor<MessageThread> {
    
    Optional<MessageThread> findByIdAndIsDeletedFalse(UUID id);
    
    Page<MessageThread> findByIsDeletedFalse(Pageable pageable);
    
    List<MessageThread> findByBusinessIdAndIsDeletedFalse(UUID businessId);
    
    List<MessageThread> findByCustomerIdAndIsDeletedFalse(UUID customerId);
    
    List<MessageThread> findByPlatformUserIdAndIsDeletedFalse(UUID platformUserId);
    
    @Query("SELECT mt FROM MessageThread mt WHERE mt.isDeleted = false AND " +
           "(mt.businessId = :userId OR mt.customerId = :userId OR mt.platformUserId = :userId)")
    List<MessageThread> findByParticipantId(@Param("userId") UUID userId);
    
    @Query("SELECT mt FROM MessageThread mt WHERE mt.isDeleted = false AND " +
           "(mt.businessId = :userId OR mt.customerId = :userId OR mt.platformUserId = :userId) AND " +
           "mt.isClosed = false")
    List<MessageThread> findOpenThreadsByParticipantId(@Param("userId") UUID userId);
    
    List<MessageThread> findByMessageTypeAndIsDeletedFalse(MessageType messageType);
    
    List<MessageThread> findByIsClosedAndIsDeletedFalse(Boolean isClosed);
    
    @Query("SELECT COUNT(mt) FROM MessageThread mt WHERE mt.businessId = :businessId AND mt.isDeleted = false")
    long countByBusinessId(@Param("businessId") UUID businessId);
    
    @Query("SELECT COUNT(mt) FROM MessageThread mt WHERE mt.isDeleted = false AND mt.isClosed = false")
    long countOpenThreads();
    
    @Query("SELECT COUNT(mt) FROM MessageThread mt WHERE mt.isDeleted = false AND mt.isSystemGenerated = true")
    long countSystemThreads();
    
    @Query("SELECT mt FROM MessageThread mt WHERE mt.isDeleted = false AND " +
           "mt.lastMessageAt BETWEEN :start AND :end")
    List<MessageThread> findByLastMessageBetween(@Param("start") LocalDateTime start,
                                                 @Param("end") LocalDateTime end);
    
    @Query("SELECT mt FROM MessageThread mt WHERE mt.isDeleted = false AND " +
           "(LOWER(mt.subject) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<MessageThread> findBySearchAndIsDeletedFalse(@Param("search") String search, Pageable pageable);
}