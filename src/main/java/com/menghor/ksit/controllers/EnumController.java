package com.menghor.ksit.controllers;

import com.menghor.ksit.enumations.*;
import com.menghor.ksit.exceptoins.response.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
@RestController
@Slf4j
@RequestMapping("/api/v1/enums")
@Tag(name = "Enum status", description = "All of enumerations")
public class EnumController {

    @GetMapping("/days-of-week")
    public ApiResponse<List<DayOfWeek>> getAllDaysOfWeek() {
        return new ApiResponse<>(
                "Success",
                "Get all enum day of week successfully...!",
                Arrays.asList(DayOfWeek.values())
        );
    }

    @GetMapping("/degree-types")
    public ApiResponse<List<DegreeEnum>> getAllDegreeTypes() {
        return new ApiResponse<>(
                "Success",
                "Get all degree types successfully!",
                Arrays.asList(DegreeEnum.values())
        );
    }

    @GetMapping("/education-levels")
    public ApiResponse<List<EducationLevelEnum>> getAllEducationLevels() {
        return new ApiResponse<>(
                "Success",
                "Get all education levels successfully!",
                Arrays.asList(EducationLevelEnum.values())
        );
    }

    @GetMapping("/genders")
    public ApiResponse<List<GenderEnum>> getAllGenders() {
        return new ApiResponse<>(
                "Success",
                "Get all gender types successfully!",
                Arrays.asList(GenderEnum.values())
        );
    }

    @GetMapping("/parent-types")
    public ApiResponse<List<ParentEnum>> getAllParentTypes() {
        return new ApiResponse<>(
                "Success",
                "Get all parent types successfully!",
                Arrays.asList(ParentEnum.values())
        );
    }

    @GetMapping("/position-types")
    public ApiResponse<List<PositionType>> getAllPositionTypes() {
        return new ApiResponse<>(
                "Success",
                "Get all position types successfully!",
                Arrays.asList(PositionType.values())
        );
    }

    @GetMapping("/roles")
    public ApiResponse<List<RoleEnum>> getAllRoles() {
        return new ApiResponse<>(
                "Success",
                "Get all roles successfully!",
                Arrays.asList(RoleEnum.values())
        );
    }

    @GetMapping("/semesters")
    public ApiResponse<List<SemesterEnum>> getAllSemesters() {
        return new ApiResponse<>(
                "Success",
                "Get all semesters successfully!",
                Arrays.asList(SemesterEnum.values())
        );
    }

    @GetMapping("/semester-statuses")
    public ApiResponse<List<SemesterType>> getAllSemesterStatuses() {
        return new ApiResponse<>(
                "Success",
                "Get all semester statuses successfully!",
                Arrays.asList(SemesterType.values())
        );
    }

    @GetMapping("/statuses")
    public ApiResponse<List<Status>> getAllStatuses() {
        return new ApiResponse<>(
                "Success",
                "Get all statuses successfully!",
                Arrays.asList(Status.values())
        );
    }

    @GetMapping("/year-levels")
    public ApiResponse<List<YearLevelEnum>> getAllYearLevels() {
        return new ApiResponse<>(
                "Success",
                "Get all year levels successfully!",
                Arrays.asList(YearLevelEnum.values())
        );
    }
}
