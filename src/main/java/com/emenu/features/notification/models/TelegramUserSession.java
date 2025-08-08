package com.emenu.features.notification.models;

import com.emenu.shared.domain.BaseUUIDEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "telegram_user_sessions", indexes = {
    @Index(name = "idx_telegram_user_id", columnList = "telegramUserId"),
    @Index(name = "idx_user_id", columnList = "userId"),
    @Index(name = "idx_last_activity", columnList = "lastActivity")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class TelegramUserSession extends BaseUUIDEntity {

    @Column(name = "telegram_user_id", nullable = false)
    private Long telegramUserId;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "telegram_username")
    private String telegramUsername;

    @Column(name = "telegram_first_name")
    private String telegramFirstName;

    @Column(name = "telegram_last_name")
    private String telegramLastName;

    @Column(name = "chat_id", nullable = false)
    private String chatId;

    @Column(name = "is_bot")
    private Boolean isBot = false;

    @Column(name = "language_code")
    private String languageCode;

    @Column(name = "is_premium")
    private Boolean isPremium = false;

    @Column(name = "first_interaction")
    private LocalDateTime firstInteraction;

    @Column(name = "last_activity")
    private LocalDateTime lastActivity;

    @Column(name = "total_interactions")
    private Long totalInteractions = 0L;

    @Column(name = "current_state")
    private String currentState; // For bot conversation state management

    @Column(name = "state_data", columnDefinition = "TEXT")
    private String stateData; // JSON data for current state

    @Column(name = "is_registered")
    private Boolean isRegistered = false;

    @Column(name = "notifications_enabled")
    private Boolean notificationsEnabled = true;

    @Column(name = "preferred_language")
    private String preferredLanguage = "en";

    // Methods
    public void updateActivity() {
        this.lastActivity = LocalDateTime.now();
        this.totalInteractions = (this.totalInteractions != null ? this.totalInteractions : 0) + 1;
    }

    public void markAsRegistered(UUID userId) {
        this.userId = userId;
        this.isRegistered = true;
        this.updateActivity();
    }

    public boolean isLinkedToUser() {
        return userId != null && Boolean.TRUE.equals(isRegistered);
    }

    public String getDisplayName() {
        if (telegramUsername != null && !telegramUsername.trim().isEmpty()) {
            return "@" + telegramUsername;
        }
        if (telegramFirstName != null && !telegramFirstName.trim().isEmpty()) {
            return telegramFirstName + (telegramLastName != null ? " " + telegramLastName : "");
        }
        return "User " + telegramUserId;
    }

    public boolean canReceiveNotifications() {
        return Boolean.TRUE.equals(notificationsEnabled);
    }

    public void setState(String state, String data) {
        this.currentState = state;
        this.stateData = data;
        this.updateActivity();
    }

    public void clearState() {
        this.currentState = null;
        this.stateData = null;
    }
}