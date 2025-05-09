package com.menghor.ksit.feature.master.controller;

import com.menghor.ksit.exceptoins.response.ApiResponse;
import com.menghor.ksit.feature.master.dto.filter.SubjectFilterDto;
import com.menghor.ksit.feature.master.dto.request.SubjectRequestDto;
import com.menghor.ksit.feature.master.dto.response.SubjectResponseDto;
import com.menghor.ksit.feature.master.service.SubjectService;
import com.menghor.ksit.utils.database.CustomPaginationResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@Slf4j
@RequestMapping("/api/v1/subjects")
public class SubjectController {
    private final SubjectService subjectService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DEVELOPER', 'STAFF')")
    public ApiResponse<SubjectResponseDto> create(@Valid @RequestBody SubjectRequestDto subjectRequestDto) {
        log.info("Received request to create new subject: {}", subjectRequestDto);
        SubjectResponseDto subjectResponseDto = subjectService.createSubject(subjectRequestDto);
        log.info("Subject created successfully with ID: {}", subjectResponseDto.getId());
        return new ApiResponse<>(
                "Success",
                "Subject created successfully...!",
                subjectResponseDto
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DEVELOPER', 'STAFF')")
    public ApiResponse<SubjectResponseDto> getSubjectById(@PathVariable Long id) {
        log.info("Received request to get subject by ID: {}", id);
        SubjectResponseDto subjectResponseDto = subjectService.getSubjectById(id);
        log.info("Successfully retrieved subject with ID: {}", id);
        return new ApiResponse<>(
                "Success",
                "Get subject by id "+ id + " successfully...!",
                subjectResponseDto
        );
    }

    @PostMapping("/updateById/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DEVELOPER', 'STAFF')")
    public ApiResponse<SubjectResponseDto> updateById(@Valid @RequestBody SubjectRequestDto subjectRequestDto, @PathVariable Long id) {
        log.info("Received request to update subject with ID: {}, update data: {}", id, subjectRequestDto);
        SubjectResponseDto subjectResponseDto = subjectService.updateSubjectById(subjectRequestDto, id);
        log.info("Successfully updated subject with ID: {}", id);
        return new ApiResponse<>(
                "Success",
                "Update subject by id " + id + " successfully...!",
                subjectResponseDto
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DEVELOPER', 'STAFF')")
    public ApiResponse<SubjectResponseDto> deleteById(@PathVariable Long id) {
        log.info("Received request to delete subject with ID: {}", id);
        SubjectResponseDto subjectResponseDto = subjectService.deleteSubjectById(id);
        log.info("Successfully deleted subject with ID: {}", id);
        return new ApiResponse<>(
                "Success",
                "Delete subject by id " + id + " successfully...!"
                , subjectResponseDto
        );
    }

    @PostMapping("/all")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DEVELOPER', 'STAFF')")
    public ApiResponse<CustomPaginationResponseDto<SubjectResponseDto>> getAllSubjects(@RequestBody SubjectFilterDto filterDto) {
        log.info("Received request to fetch all subjects with filter: {}", filterDto);
        CustomPaginationResponseDto<SubjectResponseDto> allSubjects = subjectService.getAllSubjects(filterDto);
        log.info("Successfully fetched {} subjects", allSubjects.getTotalPages());
        return new ApiResponse<>(
                "Success",
                "All subjects fetched successfully...!"
                , allSubjects
        );
    }
}