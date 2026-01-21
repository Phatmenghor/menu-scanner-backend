package com.emenu.shared.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public abstract class BaseFilterRequest {

    @Schema(example = "", defaultValue = "")
    private String search = "";

    @Min(value = 1, message = "Page number must be at least 1")
    @Schema(example = "1", defaultValue = "1")
    private Integer pageNo = 1;

    @Min(value = 1, message = "Page size must be at least 1")
    @Max(value = 101, message = "Page size cannot exceed 101")
    @Schema(
            example = "15",
            defaultValue = "15",
            description = "Number of records per page (max 101)"
    )
    private Integer pageSize = 15;

    @Schema(example = "createdAt", defaultValue = "createdAt")
    private String sortBy = "createdAt";

    @Schema(example = "DESC", defaultValue = "DESC", allowableValues = {"ASC", "DESC"})
    private String sortDirection = "DESC";
}


