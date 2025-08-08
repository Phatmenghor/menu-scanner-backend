package com.emenu.features.notification.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class TelegramBotResponse {
    
    private Boolean success;
    private String message;
    private String responseType; // TEXT, CALLBACK_ANSWER, INLINE_KEYBOARD
    private Object responseData;
    private LocalDateTime timestamp;
    
    // For callback responses
    private String callbackText;
    private Boolean showAlert;
    
    // For message responses
    private String chatId;
    private String parseMode;
    private Boolean disableWebPagePreview;
}