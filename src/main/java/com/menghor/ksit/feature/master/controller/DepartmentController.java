package com.menghor.ksit.feature.master.controller;

import com.menghor.ksit.exceptoins.response.ApiResponse;
import com.menghor.ksit.feature.master.dto.filter.DepartmentFilter;
import com.menghor.ksit.feature.master.dto.request.DepartmentRequestDto;
import com.menghor.ksit.feature.master.dto.response.DepartmentResponseDto;
import com.menghor.ksit.feature.master.dto.update.DepartmentUpdateDto;
import com.menghor.ksit.feature.master.service.DepartmentService;
import com.menghor.ksit.utils.database.CustomPaginationResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@Slf4j
@RequestMapping("/api/v1/departments")
public class DepartmentController {
    private final DepartmentService departmentService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DEVELOPER', 'STAFF')")
    public ApiResponse<DepartmentResponseDto> create(@Valid @RequestBody DepartmentRequestDto departmentRequestDto) {
        log.info("Received request to create new department: {}", departmentRequestDto);
        DepartmentResponseDto department = departmentService.createDepartment(departmentRequestDto);
        log.info("Department created successfully with ID: {}", department.getId());
        return new ApiResponse<>(
                "Success",
                "Department created successfully...!",
                department
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DEVELOPER', 'STAFF')")
    public ApiResponse<DepartmentResponseDto> getDepartmentById(@PathVariable Long id) {
        log.info("Received request to get department by ID: {}", id);
        DepartmentResponseDto department = departmentService.getDepartmentById(id);
        log.info("Successfully retrieved department with ID: {}", id);
        return new ApiResponse<>(
                "Success",
                "Get department by id "+ id + " successfully...!",
                department
        );
    }

    @PostMapping("/updateById/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DEVELOPER', 'STAFF')")
    public ApiResponse<DepartmentResponseDto> updateById(@Valid @RequestBody DepartmentUpdateDto departmentRequestDto, @PathVariable Long id) {
        log.info("Received request to update department with ID: {}, update data: {}", id, departmentRequestDto);
        DepartmentResponseDto department = departmentService.updateDepartmentById(departmentRequestDto, id);
        log.info("Successfully updated department with ID: {}", id);
        return new ApiResponse<>(
                "Success",
                "Update department by id " + id + " successfully...!",
                department
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DEVELOPER', 'STAFF')")
    public ApiResponse<DepartmentResponseDto> deleteById(@PathVariable Long id) {
        log.info("Received request to delete department with ID: {}", id);
        DepartmentResponseDto department = departmentService.deleteDepartmentById(id);
        log.info("Successfully deleted department with ID: {}", id);
        return new ApiResponse<>(
                "Success",
                "Delete department by id " + id + " successfully...!",
                department
        );
    }

    @PostMapping("/all")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'DEVELOPER', 'STAFF')")
    public ApiResponse<CustomPaginationResponseDto<DepartmentResponseDto>> getAllDepartments(@RequestBody DepartmentFilter filterDto) {
        log.info("Received request to fetch all departments with filter: {}", filterDto);
        CustomPaginationResponseDto<DepartmentResponseDto> department = departmentService.getAllDepartments(filterDto);
        log.info("Successfully fetched {} departments", department.getTotalPages());
        return new ApiResponse<>(
                "Success",
                "All departments fetched successfully...!",
                department
        );
    }
}