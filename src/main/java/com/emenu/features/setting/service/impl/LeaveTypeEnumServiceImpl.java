package com.emenu.features.setting.service.impl;

import com.emenu.exception.custom.ResourceNotFoundException;
import com.emenu.exception.custom.ValidationException;
import com.emenu.features.setting.dto.filter.ConfigEnumFilterRequest;
import com.emenu.features.setting.dto.request.LeaveTypeEnumCreateRequest;
import com.emenu.features.setting.dto.response.LeaveTypeEnumResponse;
import com.emenu.features.setting.dto.update.LeaveTypeEnumUpdateRequest;
import com.emenu.features.setting.mapper.LeaveTypeEnumMapper;
import com.emenu.features.setting.models.LeaveTypeEnum;
import com.emenu.features.setting.repository.LeaveTypeEnumRepository;
import com.emenu.features.setting.service.LeaveTypeEnumService;
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
public class LeaveTypeEnumServiceImpl implements LeaveTypeEnumService {

    private final LeaveTypeEnumRepository repository;
    private final LeaveTypeEnumMapper mapper;
    private final PaginationMapper paginationMapper;

    @Override
    public LeaveTypeEnumResponse create(LeaveTypeEnumCreateRequest request) {
        log.info("Creating leave type enum: {}", request.getEnumName());

        // Check if enum already exists for this business
        boolean exists = repository.findByBusinessIdAndEnumNameAndIsDeletedFalse(
                request.getBusinessId(), request.getEnumName()).isPresent();

        if (exists) {
            throw new ValidationException(
                    "Enum name already exists for this business: " + request.getEnumName());
        }

        final LeaveTypeEnum enumRecord = mapper.toEntity(request);
        LeaveTypeEnum savedEnum = repository.save(enumRecord);
        log.info("Leave type enum created: {}", savedEnum.getId());

        return mapper.toResponse(savedEnum);
    }

    @Override
    @Transactional(readOnly = true)
    public LeaveTypeEnumResponse getById(UUID id) {
        LeaveTypeEnum enumRecord = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave type enum not found"));
        return mapper.toResponse(enumRecord);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<LeaveTypeEnumResponse> getAll(ConfigEnumFilterRequest filter) {
        Pageable pageable = PaginationUtils.createPageable(
                filter.getPageNo(),
                filter.getPageSize(),
                filter.getSortBy(),
                filter.getSortDirection()
        );

        Page<LeaveTypeEnum> page = repository.findWithFilters(
                filter.getBusinessId(),
                filter.getSearch(),
                pageable
        );

        return paginationMapper.toPaginationResponse(page,
                enums -> mapper.toResponseList(enums.stream().toList()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveTypeEnumResponse> getAllList(ConfigEnumFilterRequest filter) {
        Pageable pageable = PaginationUtils.createPageable(
                filter.getPageNo(),
                filter.getPageSize(),
                filter.getSortBy(),
                filter.getSortDirection()
        );

        Page<LeaveTypeEnum> page = repository.findWithFilters(
                filter.getBusinessId(),
                filter.getSearch(),
                pageable
        );

        return mapper.toResponseList(page.getContent());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveTypeEnumResponse> getByBusinessId(UUID businessId) {
        List<LeaveTypeEnum> enums = repository.findByBusinessIdAndIsDeletedFalse(businessId);
        return mapper.toResponseList(enums);
    }

    @Override
    public LeaveTypeEnumResponse update(UUID id, LeaveTypeEnumUpdateRequest request) {
        log.info("Updating leave type enum: {}", id);

        final LeaveTypeEnum enumRecord = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave type enum not found"));

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
        LeaveTypeEnum updatedEnum = repository.save(enumRecord);
        log.info("Leave type enum updated: {}", id);

        return mapper.toResponse(updatedEnum);
    }

    @Override
    public LeaveTypeEnumResponse delete(UUID id) {
        log.info("Deleting leave type enum: {}", id);

        LeaveTypeEnum enumRecord = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave type enum not found"));

        enumRecord.softDelete();
        enumRecord = repository.save(enumRecord);
        log.info("Leave type enum deleted: {}", id);
        return mapper.toResponse(enumRecord);
    }
}