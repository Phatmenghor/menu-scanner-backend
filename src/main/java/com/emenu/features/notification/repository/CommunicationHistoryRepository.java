package com.emenu.features.notification.repository;

import com.emenu.enums.notification.NotificationChannel;
import com.emenu.features.notification.models.CommunicationHistory;
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
public interface CommunicationHistoryRepository extends JpaRepository<CommunicationHistory, UUID>, JpaSpecificationExecutor<CommunicationHistory> {
    
    Optional<CommunicationHistory> findByIdAndIsDeletedFalse(UUID id);
    
    Page<CommunicationHistory> findByRecipientIdAndIsDeletedFalse(UUID recipientId, Pageable pageable);
    
    List<CommunicationHistory> findByBusinessIdAndIsDeletedFalse(UUID businessId);
    
    List<CommunicationHistory> findByChannelAndIsDeletedFalse(NotificationChannel channel);
    
    @Query("SELECT ch FROM CommunicationHistory ch WHERE ch.isDeleted = false AND " +
           "ch.sentAt BETWEEN :start AND :end")
    List<CommunicationHistory> findBySentAtBetween(@Param("start") LocalDateTime start,
                                                   @Param("end") LocalDateTime end);
    
    @Query("SELECT COUNT(ch) FROM CommunicationHistory ch WHERE ch.recipientId = :recipientId AND " +
           "ch.isDeleted = false")
    long countByRecipient(@Param("recipientId") UUID recipientId);
    
    @Query("SELECT COUNT(ch) FROM CommunicationHistory ch WHERE ch.businessId = :businessId AND " +
           "ch.isDeleted = false")
    long countByBusiness(@Param("businessId") UUID businessId);
    
    @Query("SELECT COUNT(ch) FROM CommunicationHistory ch WHERE ch.channel = :channel AND " +
           "ch.isDeleted = false")
    long countByChannel(@Param("channel") NotificationChannel channel);
    
    @Query("SELECT COUNT(ch) FROM CommunicationHistory ch WHERE ch.status = :status AND " +
           "ch.isDeleted = false")
    long countByStatus(@Param("status") String status);
    
    List<CommunicationHistory> findByRelatedThreadIdAndIsDeletedFalse(UUID threadId);
    
    List<CommunicationHistory> findByRelatedMessageIdAndIsDeletedFalse(UUID messageId);
    
    Optional<CommunicationHistory> findByExternalMessageIdAndIsDeletedFalse(String externalMessageId);
    
    @Query("SELECT ch FROM CommunicationHistory ch WHERE ch.isDeleted = false AND " +
           "(LOWER(ch.subject) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(ch.content) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<CommunicationHistory> findBySearchAndIsDeletedFalse(@Param("search") String search, Pageable pageable);
}