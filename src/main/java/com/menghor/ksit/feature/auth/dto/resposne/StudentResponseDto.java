package com.menghor.ksit.feature.auth.dto.resposne;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StudentResponseDto {
    private Long id;
    private String username;
    private String identifyNumber;
    private String password;
    private String classCode;
    private LocalDateTime createdAt;
}