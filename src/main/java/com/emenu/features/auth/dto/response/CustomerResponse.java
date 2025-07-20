package com.emenu.features.auth.dto.response;

import com.emenu.enums.AccountStatus;
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
    private AccountStatus accountStatus;
    private String address;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}