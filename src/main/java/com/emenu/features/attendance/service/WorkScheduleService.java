package com.emenu.features.attendance.service;

import com.emenu.features.attendance.dto.request.WorkScheduleCreateRequest;
import com.emenu.features.attendance.dto.response.WorkScheduleResponse;
import com.emenu.features.attendance.dto.update.WorkScheduleUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface WorkScheduleService {

    WorkScheduleResponse createSchedule(WorkScheduleCreateRequest request);

    WorkScheduleResponse updateSchedule(Long id, WorkScheduleUpdateRequest request);

    WorkScheduleResponse getScheduleById(Long id);

    List<WorkScheduleResponse> getSchedulesByUserId(Long userId);

    List<WorkScheduleResponse> getSchedulesByBusinessId(Long businessId);

    Page<WorkScheduleResponse> getAllSchedules(Pageable pageable);

    void deleteSchedule(Long id);
}
