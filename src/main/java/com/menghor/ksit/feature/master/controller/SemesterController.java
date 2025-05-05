package com.menghor.ksit.feature.master.controller;

import com.menghor.ksit.exceptoins.response.ApiResponse;
import com.menghor.ksit.feature.master.dto.semester.request.SemesterFilterDto;
import com.menghor.ksit.feature.master.dto.semester.request.SemesterRequestDto;
import com.menghor.ksit.feature.master.dto.semester.response.SemesterResponseDto;
import com.menghor.ksit.feature.master.service.SemesterService;
import com.menghor.ksit.utils.database.CustomPaginationResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@Slf4j
@RequestMapping("/api/v1/semesters")
public class SemesterController {
    private final SemesterService semesterService;

    @PostMapping
    public ApiResponse<SemesterResponseDto> create(@Valid @RequestBody SemesterRequestDto semesterRequestDto) {
        SemesterResponseDto semesterResponseDto = semesterService.createSemester(semesterRequestDto);
        return new ApiResponse<>(
                "Success",
                "Semester created successfully...!",
                semesterResponseDto
        );
    }

    @PostMapping("/getSemesterById/{id}")
    public ApiResponse<SemesterResponseDto> getSemesterById(@PathVariable Long id) {
        SemesterResponseDto semesterResponseDto = semesterService.getSemesterById(id);
        return new ApiResponse<>(
                "Success",
                "Get semester by id "+ id + " successfully...!",
                semesterResponseDto
        );
    }

    @PostMapping("/updateSemesterById/{id}")
    public ApiResponse<SemesterResponseDto> updateSemesterById(@PathVariable Long id, @Valid @RequestBody SemesterRequestDto semesterRequestDto) {
        SemesterResponseDto semesterResponseDto = semesterService.updateSemesterById(id, semesterRequestDto);
        return new ApiResponse<>(
                "Success",
                "Update semester by id " + id + " successfully...!",
                semesterResponseDto
        );
    }

    @DeleteMapping("/deleteSemesterById/{id}")
    public ApiResponse<SemesterResponseDto> deleteSemesterById(@PathVariable Long id) {
        SemesterResponseDto semesterResponseDto = semesterService.deleteSemesterById(id);
        return new ApiResponse<>(
                "Success",
                "Delete semester by id " + id + " successfully...!",
                semesterResponseDto
        );
    }

    @PostMapping("/getAllSemesters")
    public ApiResponse<CustomPaginationResponseDto<SemesterResponseDto>> getAllSemesters(@RequestBody SemesterFilterDto semesterFilterDto) {
        CustomPaginationResponseDto<SemesterResponseDto> paginationResponseDto = semesterService.getAllSemesters(semesterFilterDto);
        return new ApiResponse<>(
                "Success",
                "All semesters fetched successfully...!",
                paginationResponseDto
        );
    }
}
