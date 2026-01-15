package com.emenu.features.enums.service.impl;

import com.emenu.exception.custom.ResourceNotFoundException;
import com.emenu.exception.custom.ValidationException;
import com.emenu.features.enums.dto.filter.ConfigEnumFilterRequest;
import com.emenu.features.enums.dto.request.AttendanceStatusEnumCreateRequest;
import com.emenu.features.enums.dto.response.AttendanceStatusEnumResponse;
import com.emenu.features.enums.dto.update.AttendanceStatusEnumUpdateRequest;
import com.emenu.features.enums.mapper.AttendanceStatusEnumMapper;
import com.emenu.features.enums.models.AttendanceStatusEnum;
import com.emenu.features.enums.repository.AttendanceStatusEnumRepository;
import com.emenu.features.enums.service.AttendanceStatusEnumService;
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
public class AttendanceStatusEnumServiceImpl implements AttendanceStatusEnumService {

    private final AttendanceStatusEnumRepository repository;
    private final AttendanceStatusEnumMapper mapper;
    private final PaginationMapper paginationMapper;

    @Override
    public AttendanceStatusEnumResponse create(AttendanceStatusEnumCreateRequest request) {
        log.info("Creating attendance status enum: {}", request.getEnumName());

        // Check if enum already exists for this business
        boolean exists = repository.findByBusinessIdAndEnumNameAndIsDeletedFalse(
                request.getBusinessId(), request.getEnumName()).isPresent();

        if (exists) {
            throw new ValidationException(
                    "Enum name already exists for this business: " + request.getEnumName());
        }

        final AttendanceStatusEnum enumRecord = mapper.toEntity(request);
        AttendanceStatusEnum savedEnum = repository.save(enumRecord);
        log.info("Attendance status enum created: {}", savedEnum.getId());

        return mapper.toResponse(savedEnum);
    }

    @Override
    @Transactional(readOnly = true)
    public AttendanceStatusEnumResponse getById(UUID id) {
        AttendanceStatusEnum enumRecord = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance status enum not found"));
        return mapper.toResponse(enumRecord);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<AttendanceStatusEnumResponse> getAll(ConfigEnumFilterRequest filter) {
        Pageable pageable = PaginationUtils.createPageable(
                filter.getPageNo(),
                filter.getPageSize(),
                filter.getSortBy(),
                filter.getSortDirection()
        );

        Page<AttendanceStatusEnum> page = repository.findWithFilters(
                filter.getBusinessId(),
                filter.getSearch(),
                pageable
        );

        return paginationMapper.toPaginationResponse(page,
                enums -> mapper.toResponseList(enums.stream().toList()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceStatusEnumResponse> getByBusinessId(UUID businessId) {
        List<AttendanceStatusEnum> enums = repository.findByBusinessIdAndIsDeletedFalse(businessId);
        return mapper.toResponseList(enums);
    }

    @Override
    public AttendanceStatusEnumResponse update(UUID id, AttendanceStatusEnumUpdateRequest request) {
        log.info("Updating attendance status enum: {}", id);

        final AttendanceStatusEnum enumRecord = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance status enum not found"));

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
        AttendanceStatusEnum updatedEnum = repository.save(enumRecord);
        log.info("Attendance status enum updated: {}", id);

        return mapper.toResponse(updatedEnum);
    }

    @Override
    public AttendanceStatusEnumResponse delete(UUID id) {
        log.info("Deleting attendance status enum: {}", id);

        AttendanceStatusEnum enumRecord = repository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance status enum not found"));

        enumRecord.softDelete();
        enumRecord = repository.save(enumRecord);
        log.info("Attendance status enum deleted: {}", id);
        return mapper.toResponse(enumRecord);
    }
}