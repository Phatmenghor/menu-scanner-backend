package com.emenu.features.user_management.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class PlatformUserResponse {
    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private String phoneNumber;
    private String department;
    private String position;
    private Boolean emailVerified;
    private String status;
    private List<String> roles;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
}
