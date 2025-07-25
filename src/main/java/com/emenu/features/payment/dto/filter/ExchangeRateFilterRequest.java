package com.emenu.features.payment.dto.filter;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class ExchangeRateFilterRequest {
    private String search;
    private Boolean isActive;
    
    @Min(value = 1, message = "Page number must be at least 1")
    private Integer pageNo = 1;
    
    @Min(value = 1, message = "Page size must be at least 1")
    @Max(value = 100, message = "Page size cannot exceed 100")
    private Integer pageSize = 10;
    
    private String sortBy = "createdAt";
    private String sortDirection = "DESC";
}