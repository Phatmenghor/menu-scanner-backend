package com.menghor.ksit.feature.master.service;

import com.menghor.ksit.feature.master.dto.semester.request.SemesterFilterDto;
import com.menghor.ksit.feature.master.dto.semester.request.SemesterRequestDto;
import com.menghor.ksit.feature.master.dto.semester.response.SemesterResponseDto;
import com.menghor.ksit.utils.database.CustomPaginationResponseDto;

public interface SemesterService {

    SemesterResponseDto createSemester(SemesterRequestDto semesterRequestDto);
    SemesterResponseDto getSemesterById(Long id);
    SemesterResponseDto updateSemesterById(Long id, SemesterRequestDto semesterRequestDto);
    SemesterResponseDto deleteSemesterById(Long id);
    CustomPaginationResponseDto<SemesterResponseDto> getAllSemesters(SemesterFilterDto semesterFilterDto);
}
