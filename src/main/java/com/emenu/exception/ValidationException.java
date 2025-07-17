package com.emenu.exception;

import com.emenu.shared.constants.ErrorCodes;
import org.springframework.http.HttpStatus;

public class ValidationException extends CustomException {
    public ValidationException(String message) {
        super(message, ErrorCodes.VALIDATION_ERROR, HttpStatus.BAD_REQUEST.value());
    }
}