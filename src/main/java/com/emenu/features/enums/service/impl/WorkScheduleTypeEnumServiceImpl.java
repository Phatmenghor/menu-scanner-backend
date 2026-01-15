package com.emenu.features.enums.service.impl;

import com.emenu.exception.custom.ResourceNotFoundException;
import com.emenu.exception.custom.ValidationException;
import com.emenu.features.enums.dto.filter.ConfigEnumFilterRequest;
import com.emenu.features.enums.dto.request.WorkScheduleTypeEnumCreateRequest;
import com.emenu.features.enums.dto.response.WorkScheduleTypeEnumResponse;
import com.emenu.features.enums.dto.update.WorkScheduleTypeEnumUpdateRequest;
import com.emenu.features.enums.mapper.WorkScheduleTypeEnumMapper;
import com.emenu.features.enums.models.WorkScheduleTypeEnum;
import com.emenu.features.enums.repository.WorkScheduleTypeEnumRepository;
import com.emenu.features.enums.service.WorkScheduleTypeEnumService;
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
public class WorkScheduleTypeEnumServiceImpl implements WorkScheduleTypeEnumService {

    private final WorkScheduleTypeEnumRepository repository;
    private final WorkScheduleTypeEnumMapper mapper;
    private final PaginationMapper paginationMapper;

    @Override
    public WorkScheduleTypeEnumResponse create(WorkScheduleTypeEnumCreateRequest request) {
        log.info("Creating work schedule type enum: {}", request.getEnumName());

        // Check if enum already exists for this business
        boolean exists = repository.findByBusinessIdAndEnumNameAndIsDeletedFalse(
                request.getBusinessId(), request.getEnumName()).isPresent();

        if (exists) {
            throw new ValidationException(
                    "Enum name already exists for this business: " + request.getEnumName());
        }

        final WorkScheduleTypeEnum enumRecord = mapper.toEntity(request);
        WorkScheduleTypeEnum savedEnum = repository.save(enumRecord);
        log.info("Work schedule type enum created: {}", savedEnum.getId());

        return mapper.toResponse(savedEnum);
    }

    @Override
    @Transactional(readOnly = true)
    public WorkScheduleTypeEnumResponse getById(UUID id) {
        WorkScheduleTypeEnum enumRecord = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Work schedule type enum not found"));
        return mapper.toResponse(enumRecord);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<WorkScheduleTypeEnumResponse> getAll(ConfigEnumFilterRequest filter) {
        Pageable pageable = PaginationUtils.createPageable(
                filter.getPageNo(),
                filter.getPageSize(),
                filter.getSortBy(),
                filter.getSortDirection()
        );

        Page<WorkScheduleTypeEnum> page = repository.findWithFilters(
                filter.getBusinessId(),
                filter.getSearch(),
                pageable
        );

        return paginationMapper.toPaginationResponse(page,
                enums -> mapper.toResponseList(enums.stream().toList()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkScheduleTypeEnumResponse> getByBusinessId(UUID businessId) {
        List<WorkScheduleTypeEnum> enums = repository.findByBusinessIdAndIsDeletedFalse(businessId);
        return mapper.toResponseList(enums);
    }

    @Override
    public WorkScheduleTypeEnumResponse update(UUID id, WorkScheduleTypeEnumUpdateRequest request) {
        log.info("Updating work schedule type enum: {}", id);

        final WorkScheduleTypeEnum enumRecord = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Work schedule type enum not found"));

        if (request.getEnumName() != null) {
            // Check if new name already exists for this business
            final UUID businessId = enumRecord.getBusinessId();
            final String enumName = request.getEnumName();

            boolean exists = repository.findByBusinessIdAndEnumNameAndIsDeletedFalse(
                            businessId, enumName)
                    .filter(e -> !e.getId().equals(id))
                    .isPresent();

            if (exists) {
                throw new ValidationException(
                        "Enum name already exists for this business: " + enumName);
            }
        }

        mapper.updateEntity(request, enumRecord);
        WorkScheduleTypeEnum updatedEnum = repository.save(enumRecord);
        log.info("Work schedule type enum updated: {}", id);

        return mapper.toResponse(updatedEnum);
    }

    @Override
    public WorkScheduleTypeEnumResponse delete(UUID id) {
        log.info("Deleting work schedule type enum: {}", id);

        WorkScheduleTypeEnum enumRecord = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Work schedule type enum not found"));

        enumRecord.softDelete();
        enumRecord = repository.save(enumRecord);
        log.info("Work schedule type enum deleted: {}", id);
        return mapper.toResponse(enumRecord);
    }
}