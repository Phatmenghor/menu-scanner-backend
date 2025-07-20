package com.emenu.features.auth.dto.response;

import com.emenu.enums.BusinessStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class BusinessResponse {
    
    private UUID id;
    private String name;
    private String email;
    private String phone;
    private String address;
    private String description;
    private BusinessStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    
    // Statistics
    private Integer totalStaff;
    private Integer totalCustomers;
    private Integer totalMenuItems;
    private Integer totalTables;
    private Boolean hasActiveSubscription;
    private String currentSubscriptionPlan;
}
