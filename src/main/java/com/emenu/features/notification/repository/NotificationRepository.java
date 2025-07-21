package com.emenu.features.notification.repository;

import com.emenu.enums.notification.AlertType;
import com.emenu.enums.notification.NotificationChannel;
import com.emenu.features.notification.models.Notification;
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
public interface NotificationRepository extends JpaRepository<Notification, UUID>, JpaSpecificationExecutor<Notification> {
    
    Optional<Notification> findByIdAndIsDeletedFalse(UUID id);
    
    Page<Notification> findByRecipientIdAndIsDeletedFalse(UUID recipientId, Pageable pageable);
    
    List<Notification> findByRecipientIdAndIsReadAndIsDeletedFalse(UUID recipientId, Boolean isRead);
    
    List<Notification> findByBusinessIdAndIsDeletedFalse(UUID businessId);
    
    List<Notification> findByChannelAndIsDeletedFalse(NotificationChannel channel);
    
    List<Notification> findByAlertTypeAndIsDeletedFalse(AlertType alertType);
    
    @Query("SELECT n FROM Notification n WHERE n.isDeleted = false AND n.isSent = false AND " +
           "(n.scheduledAt IS NULL OR n.scheduledAt <= :now)")
    List<Notification> findPendingNotifications(@Param("now") LocalDateTime now);
    
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.recipientId = :recipientId AND " +
           "n.isRead = false AND n.isDeleted = false")
    long countUnreadByRecipient(@Param("recipientId") UUID recipientId);
    
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.businessId = :businessId AND n.isDeleted = false")
    long countByBusinessId(@Param("businessId") UUID businessId);
    
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.channel = :channel AND n.isDeleted = false")
    long countByChannel(@Param("channel") NotificationChannel channel);
    
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.alertType = :alertType AND n.isDeleted = false")
    long countByAlertType(@Param("alertType") AlertType alertType);
    
    @Query("SELECT n FROM Notification n WHERE n.isDeleted = false AND " +
           "n.createdAt BETWEEN :start AND :end")
    List<Notification> findByCreatedAtBetween(@Param("start") LocalDateTime start, 
                                             @Param("end") LocalDateTime end);
    
    @Query("SELECT n FROM Notification n WHERE n.isDeleted = false AND n.deliveryStatus = 'FAILED'")
    List<Notification> findFailedNotifications();
    
    @Query("SELECT n FROM Notification n WHERE n.relatedEntityType = :entityType AND " +
           "n.relatedEntityId = :entityId AND n.isDeleted = false")
    List<Notification> findByRelatedEntity(@Param("entityType") String entityType, 
                                          @Param("entityId") UUID entityId);
}