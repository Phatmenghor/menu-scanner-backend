package com.menghor.ksit.feature.course.controller;

import com.menghor.ksit.exceptoins.response.ApiResponse;
import com.menghor.ksit.feature.course.dto.request.CourseFilterDto;
import com.menghor.ksit.feature.course.dto.request.CourseRequestDto;
import com.menghor.ksit.feature.course.dto.response.CourseResponseDto;
import com.menghor.ksit.feature.course.dto.response.CourseResponseListDto;
import com.menghor.ksit.feature.course.service.CourseService;
import com.menghor.ksit.utils.database.CustomPaginationResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/courses")
public class CourseController {
    private final CourseService courseService;

    @PostMapping
    public ApiResponse<CourseResponseDto> create(@Valid @RequestBody CourseRequestDto courseRequestDto) {
        CourseResponseDto courseResponseDto = courseService.createCourse(courseRequestDto);
        return new ApiResponse<>(
                "Success",
                "Courses created successfully...!",
                courseResponseDto
        );
    }

    @GetMapping("/getCourseById/{id}")
    public ApiResponse<CourseResponseDto> getCourseById(@PathVariable Long id) {
        CourseResponseDto courseResponseDto = courseService.getCourseById(id);
        return new ApiResponse<>(
                "Success",
                "Get course by id "+ id + " successfully...!",
                courseResponseDto
        );
    }

    @PostMapping("/updateById/{id}")
    public ApiResponse<CourseResponseDto> updateById(@PathVariable Long id, @Valid @RequestBody CourseRequestDto courseRequestDto) {
        CourseResponseDto courseResponseDto = courseService.updateById(id, courseRequestDto);
        return new ApiResponse<>(
                "Success",
                "Update course by id " + id + " successfully...!",
                courseResponseDto
        );
    }

    @DeleteMapping("/deleteById/{id}")
    public ApiResponse<CourseResponseDto> deleteById(@PathVariable Long id) {
        CourseResponseDto courseResponseDto = courseService.deleteById(id);
        return new ApiResponse<>(
                "Success",
                "Delete course by id " + id + " successfully...!",
                courseResponseDto
        );
    }

    @PostMapping("/get-all-courses")
    public ApiResponse<CustomPaginationResponseDto<CourseResponseListDto>> getAllCourses(@RequestBody CourseFilterDto courseFilterDto) {
        CustomPaginationResponseDto<CourseResponseListDto> responseDto = courseService.getAllCourses(courseFilterDto);
        return new ApiResponse<>(
                "Success",
                "All courses fetched successfully...!",
                responseDto
        );
    }
}
