package com.menghor.ksit.feature.auth.dto.resposne;

import com.menghor.ksit.enumations.RoleEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDto {
    private String accessToken;
    private String tokenType = "Bearer ";

    // User basic information
    private Long userId;
    private String username;
    private String email;
    private List<RoleEnum> roles;

    // Constructor for backward compatibility
    public AuthResponseDto(String accessToken) {
        this.accessToken = accessToken;
    }

    // Convenience method for full token
    public String getFullToken() {
        return tokenType + accessToken;
    }
}