package com.emenu.features.notification.dto.request;

import lombok.Data;

import java.util.Map;

@Data
public class TelegramBotCallbackRequest {
    
    private Long callbackQueryId;
    private Long telegramUserId;
    private String chatId;
    private String data; // Callback data
    private String messageId;
    
    // User info
    private String telegramUsername;
    private String telegramFirstName;
    private String telegramLastName;
    
    // Additional context
    private Map<String, String> context;
}
