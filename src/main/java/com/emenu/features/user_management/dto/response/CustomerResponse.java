package com.emenu.features.user_management.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CustomerResponse {
    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private String phoneNumber;
    private Boolean emailVerified;
    private String status;
    private Integer loyaltyPoints;
    private String customerTier;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
}