package com.emenu.features.notification.repository;

import com.emenu.features.notification.models.MessageAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MessageAttachmentRepository extends JpaRepository<MessageAttachment, UUID> {
    
    Optional<MessageAttachment> findByIdAndIsDeletedFalse(UUID id);
    
    List<MessageAttachment> findByMessageIdAndIsDeletedFalse(UUID messageId);
    
    @Query("SELECT COUNT(ma) FROM MessageAttachment ma WHERE ma.messageId = :messageId AND ma.isDeleted = false")
    long countByMessageId(@Param("messageId") UUID messageId);
    
    @Query("SELECT SUM(ma.fileSize) FROM MessageAttachment ma WHERE ma.messageId = :messageId AND ma.isDeleted = false")
    Long getTotalFileSizeByMessage(@Param("messageId") UUID messageId);
    
    List<MessageAttachment> findByFileTypeAndIsDeletedFalse(String fileType);
}