package com.emenu.features.attendance.service.impl;

import com.emenu.exception.ResourceNotFoundException;
import com.emenu.features.attendance.dto.request.WorkScheduleCreateRequest;
import com.emenu.features.attendance.dto.response.WorkScheduleResponse;
import com.emenu.features.attendance.dto.update.WorkScheduleUpdateRequest;
import com.emenu.features.attendance.mapper.WorkScheduleMapper;
import com.emenu.features.attendance.models.AttendancePolicy;
import com.emenu.features.attendance.models.WorkSchedule;
import com.emenu.features.attendance.repository.AttendancePolicyRepository;
import com.emenu.features.attendance.repository.WorkScheduleRepository;
import com.emenu.features.attendance.service.WorkScheduleService;
import com.emenu.features.auth.models.User;
import com.emenu.features.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkScheduleServiceImpl implements WorkScheduleService {

    private final WorkScheduleRepository scheduleRepository;
    private final UserRepository userRepository;
    private final AttendancePolicyRepository policyRepository;
    private final WorkScheduleMapper scheduleMapper;

    @Override
    @Transactional
    public WorkScheduleResponse createSchedule(WorkScheduleCreateRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getUserId()));

        AttendancePolicy policy = policyRepository.findById(request.getPolicyId())
                .orElseThrow(() -> new ResourceNotFoundException("Attendance policy not found with id: " + request.getPolicyId()));

        WorkSchedule schedule = scheduleMapper.toEntity(request, user, policy);
        WorkSchedule savedSchedule = scheduleRepository.save(schedule);

        return scheduleMapper.toResponse(savedSchedule);
    }

    @Override
    @Transactional
    public WorkScheduleResponse updateSchedule(Long id, WorkScheduleUpdateRequest request) {
        WorkSchedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Work schedule not found with id: " + id));

        if (request.getScheduleName() != null) schedule.setScheduleName(request.getScheduleName());
        if (request.getScheduleType() != null) schedule.setScheduleType(request.getScheduleType());
        if (request.getWorkDays() != null) schedule.setWorkDays(request.getWorkDays());
        if (request.getCustomStartTime() != null) schedule.setCustomStartTime(request.getCustomStartTime());
        if (request.getCustomEndTime() != null) schedule.setCustomEndTime(request.getCustomEndTime());
        if (request.getIsActive() != null) schedule.setIsActive(request.getIsActive());

        WorkSchedule updatedSchedule = scheduleRepository.save(schedule);
        return scheduleMapper.toResponse(updatedSchedule);
    }

    @Override
    @Transactional(readOnly = true)
    public WorkScheduleResponse getScheduleById(Long id) {
        WorkSchedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Work schedule not found with id: " + id));

        return scheduleMapper.toResponse(schedule);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkScheduleResponse> getSchedulesByUserId(Long userId) {
        return scheduleRepository.findByUserId(userId).stream()
                .map(scheduleMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkScheduleResponse> getSchedulesByBusinessId(Long businessId) {
        return scheduleRepository.findByBusinessId(businessId).stream()
                .map(scheduleMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<WorkScheduleResponse> getAllSchedules(Pageable pageable) {
        return scheduleRepository.findAll(pageable)
                .map(scheduleMapper::toResponse);
    }

    @Override
    @Transactional
    public void deleteSchedule(Long id) {
        if (!scheduleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Work schedule not found with id: " + id);
        }
        scheduleRepository.deleteById(id);
    }
}
