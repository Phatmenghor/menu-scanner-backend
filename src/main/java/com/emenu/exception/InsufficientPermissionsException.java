package com.emenu.exception;

import com.emenu.shared.constants.ErrorCodes;
import org.springframework.http.HttpStatus;

public class InsufficientPermissionsException extends CustomException {
    public InsufficientPermissionsException(String message) {
        super(message, ErrorCodes.INSUFFICIENT_PERMISSIONS, HttpStatus.FORBIDDEN.value());
    }
}
