package com.menghor.ksit.feature.master.service;

import com.menghor.ksit.feature.master.dto.major.request.MajorFilterDto;
import com.menghor.ksit.feature.master.dto.major.request.MajorRequestDto;
import com.menghor.ksit.feature.master.dto.major.response.MajorResponseDto;
import com.menghor.ksit.feature.master.dto.major.response.MajorResponseListDto;
import com.menghor.ksit.utils.database.CustomPaginationResponseDto;

public interface MajorService {

    MajorResponseDto createMajor(MajorRequestDto majorRequestDto);

    MajorResponseDto getMajorById(Long id);

    MajorResponseDto updateMajorById(Long id, MajorRequestDto majorRequestDto);

    MajorResponseDto deleteMajorById(Long id);

    CustomPaginationResponseDto<MajorResponseListDto> getAllMajors(MajorFilterDto filterDto);
}
