package com.menghor.ksit.feature.master.service;

import com.menghor.ksit.feature.master.dto.filter.SemesterFilterDto;
import com.menghor.ksit.feature.master.dto.request.SemesterRequestDto;
import com.menghor.ksit.feature.master.dto.response.SemesterResponseDto;
import com.menghor.ksit.feature.master.dto.update.SemesterUpdateDto;
import com.menghor.ksit.utils.database.CustomPaginationResponseDto;

public interface SemesterService {

    SemesterResponseDto createSemester(SemesterRequestDto semesterRequestDto);

    SemesterResponseDto getSemesterById(Long id);

    SemesterResponseDto updateSemesterById(Long id, SemesterUpdateDto semesterRequestDto);

    SemesterResponseDto deleteSemesterById(Long id);

    CustomPaginationResponseDto<SemesterResponseDto> getAllSemesters(SemesterFilterDto semesterFilterDto);
}
