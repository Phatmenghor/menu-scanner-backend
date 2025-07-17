package com.emenu.exception;

import com.emenu.shared.constants.ErrorCodes;
import org.springframework.http.HttpStatus;

public class SubscriptionExpiredException extends CustomException {
    public SubscriptionExpiredException(String message) {
        super(message, ErrorCodes.SUBSCRIPTION_EXPIRED, HttpStatus.FORBIDDEN.value());
    }
}
