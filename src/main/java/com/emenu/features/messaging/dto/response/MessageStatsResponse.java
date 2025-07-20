package com.emenu.features.messaging.dto.response;

import lombok.Data;

@Data
public class MessageStatsResponse {
    private Long totalMessages;
    private Long sentMessages;
    private Long receivedMessages;
    private Long unreadMessages;
    private Long readMessages;
    
    // By type
    private Long generalMessages;
    private Long notifications;
    private Long announcements;
    private Long supportMessages;
    
    // By priority
    private Long lowPriorityMessages;
    private Long normalPriorityMessages;
    private Long highPriorityMessages;
    private Long urgentMessages;
    
    // Time-based
    private Long messagesToday;
    private Long messagesThisWeek;
    private Long messagesThisMonth;
}