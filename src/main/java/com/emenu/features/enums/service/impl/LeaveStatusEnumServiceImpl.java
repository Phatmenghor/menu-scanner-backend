package com.emenu.features.enums.service.impl;

import com.emenu.exception.custom.ResourceNotFoundException;
import com.emenu.exception.custom.ValidationException;
import com.emenu.features.enums.dto.filter.ConfigEnumFilterRequest;
import com.emenu.features.enums.dto.request.LeaveStatusEnumCreateRequest;
import com.emenu.features.enums.dto.response.LeaveStatusEnumResponse;
import com.emenu.features.enums.dto.update.LeaveStatusEnumUpdateRequest;
import com.emenu.features.enums.mapper.LeaveStatusEnumMapper;
import com.emenu.features.enums.models.LeaveStatusEnum;
import com.emenu.features.enums.repository.LeaveStatusEnumRepository;
import com.emenu.features.enums.service.LeaveStatusEnumService;
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
public class LeaveStatusEnumServiceImpl implements LeaveStatusEnumService {

    private final LeaveStatusEnumRepository repository;
    private final LeaveStatusEnumMapper mapper;
    private final PaginationMapper paginationMapper;

    @Override
    public LeaveStatusEnumResponse create(LeaveStatusEnumCreateRequest request) {
        log.info("Creating leave status enum: {}", request.getEnumName());

        // Check if enum already exists for this business
        boolean exists = repository.findByBusinessIdAndEnumNameAndIsDeletedFalse(
                request.getBusinessId(), request.getEnumName()).isPresent();

        if (exists) {
            throw new ValidationException(
                    "Enum name already exists for this business: " + request.getEnumName());
        }

        final LeaveStatusEnum enumRecord = mapper.toEntity(request);
        LeaveStatusEnum savedEnum = repository.save(enumRecord);
        log.info("Leave status enum created: {}", savedEnum.getId());

        return mapper.toResponse(savedEnum);
    }

    @Override
    @Transactional(readOnly = true)
    public LeaveStatusEnumResponse getById(UUID id) {
        LeaveStatusEnum enumRecord = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave status enum not found"));
        return mapper.toResponse(enumRecord);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<LeaveStatusEnumResponse> getAll(ConfigEnumFilterRequest filter) {
        Pageable pageable = PaginationUtils.createPageable(
                filter.getPageNo(),
                filter.getPageSize(),
                filter.getSortBy(),
                filter.getSortDirection()
        );

        Page<LeaveStatusEnum> page = repository.findWithFilters(
                filter.getBusinessId(),
                filter.getSearch(),
                pageable
        );

        return paginationMapper.toPaginationResponse(page,
                enums -> mapper.toResponseList(enums.stream().toList()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveStatusEnumResponse> getAllList(ConfigEnumFilterRequest filter) {
        Pageable pageable = PaginationUtils.createPageable(
                filter.getPageNo(),
                filter.getPageSize(),
                filter.getSortBy(),
                filter.getSortDirection()
        );

        Page<LeaveStatusEnum> page = repository.findWithFilters(
                filter.getBusinessId(),
                filter.getSearch(),
                pageable
        );

        return mapper.toResponseList(page.getContent());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveStatusEnumResponse> getByBusinessId(UUID businessId) {
        List<LeaveStatusEnum> enums = repository.findByBusinessIdAndIsDeletedFalse(businessId);
        return mapper.toResponseList(enums);
    }

    @Override
    public LeaveStatusEnumResponse update(UUID id, LeaveStatusEnumUpdateRequest request) {
        log.info("Updating leave status enum: {}", id);

        final LeaveStatusEnum enumRecord = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave status enum not found"));

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
        LeaveStatusEnum updatedEnum = repository.save(enumRecord);
        log.info("Leave status enum updated: {}", id);

        return mapper.toResponse(updatedEnum);
    }

    @Override
    public LeaveStatusEnumResponse delete(UUID id) {
        log.info("Deleting leave status enum: {}", id);

        LeaveStatusEnum enumRecord = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Leave status enum not found"));

        enumRecord.softDelete();
        enumRecord = repository.save(enumRecord);
        log.info("Leave status enum deleted: {}", id);
        return mapper.toResponse(enumRecord);

    }
}