package com.menghor.ksit.feature.master.dto.request;

import com.menghor.ksit.enumations.SemesterEnum;
import com.menghor.ksit.enumations.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class SemesterRequestDto {

    @Schema(example = "")
    private LocalDate startDate;

    @Schema(example = "")
    private LocalDate endDate;

    @NotNull(message = "SemesterEnum academy year is required")
    @Min(value = 1000, message = "Must be 4 digits")
    @Max(value = 9999, message = "Must be 4 digits")
    @Schema(example = "0000")
    private Integer academyYear;

    private SemesterEnum semester;

    private Status status = Status.ACTIVE;
}
