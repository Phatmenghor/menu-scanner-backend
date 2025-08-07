package com.emenu.features.notification.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TelegramMessageRequest {
    private String chatId;
    private String text;
    private String parseMode = "Markdown"; // HTML or Markdown
    private Boolean disableWebPagePreview = false;
    private Boolean disableNotification = false;
}