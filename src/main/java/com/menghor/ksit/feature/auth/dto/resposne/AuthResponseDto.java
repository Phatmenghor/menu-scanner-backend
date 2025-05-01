package com.menghor.ksit.feature.auth.dto.resposne;

import lombok.Data;

@Data
public class AuthResponseDto {
    private String accessToken;
    private String tokenType = "Bearer ";
    private UserDto userRole;

    public AuthResponseDto(String accessToken, UserDto userRole) {
        this.accessToken = accessToken;
        this.userRole = userRole;
    }

    // You may want to add a method that combines accessToken and tokenType for convenience
    public String getFullToken() {
        return tokenType + accessToken;
    }
}