package com.emenu.features.notification.models;

import com.emenu.enums.notification.AlertType;
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

    @Column(name = "recipient_id", nullable = false)
    private UUID recipientId;

    @Column(name = "sender_id")
    private UUID senderId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "html_content", columnDefinition = "TEXT")
    private String htmlContent;

    @Enumerated(EnumType.STRING)
    @Column(name = "alert_type")
    private AlertType alertType;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false)
    private NotificationChannel channel;

    @Column(name = "is_read")
    private Boolean isRead = false;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "is_sent")
    private Boolean isSent = false;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "delivery_status")
    private String deliveryStatus;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @Column(name = "business_id")
    private UUID businessId;

    @Column(name = "related_entity_type")
    private String relatedEntityType; // Subscription, Payment, User, etc.

    @Column(name = "related_entity_id")
    private UUID relatedEntityId;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata; // JSON for additional data

    // Business methods
    public void markAsRead() {
        this.isRead = true;
        this.readAt = LocalDateTime.now();
    }

    public void markAsSent() {
        this.isSent = true;
        this.sentAt = LocalDateTime.now();
        this.deliveryStatus = "SENT";
    }

    public void markAsDelivered() {
        this.deliveryStatus = "DELIVERED";
    }

    public void markAsFailed(String errorMessage) {
        this.deliveryStatus = "FAILED";
        this.errorMessage = errorMessage;
    }

    public boolean shouldSendNow() {
        return scheduledAt == null || LocalDateTime.now().isAfter(scheduledAt);
    }
}
