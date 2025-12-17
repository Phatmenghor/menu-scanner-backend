package com.emenu.features.notification.models;

import com.emenu.enums.notification.MessageStatus;
import com.emenu.enums.notification.MessageType;
import com.emenu.enums.notification.NotificationChannel;
import com.emenu.shared.domain.BaseUUIDEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notifications")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Notification extends BaseUUIDEntity {

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "message", columnDefinition = "TEXT", nullable = false)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false)
    private MessageType messageType;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false)
    private NotificationChannel channel = NotificationChannel.IN_APP;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private MessageStatus status = MessageStatus.SENT;

    // Recipient Information
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    
    @Column(name = "user_name")
    private String userName;

    // Business Context (if applicable)
    @Column(name = "business_id")
    private UUID businessId;

    // For System Owner Copy
    @Column(name = "is_system_copy", nullable = false)
    private Boolean isSystemCopy = false;  // true = copy sent to platform owner

    // Read Status
    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    // Reference (optional - for linking to orders, payments, etc)
    @Column(name = "reference_type")
    private String referenceType; 
    
    @Column(name = "reference_id")
    private UUID referenceId;

    // Action URL (for frontend navigation)
    @Column(name = "action_url")
    private String actionUrl;

    // Helper methods
    public void markAsRead() {
        this.isRead = true;
        this.readAt = LocalDateTime.now();
        this.status = MessageStatus.READ;
    }

    public static Notification createUserNotification(
            String title, 
            String message, 
            MessageType type,
            UUID userId,
            String userName) {
        Notification notification = new Notification();
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setMessageType(type);
        notification.setUserId(userId);
        notification.setUserName(userName);
        notification.setIsSystemCopy(false);
        return notification;
    }

    public static Notification createSystemCopy(Notification original, UUID platformOwnerId) {
        Notification systemCopy = new Notification();
        systemCopy.setTitle("[System] " + original.getTitle());
        systemCopy.setMessage(original.getMessage() + "\n\nUser: " + original.getUserName());
        systemCopy.setMessageType(original.getMessageType());
        systemCopy.setUserId(platformOwnerId);
        systemCopy.setIsSystemCopy(true);
        systemCopy.setReferenceType(original.getReferenceType());
        systemCopy.setReferenceId(original.getReferenceId());
        systemCopy.setBusinessId(original.getBusinessId());
        return systemCopy;
    }
}
