package com.menghor.ksit.feature.school.controller;

import com.menghor.ksit.exceptoins.response.ApiResponse;
import com.menghor.ksit.feature.school.dto.filter.CourseFilterDto;
import com.menghor.ksit.feature.school.dto.request.CourseRequestDto;
import com.menghor.ksit.feature.school.dto.response.CourseResponseDto;
import com.menghor.ksit.feature.school.dto.update.CourseUpdateDto;
import com.menghor.ksit.feature.school.service.CourseService;
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
    public ApiResponse<CourseResponseDto> createCourse(@Valid @RequestBody CourseRequestDto courseRequestDto) {
        log.info("REST request to create course: {}", courseRequestDto);
        CourseResponseDto courseResponseDto = courseService.createCourse(courseRequestDto);
        return new ApiResponse<>(
                "success",
                "Course created successfully",
                courseResponseDto
        );
    }

    @GetMapping("/{id}")
    public ApiResponse<CourseResponseDto> getCourseById(@PathVariable Long id) {
        log.info("REST request to get course by ID: {}", id);
        CourseResponseDto courseResponseDto = courseService.getCourseById(id);
        return new ApiResponse<>(
                "success",
                "Course retrieved successfully",
                courseResponseDto
        );
    }

    @PostMapping("/updateById/{id}")
    public ApiResponse<CourseResponseDto> updateCourse(@PathVariable Long id, @Valid @RequestBody CourseUpdateDto courseRequestDto) {
        log.info("REST request to update course with ID {}: {}", id, courseRequestDto);
        CourseResponseDto courseResponseDto = courseService.updateById(id, courseRequestDto);
        return new ApiResponse<>(
                "success",
                "Course updated successfully",
                courseResponseDto
        );
    }

    @DeleteMapping("/{id}")
    public ApiResponse<CourseResponseDto> deleteCourse(@PathVariable Long id) {
        log.info("REST request to delete course with ID: {}", id);
        CourseResponseDto courseResponseDto = courseService.deleteById(id);
        return new ApiResponse<>(
                "success",
                "Course deleted successfully",
                courseResponseDto
        );
    }

    @PostMapping("/all")
    public ApiResponse<CustomPaginationResponseDto<CourseResponseDto>> getAllCourses(@RequestBody CourseFilterDto filterDto) {
        log.info("REST request to search courses with filter: {}", filterDto);
        CustomPaginationResponseDto<CourseResponseDto> responseDto = courseService.getAllCourses(filterDto);
        return new ApiResponse<>(
                "success",
                "Courses retrieved successfully",
                responseDto
        );
    }
}