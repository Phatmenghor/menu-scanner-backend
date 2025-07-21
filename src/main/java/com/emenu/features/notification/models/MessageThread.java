package com.emenu.features.notification.models;

import com.emenu.enums.notification.MessageType;
import com.emenu.shared.domain.BaseUUIDEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "message_threads")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class MessageThread extends BaseUUIDEntity {

    @Column(name = "subject", nullable = false)
    private String subject;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false)
    private MessageType messageType;

    @Column(name = "participant_ids", columnDefinition = "TEXT")
    private String participantIds; // JSON array of UUID strings

    @Column(name = "business_id")
    private UUID businessId;

    @Column(name = "customer_id")
    private UUID customerId;

    @Column(name = "platform_user_id")
    private UUID platformUserId;

    @Column(name = "is_system_generated")
    private Boolean isSystemGenerated = false;

    @Column(name = "priority")
    private Integer priority = 1; // 1 = Low, 2 = Medium, 3 = High, 4 = Critical

    @Column(name = "is_closed")
    private Boolean isClosed = false;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Column(name = "closed_by")
    private UUID closedBy;

    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;

    @Column(name = "unread_count")
    private Integer unreadCount = 0;

    @OneToMany(mappedBy = "messageThread", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Message> messages;

    // Business methods
    public void closeThread(UUID userId) {
        this.isClosed = true;
        this.closedAt = LocalDateTime.now();
        this.closedBy = userId;
    }

    public void reopenThread() {
        this.isClosed = false;
        this.closedAt = null;
        this.closedBy = null;
    }

    public void updateLastMessageTime() {
        this.lastMessageAt = LocalDateTime.now();
    }

    public void incrementUnreadCount() {
        this.unreadCount = (this.unreadCount == null ? 0 : this.unreadCount) + 1;
    }

    public void resetUnreadCount() {
        this.unreadCount = 0;
    }
}