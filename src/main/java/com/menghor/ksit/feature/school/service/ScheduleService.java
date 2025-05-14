package com.menghor.ksit.feature.school.service;

import com.menghor.ksit.feature.school.dto.filter.ScheduleFilterDto;
import com.menghor.ksit.feature.school.dto.request.ScheduleRequestDto;
import com.menghor.ksit.feature.school.dto.response.ScheduleResponseDto;
import com.menghor.ksit.feature.school.dto.response.ScheduleResponseListDto;
import com.menghor.ksit.feature.school.dto.update.ScheduleUpdateDto;
import com.menghor.ksit.utils.database.CustomPaginationResponseDto;

import java.util.List;

public interface ScheduleService {

    /**
     * Create a new schedule
     */
    ScheduleResponseDto createSchedule(ScheduleRequestDto requestDto);

    /**
     * Get a schedule by ID
     */
    ScheduleResponseDto getScheduleById(Long id);

    /**
     * Update an existing schedule
     */
    ScheduleResponseDto updateSchedule(Long id, ScheduleUpdateDto updateDto);

    /**
     * Delete a schedule
     */
    ScheduleResponseDto deleteSchedule(Long id);

    /**
     * Get all schedules with filtering
     */
    CustomPaginationResponseDto<ScheduleResponseDto> getAllSchedules(ScheduleFilterDto filterDto);
}