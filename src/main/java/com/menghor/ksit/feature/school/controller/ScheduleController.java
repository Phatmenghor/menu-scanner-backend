package com.menghor.ksit.feature.school.controller;

import com.menghor.ksit.exceptoins.response.ApiResponse;
import com.menghor.ksit.feature.school.dto.filter.ScheduleFilterDto;
import com.menghor.ksit.feature.school.dto.request.ScheduleRequestDto;
import com.menghor.ksit.feature.school.dto.response.ScheduleResponseDto;
import com.menghor.ksit.feature.school.dto.update.ScheduleUpdateDto;
import com.menghor.ksit.feature.school.service.ScheduleService;
import com.menghor.ksit.utils.database.CustomPaginationResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/schedules")
@RequiredArgsConstructor
@Slf4j
public class ScheduleController {
    private final ScheduleService scheduleService;

    @PostMapping
    public ApiResponse<ScheduleResponseDto> createSchedule(@Valid @RequestBody ScheduleRequestDto requestDto) {
        log.info("REST request to create schedule: {}", requestDto);
        ScheduleResponseDto responseDto = scheduleService.createSchedule(requestDto);
        log.info("Schedule created successfully: {}", responseDto);
        return new ApiResponse<>(
                "success",
                "Schedule created successfully",
                responseDto
        );
    }

    @GetMapping("/{id}")
    public ApiResponse<ScheduleResponseDto> getScheduleById(@PathVariable Long id) {
        log.info("REST request to get schedule by ID: {}", id);
        ScheduleResponseDto responseDto = scheduleService.getScheduleById(id);
        log.info("Schedule retrieved successfully: {}", responseDto);
        return new ApiResponse<>(
                "success",
                "Schedule retrieved successfully",
                responseDto
        );
    }

    @PostMapping("/updateById/{id}")
    public ApiResponse<ScheduleResponseDto> updateSchedule(@PathVariable Long id, @Valid @RequestBody ScheduleUpdateDto updateDto) {
        log.info("REST request to update schedule with ID {}: {}", id, updateDto);
        ScheduleResponseDto responseDto = scheduleService.updateSchedule(id, updateDto);
        log.info("Schedule updated successfully: {}", responseDto);
        return new ApiResponse<>(
                "success",
                "Schedule updated successfully",
                responseDto
        );
    }

    @DeleteMapping("/{id}")
    public ApiResponse<ScheduleResponseDto> deleteSchedule(@PathVariable Long id) {
        log.info("REST request to delete schedule with ID: {}", id);
        ScheduleResponseDto responseDto = scheduleService.deleteSchedule(id);
        log.info("Schedule deleted successfully: {}", responseDto);
        return new ApiResponse<>(
                "success",
                "Schedule deleted successfully",
                responseDto
        );
    }

    @PostMapping("/all")
    public ApiResponse<CustomPaginationResponseDto<ScheduleResponseDto>> getAllSchedules(@RequestBody ScheduleFilterDto filterDto) {
        log.info("REST request to search schedules with filter: {}", filterDto);
        CustomPaginationResponseDto<ScheduleResponseDto> responseDto = scheduleService.getAllSchedules(filterDto);
        log.info("Schedules retrieved successfully: {}", responseDto);
        return new ApiResponse<>(
                "success",
                "Schedules retrieved successfully",
                responseDto
        );
    }

    @PostMapping("/my-schedules")
    public ApiResponse<CustomPaginationResponseDto<ScheduleResponseDto>> getMySchedules(@RequestBody ScheduleFilterDto filterDto) {
        log.info("REST request to get user-specific schedules with filter: {}", filterDto);
        CustomPaginationResponseDto<ScheduleResponseDto> responseDto = scheduleService.getMySchedules(filterDto);
        log.info("User schedules retrieved successfully: {}", responseDto);
        return new ApiResponse<>(
                "success",
                "User schedules retrieved successfully",
                responseDto
        );
    }
}