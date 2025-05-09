package com.menghor.ksit.feature.master.service;

import com.menghor.ksit.feature.master.dto.filter.ClassFilterDto;
import com.menghor.ksit.feature.master.dto.request.ClassRequestDto;
import com.menghor.ksit.feature.master.dto.response.ClassResponseDto;
import com.menghor.ksit.feature.master.dto.response.ClassResponseListDto;
import com.menghor.ksit.feature.master.dto.update.ClassUpdateDto;
import com.menghor.ksit.utils.database.CustomPaginationResponseDto;

public interface ClassService {

    ClassResponseDto createClass(ClassRequestDto classRequestDto);

    ClassResponseDto getClassById(Long id);

    ClassResponseDto updateClassById(Long id, ClassUpdateDto classRequestDto);

    ClassResponseDto deleteClassById(Long id);

    CustomPaginationResponseDto<ClassResponseListDto> getAllClasses(ClassFilterDto filterDto);
}
