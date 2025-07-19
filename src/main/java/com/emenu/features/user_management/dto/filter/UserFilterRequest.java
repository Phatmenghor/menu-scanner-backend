package com.emenu.features.user_management.dto.filter;

import com.emenu.enums.Status;
import com.emenu.enums.UserType;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class UserFilterRequest {
    private String search; // Search in name, email
    private UserType userType;
    private Status status;
    private UUID businessId;
    private Boolean emailVerified;
    private LocalDate createdAfter;
    private LocalDate createdBefore;
    
    // Pagination
    private Integer pageNo = 0;
    private Integer pageSize = 10;
    private String sortBy = "createdAt";
    private String sortDirection = "DESC";
}