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
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Statistics
    private Integer totalOrders;
    private Integer totalMessages;
    private Integer unreadMessages;
}