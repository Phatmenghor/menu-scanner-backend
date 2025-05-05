package com.menghor.ksit.feature.master.dto.semester.request;

import com.menghor.ksit.enumations.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class SemesterRequestDto {
    @NotBlank(message = "Semester name is required")
    private String name;

    @Schema(example = "")
    private LocalDate startDate;

    @Schema(example = "")
    private LocalDate endDate;

    @NotNull(message = "Semester academy year is required")
    @Min(value = 1000, message = "Must be 4 digits")
    @Max(value = 9999, message = "Must be 4 digits")
    @Schema(example = "0000")
    private Integer academyYear;

    private Status status = Status.ACTIVE;
}
