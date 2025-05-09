package com.menghor.ksit.feature.master.dto.update;

import com.menghor.ksit.enumations.DegreeEnum;
import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.enumations.YearLevelEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ClassUpdateDto {
    private String code;
    private Long majorId;
    @Min(value = 1000, message = "Must be 4 digits")
    @Max(value = 9999, message = "Must be 4 digits")
    @Schema(example = "0000")
    private Integer academyYear;
    private DegreeEnum degree;
    private YearLevelEnum yearLevel;
    private Status status = Status.ACTIVE;
}
