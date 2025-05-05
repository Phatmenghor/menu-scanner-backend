package com.menghor.ksit.feature.master.service;

import com.menghor.ksit.feature.master.dto.department.request.DepartmentFilter;
import com.menghor.ksit.feature.master.dto.department.request.DepartmentRequestDto;
import com.menghor.ksit.feature.master.dto.department.response.DepartmentResponseDto;
import com.menghor.ksit.utils.database.CustomPaginationResponseDto;

public interface DepartmentService {

    DepartmentResponseDto createDepartment(DepartmentRequestDto departmentRequestDto);
    DepartmentResponseDto getDepartmentById(Long id);
    DepartmentResponseDto updateDepartmentById(DepartmentRequestDto departmentRequestDto, Long id);
    DepartmentResponseDto deleteDepartmentById(Long id);
    CustomPaginationResponseDto<DepartmentResponseDto> getAllDepartments(DepartmentFilter departmentFilter);
}
