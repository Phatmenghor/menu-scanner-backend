package com.emenu.features.notification.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TelegramMessageRequest {
    
    @JsonProperty("chat_id")
    private String chatId;
    
    private String text;
    
    @JsonProperty("parse_mode")
    @Builder.Default
    private String parseMode = "Markdown"; // HTML or Markdown
    
    @JsonProperty("disable_web_page_preview")
    @Builder.Default
    private Boolean disableWebPagePreview = false;
    
    @JsonProperty("disable_notification")
    @Builder.Default
    private Boolean disableNotification = false;
}