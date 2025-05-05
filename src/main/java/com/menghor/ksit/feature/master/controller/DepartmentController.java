package com.menghor.ksit.feature.master.controller;

import com.menghor.ksit.exceptoins.response.ApiResponse;
import com.menghor.ksit.feature.master.dto.department.request.DepartmentFilter;
import com.menghor.ksit.feature.master.dto.department.request.DepartmentRequestDto;
import com.menghor.ksit.feature.master.dto.department.response.DepartmentResponseDto;
import com.menghor.ksit.feature.master.service.DepartmentService;
import com.menghor.ksit.utils.database.CustomPaginationResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@Slf4j
@RequestMapping("/api/v1/departments")
public class DepartmentController {
    private final DepartmentService departmentService;

    @PostMapping
    public ApiResponse<DepartmentResponseDto> create(@Valid @RequestBody DepartmentRequestDto departmentRequestDto) {
        DepartmentResponseDto department = departmentService.createDepartment(departmentRequestDto);
        return new ApiResponse<>(
                "Success",
                "Department created successfully...!",
                department
        );
    }

    @GetMapping("/getDepartmentById/{id}")
    public ApiResponse<DepartmentResponseDto> getDepartmentById(@PathVariable Long id) {
        DepartmentResponseDto department = departmentService.getDepartmentById(id);
        return new ApiResponse<>(
                "Success",
                "Get department by id "+ id + " successfully...!",
                department
        );
    }

    @PostMapping("/updateById/{id}")
    public ApiResponse<DepartmentResponseDto> updateById(@Valid @RequestBody DepartmentRequestDto departmentRequestDto, @PathVariable Long id) {
        DepartmentResponseDto department = departmentService.updateDepartmentById(departmentRequestDto, id);
        return new ApiResponse<>(
                "Success",
                "Update department by id " + id + " successfully...!",
                department

        );
    }

    @DeleteMapping("/deleteById/{id}")
    public ApiResponse<DepartmentResponseDto> deleteById(@PathVariable Long id) {
        DepartmentResponseDto department = departmentService.deleteDepartmentById(id);
        return new ApiResponse<>(
                "Success",
                "Delete department by id " + id + " successfully...!",
                department
        );
    }

    @PostMapping("/getAllDepartments")
    public ApiResponse<CustomPaginationResponseDto<DepartmentResponseDto>> getAllDepartments(@RequestBody DepartmentFilter filterDto) {
        CustomPaginationResponseDto<DepartmentResponseDto> department = departmentService.getAllDepartments(filterDto);
        return new ApiResponse<>(
                "Success",
                "All departments fetched successfully...!",
                department
        );
    }
}
