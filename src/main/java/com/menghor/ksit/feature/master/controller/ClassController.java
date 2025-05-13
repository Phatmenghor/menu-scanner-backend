package com.menghor.ksit.feature.master.controller;

import com.menghor.ksit.exceptoins.response.ApiResponse;
import com.menghor.ksit.feature.master.dto.filter.ClassFilterDto;
import com.menghor.ksit.feature.master.dto.request.ClassRequestDto;
import com.menghor.ksit.feature.master.dto.response.ClassResponseDto;
import com.menghor.ksit.feature.master.dto.update.ClassUpdateDto;
import com.menghor.ksit.feature.master.service.ClassService;
import com.menghor.ksit.utils.database.CustomPaginationResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@Slf4j
@RequestMapping("/api/v1/classes")
public class ClassController {
    private final ClassService classService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DEVELOPER', 'STAFF')")
    public ApiResponse<ClassResponseDto> create(@Valid @RequestBody ClassRequestDto classRequestDto) {
        log.info("Received request to create new class: {}", classRequestDto);
        ClassResponseDto classResponseDto = classService.createClass(classRequestDto);
        log.info("Class created successfully with ID: {}", classResponseDto.getId());
        return new ApiResponse<>(
                "Success",
                "Classes created successfully...!",
                classResponseDto
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DEVELOPER', 'STAFF')")
    public ApiResponse<ClassResponseDto> getClassById(@PathVariable Long id) {
        log.info("Received request to get class by ID: {}", id);
        ClassResponseDto classResponseDto = classService.getClassById(id);
        log.info("Successfully retrieved class with ID: {}", id);
        return new ApiResponse<>(
                "Success",
                "Get class by id "+ id + " successfully...!",
                classResponseDto
        );
    }

    @PostMapping("/updateById/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DEVELOPER', 'STAFF')")
    public ApiResponse<ClassResponseDto> updateById(@PathVariable Long id, @Valid @RequestBody ClassUpdateDto classUpdateDto) {
        log.info("Received request to update class with ID: {}, update data: {}", id, classUpdateDto);
        ClassResponseDto classResponseDto = classService.updateClassById(id, classUpdateDto);
        log.info("Successfully updated class with ID: {}", id);
        return new ApiResponse<>(
                "Success",
                "Update class by id " + id + " successfully...!",
                classResponseDto
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DEVELOPER', 'STAFF')")
    public ApiResponse<ClassResponseDto> deleteById(@PathVariable Long id) {
        log.info("Received request to delete class with ID: {}", id);
        ClassResponseDto classResponseDto = classService.deleteClassById(id);
        log.info("Successfully deleted class with ID: {}", id);
        return new ApiResponse<>(
                "Success",
                "Delete class by id " + id + " successfully...!",
                classResponseDto
        );
    }

    @PostMapping("/all")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DEVELOPER', 'STAFF')")
    public ApiResponse<CustomPaginationResponseDto<ClassResponseDto>> getAllClasses(@RequestBody ClassFilterDto filterDto) {
        log.info("Received request to fetch all classes with filter: {}", filterDto);
        CustomPaginationResponseDto<ClassResponseDto> classResponseList = classService.getAllClasses(filterDto);
        log.info("Successfully fetched {} classes", classResponseList.getTotalPages());
        return new ApiResponse<>(
                "Success",
                "All classes fetched successfully...!",
                classResponseList
        );
    }
}