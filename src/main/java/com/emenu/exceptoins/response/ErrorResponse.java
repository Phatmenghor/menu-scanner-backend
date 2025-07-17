package com.emenu.exceptoins.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ErrorResponse {
    private String status;
    private String message;
    private int statusCode;
    private String path;

    // Constructor for backward compatibility
    public ErrorResponse(String status, String message, int statusCode) {
        this.status = status;
        this.message = message;
        this.statusCode = statusCode;
        this.path = null;
    }
}