package com.emenu.features.auth.dto.filter;

import com.emenu.enums.user.BusinessStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class BusinessFilterRequest {

    // Search term (searches across business name, email, description)
    private String search;

    // Filter by business status
    private BusinessStatus status;

    // Filter by active subscription status
    private Boolean hasActiveSubscription;

    // Pagination
    @Min(value = 1, message = "Page number must be at least 1")
    private Integer pageNo = 1;

    @Min(value = 1, message = "Page size must be at least 1")
    @Max(value = 100, message = "Page size cannot exceed 100")
    private Integer pageSize = 10;

    private String sortBy = "createdAt";
    private String sortDirection = "DESC";
}
