package com.emenu.exception;

import com.emenu.shared.constants.ErrorCodes;
import org.springframework.http.HttpStatus;

public class UserNotFoundException extends CustomException {
    public UserNotFoundException(String message) {
        super(message, ErrorCodes.USER_NOT_FOUND, HttpStatus.NOT_FOUND.value());
    }
}