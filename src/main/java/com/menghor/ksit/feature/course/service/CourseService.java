package com.menghor.ksit.feature.course.service;

import com.menghor.ksit.feature.course.dto.request.CourseFilterDto;
import com.menghor.ksit.feature.course.dto.request.CourseRequestDto;
import com.menghor.ksit.feature.course.dto.response.CourseResponseDto;
import com.menghor.ksit.feature.course.dto.response.CourseResponseListDto;
import com.menghor.ksit.utils.database.CustomPaginationResponseDto;

public interface CourseService {

    CourseResponseDto createCourse(CourseRequestDto courseRequestDto);
    CourseResponseDto getCourseById(Long id);
    CourseResponseDto updateById(Long id, CourseRequestDto courseRequestDto);
    CourseResponseDto deleteById(Long id);
    CustomPaginationResponseDto<CourseResponseListDto> getAllCourses(CourseFilterDto courseFilterDto);
}
