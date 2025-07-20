package com.emenu.features.messaging.models;

import com.emenu.shared.domain.BaseUUIDEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "messages")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Message extends BaseUUIDEntity {

    @Column(name = "sender_id")
    private UUID senderId;

    @Column(name = "sender_email")
    private String senderEmail;

    @Column(name = "sender_name")
    private String senderName;

    @Column(name = "recipient_id")
    private UUID recipientId;

    @Column(name = "recipient_email")
    private String recipientEmail;

    @Column(name = "recipient_name")
    private String recipientName;

    @Column(name = "subject", nullable = false)
    private String subject;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type")
    private MessageType messageType = MessageType.GENERAL;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private MessageStatus status = MessageStatus.SENT;

    @Column(name = "priority")
    private String priority = "NORMAL";

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "business_id")
    private UUID businessId;

    public boolean isRead() {
        return readAt != null;
    }

    public void markAsRead() {
        this.readAt = LocalDateTime.now();
        this.status = MessageStatus.READ;
    }
}