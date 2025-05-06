package com.menghor.ksit.feature.master.service;

import com.menghor.ksit.feature.master.dto.classes.request.ClassFilterDto;
import com.menghor.ksit.feature.master.dto.classes.request.ClassRequestDto;
import com.menghor.ksit.feature.master.dto.classes.response.ClassResponseDto;
import com.menghor.ksit.feature.master.dto.classes.response.ClassResponseListDto;
import com.menghor.ksit.utils.database.CustomPaginationResponseDto;

public interface ClassService {

    ClassResponseDto createClass(ClassRequestDto classRequestDto);
    ClassResponseDto getClassById(Long id);
    ClassResponseDto updateClassById(Long id,ClassRequestDto classRequestDto);
    ClassResponseDto deleteClassById(Long id);
    CustomPaginationResponseDto<ClassResponseListDto> getAllClasses(ClassFilterDto filterDto);
}
