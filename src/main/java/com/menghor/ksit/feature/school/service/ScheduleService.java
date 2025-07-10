package com.menghor.ksit.feature.school.service;

import com.menghor.ksit.feature.school.dto.filter.ScheduleFilterDto;
import com.menghor.ksit.feature.school.dto.request.ScheduleBulkDuplicateRequestDto;
import com.menghor.ksit.feature.school.dto.request.ScheduleRequestDto;
import com.menghor.ksit.feature.school.dto.response.ScheduleBulkDuplicateResponseDto;
import com.menghor.ksit.feature.school.dto.response.ScheduleResponseDto;
import com.menghor.ksit.feature.school.dto.update.ScheduleUpdateDto;
import com.menghor.ksit.utils.database.CustomPaginationResponseDto;

import java.util.List;

public interface ScheduleService {

    ScheduleResponseDto createSchedule(ScheduleRequestDto requestDto);

    ScheduleResponseDto getScheduleById(Long id);

    ScheduleResponseDto updateSchedule(Long id, ScheduleUpdateDto updateDto);

    ScheduleResponseDto deleteSchedule(Long id);

    CustomPaginationResponseDto<ScheduleResponseDto> getAllSchedules(ScheduleFilterDto filterDto);

    CustomPaginationResponseDto<ScheduleResponseDto> getMySchedules(ScheduleFilterDto filterDto);

    ScheduleBulkDuplicateResponseDto bulkDuplicateSchedules(ScheduleBulkDuplicateRequestDto requestDto);

    List<ScheduleResponseDto> getAllSchedulesSimple(ScheduleFilterDto filterDto);

    List<ScheduleResponseDto> getMySchedulesSimple(ScheduleFilterDto filterDto);
}