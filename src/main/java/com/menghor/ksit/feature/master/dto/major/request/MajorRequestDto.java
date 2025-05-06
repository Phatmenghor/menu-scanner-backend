package com.menghor.ksit.feature.master.dto.major.request;

import com.menghor.ksit.enumations.Status;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MajorRequestDto {
    @NotBlank(message = "Major code is required")
    private String code;

    @NotBlank(message = "Major name is required")
    private String name;

    @NotBlank(message = "Department id is required in major")
    private Long departmentId;

    private Status status = Status.ACTIVE;
}
