package com.menghor.ksit.feature.master.service;

import com.menghor.ksit.feature.master.dto.filter.DepartmentFilter;
import com.menghor.ksit.feature.master.dto.request.DepartmentRequestDto;
import com.menghor.ksit.feature.master.dto.response.DepartmentResponseDto;
import com.menghor.ksit.feature.master.dto.update.DepartmentUpdateDto;
import com.menghor.ksit.utils.database.CustomPaginationResponseDto;

public interface DepartmentService {
    DepartmentResponseDto createDepartment(DepartmentRequestDto departmentRequestDto);

    DepartmentResponseDto getDepartmentById(Long id);

    DepartmentResponseDto updateDepartmentById(DepartmentUpdateDto departmentRequestDto, Long id);

    DepartmentResponseDto deleteDepartmentById(Long id);

    CustomPaginationResponseDto<DepartmentResponseDto> getAllDepartments(DepartmentFilter departmentFilter);

    CustomPaginationResponseDto<DepartmentResponseDto> getMyDepartments(DepartmentFilter departmentFilter);
}
