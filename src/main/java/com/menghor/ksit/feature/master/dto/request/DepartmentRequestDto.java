package com.menghor.ksit.feature.master.dto.request;

import com.menghor.ksit.enumations.Status;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DepartmentRequestDto {
    @NotBlank(message = "Department code is required")
    private String code;

    @NotBlank(message = "Department name is required")
    private String name;
    private String urlLogo;

    private Status status = Status.ACTIVE;
}
