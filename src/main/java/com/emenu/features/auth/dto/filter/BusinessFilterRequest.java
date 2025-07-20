package com.emenu.features.auth.dto.filter;

import com.emenu.enums.BusinessStatus;
import lombok.Data;

@Data
public class BusinessFilterRequest {
    
    private String name;
    private String email;
    private String phone;
    private BusinessStatus status;
    private String search;
    
    // Pagination
    private Integer pageNo = 1;
    private Integer pageSize = 10;
    private String sortBy = "createdAt";
    private String sortDirection = "DESC";
}
