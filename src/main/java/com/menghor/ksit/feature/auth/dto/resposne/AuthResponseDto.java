package com.menghor.ksit.feature.auth.dto.resposne;

import lombok.Data;

@Data
public class AuthResponseDto {
    private String accessToken;
    private String tokenType = "Bearer ";
    private UserDetailsResponseDto userDetails;

    public AuthResponseDto(String accessToken, UserDetailsResponseDto userDetails) {
        this.accessToken = accessToken;
        this.userDetails = userDetails;
    }

    // You may want to add a method that combines accessToken and tokenType for convenience
    public String getFullToken() {
        return tokenType + accessToken;
    }
}