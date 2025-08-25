package com.emenu.features.notification.dto.update;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TelegramUpdate {
    @JsonProperty("update_id")
    private Long updateId;
    
    private TelegramMessage message;
    
    @JsonProperty("callback_query")
    private TelegramCallbackQuery callbackQuery;
    
    public boolean hasMessage() {
        return message != null;
    }
    
    public boolean hasCallbackQuery() {
        return callbackQuery != null;
    }
    
    public String getChatId() {
        if (hasMessage()) {
            return message.getChat().getId().toString();
        } else if (hasCallbackQuery()) {
            return callbackQuery.getMessage().getChat().getId().toString();
        }
        return null;
    }
    
    public Long getUserId() {
        if (hasMessage()) {
            return message.getFrom().getId();
        } else if (hasCallbackQuery()) {
            return callbackQuery.getFrom().getId();
        }
        return null;
    }
}