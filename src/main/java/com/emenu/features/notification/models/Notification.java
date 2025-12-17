package com.emenu.features.notification.models;

import com.emenu.enums.notification.MessageStatus;
import com.emenu.enums.notification.MessageType;
import com.emenu.enums.notification.NotificationPriority;
import com.emenu.enums.notification.NotificationRecipientType;
import com.emenu.shared.domain.BaseUUIDEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notifications", indexes = {
    @Index(name = "idx_notification_user", columnList = "user_id, is_deleted"),
    @Index(name = "idx_notification_business", columnList = "business_id, is_deleted"),
    @Index(name = "idx_notification_read", columnList = "is_read, user_id"),
    @Index(name = "idx_notification_seen", columnList = "is_seen, user_id"),
    @Index(name = "idx_notification_group", columnList = "group_id, is_deleted")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Notification extends BaseUUIDEntity {

    // Basic Information
    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "message", columnDefinition = "TEXT", nullable = false)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false)
    private MessageType messageType;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false)
    private NotificationPriority priority = NotificationPriority.NORMAL;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private MessageStatus status = MessageStatus.SENT;

    // Recipient Information
    @Enumerated(EnumType.STRING)
    @Column(name = "recipient_type", nullable = false)
    private NotificationRecipientType recipientType = NotificationRecipientType.INDIVIDUAL_USER;

    @Column(name = "user_id", nullable = false)
    private UUID userId;
    
    @Column(name = "user_name")
    private String userName;

    @Column(name = "business_id")
    private UUID businessId;

    // Group Tracking
    @Column(name = "group_id")
    private UUID groupId;

    @Column(name = "is_seen", nullable = false)
    private Boolean isSeen = false;
    
    @Column(name = "seen_at")
    private LocalDateTime seenAt;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    @Column(name = "read_at")
    private LocalDateTime readAt;


    public void markAsSeen() {
        this.isSeen = true;
        this.seenAt = LocalDateTime.now();
    }

    public void markAsRead() {
        this.isRead = true;
        this.readAt = LocalDateTime.now();
        this.status = MessageStatus.READ;
        
        // Automatically mark as seen when read
        if (!this.isSeen) {
            this.isSeen = true;
            this.seenAt = this.readAt;
        }
    }

    public boolean isGroupNotification() {
        return groupId != null && recipientType.isGroupNotification();
    }
}