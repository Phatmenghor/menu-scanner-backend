package com.emenu.features.notification.service;

import com.emenu.features.notification.dto.update.TelegramUpdate;

public interface TelegramBotService {
    void processUpdate(TelegramUpdate update);
    boolean setWebhook(String url);
    Object getWebhookInfo();
    boolean deleteWebhook();
    void sendMessage(String chatId, String message);
    void sendMessageWithKeyboard(String chatId, String message, Object keyboard);
}