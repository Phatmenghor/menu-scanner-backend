package com.emenu.features.auth.service;

import com.emenu.features.auth.dto.request.TelegramAuthRequest;
import com.emenu.features.auth.dto.response.TelegramAuthResponse;

public interface TelegramService {
    TelegramAuthResponse loginWithTelegram(TelegramAuthRequest request);
    TelegramAuthResponse registerCustomerWithTelegram(TelegramAuthRequest request);
}
