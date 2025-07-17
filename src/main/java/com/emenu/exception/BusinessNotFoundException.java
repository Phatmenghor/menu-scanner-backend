package com.emenu.exception;

import com.emenu.shared.constants.ErrorCodes;
import org.springframework.http.HttpStatus;

public class BusinessNotFoundException extends CustomException {
    public BusinessNotFoundException(String message) {
        super(message, ErrorCodes.BUSINESS_NOT_FOUND, HttpStatus.NOT_FOUND.value());
    }
}
