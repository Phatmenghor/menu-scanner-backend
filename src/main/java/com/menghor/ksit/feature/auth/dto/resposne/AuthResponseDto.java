package com.menghor.ksit.feature.auth.dto.resposne;

import lombok.Data;

@Data
public class AuthResponseDto {
    private String accessToken;
    private String tokenType = "Bearer ";

    public AuthResponseDto(String accessToken) {
        this.accessToken = accessToken;
    }

    // You may want to add a method that combines accessToken and tokenType for convenience
    public String getFullToken() {
        return tokenType + accessToken;
    }
}