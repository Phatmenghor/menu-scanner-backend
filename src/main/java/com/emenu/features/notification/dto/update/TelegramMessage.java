package com.emenu.features.notification.dto.update;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TelegramMessage {
    @JsonProperty("message_id")
    private Integer messageId;
    
    private TelegramUser from;
    private TelegramChat chat;
    private Integer date;
    private String text;
    
    public boolean hasText() {
        return text != null && !text.trim().isEmpty();
    }
    
    public boolean isCommand() {
        return hasText() && text.startsWith("/");
    }
    
    public String getCommand() {
        if (!isCommand()) return null;
        return text.split("\\s+")[0].substring(1);
    }
    
    public String getCommandArgs() {
        if (!isCommand()) return null;
        String[] parts = text.split("\\s+", 2);
        return parts.length > 1 ? parts[1] : "";
    }
}
