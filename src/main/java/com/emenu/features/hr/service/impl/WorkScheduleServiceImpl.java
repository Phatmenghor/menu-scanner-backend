package com.emenu.features.hr.service.impl;

import com.emenu.features.enums.repository.WorkScheduleTypeEnumRepository;
import com.emenu.features.hr.dto.filter.WorkScheduleFilterRequest;
import com.emenu.features.hr.dto.request.WorkScheduleCreateRequest;
import com.emenu.features.hr.dto.response.WorkScheduleResponse;
import com.emenu.features.hr.dto.update.WorkScheduleUpdateRequest;
import com.emenu.features.hr.mapper.WorkScheduleMapper;
import com.emenu.features.hr.models.WorkSchedule;
import com.emenu.features.hr.repository.WorkScheduleRepository;
import com.emenu.features.hr.service.WorkScheduleService;
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
    
    @Override
    public WorkScheduleResponse create(WorkScheduleCreateRequest request) {
        log.info("Creating work schedule for user: {}", request.getUserId());
        
        WorkSchedule schedule = mapper.toEntity(request);
        
        // Resolve schedule type enum if provided
        if (request.getScheduleTypeEnumName() != null) {
            typeEnumRepository.findByBusinessIdAndEnumNameAndIsDeletedFalse(
                    request.getBusinessId(), request.getScheduleTypeEnumName())
                    .ifPresent(typeEnum -> schedule.setScheduleTypeEnumId(typeEnum.getId()));
        }
        
        schedule = repository.save(schedule);
        return enrichResponse(mapper.toResponse(schedule));
    }
    
    @Override
    @Transactional(readOnly = true)
    public WorkScheduleResponse getById(UUID id) {
        WorkSchedule schedule = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Work schedule not found"));
        return enrichResponse(mapper.toResponse(schedule));
    }
    
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
                filter.getPolicyId(),
                filter.getSearch(),
                pageable
        );
        
        return paginationMapper.toPaginationResponse(page,
                schedules -> schedules.stream()
                        .map(mapper::toResponse)
                        .map(this::enrichResponse)
                        .toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<WorkScheduleResponse> getByUserId(UUID userId) {
        List<WorkSchedule> schedules = repository.findByUserIdAndIsDeletedFalse(userId);
        return schedules.stream()
                .map(mapper::toResponse)
                .map(this::enrichResponse)
                .toList();
    }
    
    @Override
    public WorkScheduleResponse update(UUID id, WorkScheduleUpdateRequest request) {
        WorkSchedule schedule = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Work schedule not found"));
        
        // Resolve schedule type enum if provided
        if (request.getScheduleTypeEnumName() != null) {
            typeEnumRepository.findByBusinessIdAndEnumNameAndIsDeletedFalse(
                    schedule.getBusinessId(), request.getScheduleTypeEnumName())
                    .ifPresent(typeEnum -> schedule.setScheduleTypeEnumId(typeEnum.getId()));
        }
        
        mapper.updateEntity(request, schedule);
        schedule = repository.save(schedule);
        return enrichResponse(mapper.toResponse(schedule));
    }
    
    @Override
    public void delete(UUID id) {
        WorkSchedule schedule = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Work schedule not found"));
        schedule.softDelete();
        repository.save(schedule);
    }
    
    private WorkScheduleResponse enrichResponse(WorkScheduleResponse response) {
        if (response.getScheduleTypeEnumId() != null) {
            typeEnumRepository.findByIdAndIsDeletedFalse(response.getScheduleTypeEnumId())
                    .ifPresent(typeEnum -> response.setScheduleTypeEnumName(typeEnum.getEnumName()));
        }
        return response;
    }
}
