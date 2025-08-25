package com.emenu.features.notification.dto.update;

import lombok.Data;

@Data
public class TelegramCallbackQuery {
    private String id;
    private TelegramUser from;
    private TelegramMessage message;
    private String data;
}