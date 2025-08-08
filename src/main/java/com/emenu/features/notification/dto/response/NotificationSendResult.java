package com.emenu.features.notification.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class NotificationSendResult {
    
    private String notificationType;
    private LocalDateTime sentAt;
    
    // Send results
    private Integer totalRecipients;
    private Integer successfulSends;
    private Integer failedSends;
    
    // Breakdown by channel
    private Integer telegramSent;
    private Integer telegramFailed;
    private Integer inAppSent;
    private Integer inAppFailed;
    
    // Recipients details
    private List<RecipientResult> results;
    
    // Summary
    private Boolean allSuccessful;
    private String summary;
    private List<String> errors;
    
    @Data
    @Builder
    public static class RecipientResult {
        private UUID userId;
        private Long telegramUserId;
        private String recipientName;
        private String channel; // TELEGRAM, IN_APP
        private Boolean success;
        private String error;
        private LocalDateTime sentAt;
    }
}