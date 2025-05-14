package com.menghor.ksit.feature.master.controller;

import com.menghor.ksit.exceptoins.response.ApiResponse;
import com.menghor.ksit.feature.master.dto.filter.SemesterFilterDto;
import com.menghor.ksit.feature.master.dto.request.SemesterRequestDto;
import com.menghor.ksit.feature.master.dto.response.SemesterResponseDto;
import com.menghor.ksit.feature.master.dto.update.SemesterUpdateDto;
import com.menghor.ksit.feature.master.service.SemesterService;
import com.menghor.ksit.utils.database.CustomPaginationResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@Slf4j
@RequestMapping("/api/v1/semesters")
public class SemesterController {
    private final SemesterService semesterService;

    @PostMapping
    public ApiResponse<SemesterResponseDto> create(@Valid @RequestBody SemesterRequestDto semesterRequestDto) {
        log.info("Received request to create new semester: {}", semesterRequestDto);
        SemesterResponseDto semesterResponseDto = semesterService.createSemester(semesterRequestDto);
        log.info("Semester created successfully with ID: {}", semesterResponseDto.getId());
        return new ApiResponse<>(
                "success",
                "SemesterEnum created successfully...!",
                semesterResponseDto
        );
    }

    @GetMapping("/{id}")
    public ApiResponse<SemesterResponseDto> getSemesterById(@PathVariable Long id) {
        log.info("Received request to get semester by ID: {}", id);
        SemesterResponseDto semesterResponseDto = semesterService.getSemesterById(id);
        log.info("Successfully retrieved semester with ID: {}", id);
        return new ApiResponse<>(
                "success",
                "Get semester by id " + id + " successfully...!",
                semesterResponseDto
        );
    }

    @PostMapping("/updateSemesterById/{id}")
    public ApiResponse<SemesterResponseDto> updateSemesterById(@PathVariable Long id, @Valid @RequestBody SemesterUpdateDto semesterRequestDto) {
        log.info("Received request to update semester with ID: {}, update data: {}", id, semesterRequestDto);
        SemesterResponseDto semesterResponseDto = semesterService.updateSemesterById(id, semesterRequestDto);
        log.info("Successfully updated semester with ID: {}", id);
        return new ApiResponse<>(
                "success",
                "Update semester by id " + id + " successfully...!",
                semesterResponseDto
        );
    }

    @DeleteMapping("/{id}")
    public ApiResponse<SemesterResponseDto> deleteSemesterById(@PathVariable Long id) {
        log.info("Received request to delete semester with ID: {}", id);
        SemesterResponseDto semesterResponseDto = semesterService.deleteSemesterById(id);
        log.info("Successfully deleted semester with ID: {}", id);
        return new ApiResponse<>(
                "success",
                "Delete semester by id " + id + " successfully...!",
                semesterResponseDto
        );
    }

    @PostMapping("/all")
    public ApiResponse<CustomPaginationResponseDto<SemesterResponseDto>> getAllSemesters(@RequestBody SemesterFilterDto semesterFilterDto) {
        log.info("Received request to fetch all semesters with filter: {}", semesterFilterDto);
        CustomPaginationResponseDto<SemesterResponseDto> paginationResponseDto = semesterService.getAllSemesters(semesterFilterDto);
        log.info("Successfully fetched {} semesters", paginationResponseDto.getTotalPages());
        return new ApiResponse<>(
                "success",
                "All semesters fetched successfully...!",
                paginationResponseDto
        );
    }
}