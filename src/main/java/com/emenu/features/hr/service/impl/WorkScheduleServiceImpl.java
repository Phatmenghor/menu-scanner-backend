package com.emenu.features.hr.service.impl;

import com.emenu.features.auth.mapper.UserMapper;
import com.emenu.features.setting.repository.WorkScheduleTypeEnumRepository;
import com.emenu.features.hr.dto.filter.WorkScheduleFilterRequest;
import com.emenu.features.hr.dto.request.WorkScheduleCreateRequest;
import com.emenu.features.hr.dto.response.WorkScheduleResponse;
import com.emenu.features.hr.dto.update.WorkScheduleUpdateRequest;
import com.emenu.features.hr.mapper.WorkScheduleMapper;
import com.emenu.features.hr.models.WorkSchedule;
import com.emenu.features.hr.repository.WorkScheduleRepository;
import com.emenu.features.hr.service.WorkScheduleService;
import com.emenu.exception.custom.ResourceNotFoundException;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.mapper.PaginationMapper;
import com.emenu.shared.pagination.PaginationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class WorkScheduleServiceImpl implements WorkScheduleService {

    private final WorkScheduleRepository repository;
    private final WorkScheduleTypeEnumRepository typeEnumRepository;
    private final WorkScheduleMapper mapper;
    private final PaginationMapper paginationMapper;
    private final UserMapper userMapper;

    /**
     * Creates a new work schedule for an employee
     */
    @Override
    public WorkScheduleResponse create(WorkScheduleCreateRequest request) {
        log.info("Creating work schedule for user: {}", request.getUserId());

        final WorkSchedule schedule = mapper.toEntity(request);

        schedule.setScheduleTypeEnum(request.getScheduleTypeEnumName());

        WorkSchedule savedSchedule = repository.save(schedule);
        return enrichResponse(mapper.toResponse(savedSchedule), savedSchedule);
    }

    /**
     * Retrieves a work schedule by ID
     */
    @Override
    @Transactional(readOnly = true)
    public WorkScheduleResponse getById(UUID id) {
        WorkSchedule schedule = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Work schedule not found"));
        return enrichResponse(mapper.toResponse(schedule), schedule);
    }

    /**
     * Retrieves all work schedules with filtering and pagination support
     */
    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<WorkScheduleResponse> getAll(WorkScheduleFilterRequest filter) {
        Pageable pageable = PaginationUtils.createPageable(
                filter.getPageNo(),
                filter.getPageSize(),
                filter.getSortBy(),
                filter.getSortDirection()
        );

        Page<WorkSchedule> page = repository.findWithFilters(
                filter.getBusinessId(),
                filter.getUserId(),
                filter.getSearch(),
                pageable
        );

        return paginationMapper.toPaginationResponse(page,
                schedules -> schedules.stream()
                        .map(s -> enrichResponse(mapper.toResponse(s), s))
                        .toList());
    }

    /**
     * Retrieves all work schedules for a specific user
     */
    @Override
    @Transactional(readOnly = true)
    public List<WorkScheduleResponse> getByUserId(UUID userId) {
        List<WorkSchedule> schedules = repository.findByUserIdAndIsDeletedFalse(userId);
        return schedules.stream()
                .map(s -> enrichResponse(mapper.toResponse(s), s))
                .toList();
    }

    /**
     * Updates an existing work schedule
     */
    @Override
    public WorkScheduleResponse update(UUID id, WorkScheduleUpdateRequest request) {
        final WorkSchedule schedule = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Work schedule not found"));

        schedule.setScheduleTypeEnum(request.getScheduleTypeEnumName());

        mapper.updateEntity(request, schedule);
        WorkSchedule updatedSchedule = repository.save(schedule);
        return enrichResponse(mapper.toResponse(updatedSchedule), updatedSchedule);
    }

    /**
     * Soft deletes a work schedule
     */
    @Override
    public WorkScheduleResponse delete(UUID id) {
        WorkSchedule schedule = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Work schedule not found"));
        schedule.softDelete();
        schedule = repository.save(schedule);
        return enrichResponse(mapper.toResponse(schedule), schedule);
    }

    private WorkScheduleResponse enrichResponse(WorkScheduleResponse response, WorkSchedule schedule) {
        response.setUserInfo(userMapper.toUserBasicInfo(schedule.getUser()));
        return response;
    }
}