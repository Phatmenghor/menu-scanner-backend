package com.menghor.ksit.feature.master.service.impl;

import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.exceptoins.error.DuplicateNameException;
import com.menghor.ksit.exceptoins.error.NotFoundException;
import com.menghor.ksit.feature.master.dto.filter.MajorFilterDto;
import com.menghor.ksit.feature.master.dto.request.MajorRequestDto;
import com.menghor.ksit.feature.master.dto.response.MajorResponseDto;
import com.menghor.ksit.feature.master.dto.response.MajorResponseListDto;
import com.menghor.ksit.feature.master.dto.update.MajorUpdateDto;
import com.menghor.ksit.feature.master.mapper.MajorMapper;
import com.menghor.ksit.feature.master.model.DepartmentEntity;
import com.menghor.ksit.feature.master.model.MajorEntity;
import com.menghor.ksit.feature.master.repository.DepartmentRepository;
import com.menghor.ksit.feature.master.repository.MajorRepository;
import com.menghor.ksit.feature.master.service.MajorService;
import com.menghor.ksit.feature.master.specification.MajorSpecification;
import com.menghor.ksit.utils.database.CustomPaginationResponseDto;
import com.menghor.ksit.utils.pagiantion.PaginationUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MajorServiceImpl implements MajorService {
    private final MajorRepository majorRepository;
    private final DepartmentRepository departmentRepository;
    private final MajorMapper majorMapper;

    @Override
    @Transactional
    public MajorResponseDto createMajor(MajorRequestDto majorRequestDto) {
        log.info("Creating new major with code: {}, name: {}, departmentId: {}",
                majorRequestDto.getCode(), majorRequestDto.getName(), majorRequestDto.getDepartmentId());

        // Determine the status (default to ACTIVE if not specified)
        Status status = majorRequestDto.getStatus() != null ?
                majorRequestDto.getStatus() : Status.ACTIVE;

        // Only check for duplicates if this major will be ACTIVE
        if (status == Status.ACTIVE) {
            // Check if an ACTIVE major with the same code already exists
            boolean activeMajorExists = majorRepository.existsByCodeAndStatus(
                    majorRequestDto.getCode(), Status.ACTIVE);

            if (activeMajorExists) {
                throw new DuplicateNameException("Major with code '" +
                        majorRequestDto.getCode() + "' already exists");
            }
        }

        // Proceed with major creation
        MajorEntity majorEntity = majorMapper.toEntity(majorRequestDto);

        // Ensure status is set if it wasn't specified
        if (majorEntity.getStatus() == null) {
            majorEntity.setStatus(Status.ACTIVE);
        }

        // Find and set the department
        if (majorRequestDto.getDepartmentId() != null) {
            DepartmentEntity department = findDepartmentById(majorRequestDto.getDepartmentId());
            majorEntity.setDepartment(department);
        }

        MajorEntity savedMajor = majorRepository.save(majorEntity);
        log.info("Major created successfully with ID: {}", savedMajor.getId());

        return majorMapper.toResponseDto(savedMajor);
    }

    @Override
    public MajorResponseDto getMajorById(Long id) {
        log.info("Fetching major by ID: {}", id);

        MajorEntity majorEntity = findMajorById(id);

        log.info("Retrieved major with ID: {}", id);
        return majorMapper.toResponseDto(majorEntity);
    }

    @Override
    @Transactional
    public MajorResponseDto updateMajorById(Long id, MajorUpdateDto majorUpdateDto) {
        log.info("Updating major with ID: {}", id);

        // Find the existing entity
        MajorEntity existingMajor = findMajorById(id);

        // Determine what the status will be after the update
        Status newStatus = majorUpdateDto.getStatus() != null ?
                majorUpdateDto.getStatus() : existingMajor.getStatus();

        // If the new status will be ACTIVE and code is changing, check for duplicates
        if (newStatus == Status.ACTIVE &&
                majorUpdateDto.getCode() != null &&
                !majorUpdateDto.getCode().equals(existingMajor.getCode())) {

            boolean activeMajorExists = majorRepository.existsByCodeAndStatus(
                    majorUpdateDto.getCode(), Status.ACTIVE);

            if (activeMajorExists) {
                throw new DuplicateNameException("Major with code '" +
                        majorUpdateDto.getCode() + "' already exists");
            }
        }

        // If the status is changing to ACTIVE (from non-ACTIVE) and the code isn't changing,
        // we still need to check if another ACTIVE major with the same code exists
        if (newStatus == Status.ACTIVE &&
                existingMajor.getStatus() != Status.ACTIVE) {

            boolean activeMajorWithSameCodeExists = majorRepository.existsByCodeAndStatusAndIdNot(
                    existingMajor.getCode(), Status.ACTIVE, id);

            if (activeMajorWithSameCodeExists) {
                throw new DuplicateNameException("Major with code '" +
                        existingMajor.getCode() + "' already exists");
            }
        }

        // Use MapStruct to update only non-null fields
        majorMapper.updateEntityFromDto(majorUpdateDto, existingMajor);

        // Handle department relationship separately if provided
        if (majorUpdateDto.getDepartmentId() != null) {
            DepartmentEntity department = findDepartmentById(majorUpdateDto.getDepartmentId());
            existingMajor.setDepartment(department);
        }

        // Save the updated entity
        MajorEntity updatedMajor = majorRepository.save(existingMajor);
        log.info("Major updated successfully with ID: {}", id);

        return majorMapper.toResponseDto(updatedMajor);
    }

    @Override
    @Transactional
    public MajorResponseDto deleteMajorById(Long id) {
        log.info("Deleting major with ID: {}", id);

        MajorEntity majorEntity = findMajorById(id);
        majorEntity.setStatus(Status.DELETED);

        majorEntity = majorRepository.save(majorEntity);
        log.info("Major deleted successfully with ID: {}", id);

        return majorMapper.toResponseDto(majorEntity);
    }

    @Override
    public CustomPaginationResponseDto<MajorResponseListDto> getAllMajors(MajorFilterDto filterDto) {
        log.info("Fetching all majors with filter: {}", filterDto);

        // Validate and prepare pagination using PaginationUtils
        Pageable pageable = PaginationUtils.createPageable(
                filterDto.getPageNo(),
                filterDto.getPageSize(),
                "createdAt",
                "DESC"
        );

        // Create specification from filter criteria
        Specification<MajorEntity> spec = MajorSpecification.combine(
                filterDto.getSearch(),
                filterDto.getStatus(),
                filterDto.getDepartmentId()
        );

        // Execute query with specification and pagination
        Page<MajorEntity> majorPage = majorRepository.findAll(spec, pageable);

        // Apply status correction for any null statuses
        majorPage.getContent().forEach(major -> {
            if (major.getStatus() == null) {
                log.debug("Correcting null status to ACTIVE for major ID: {}", major.getId());
                major.setStatus(Status.ACTIVE);
                majorRepository.save(major);
            }
        });

        // Map to response DTO
        CustomPaginationResponseDto<MajorResponseListDto> response = majorMapper.toMajorAllResponseDto(majorPage);
        log.info("Retrieved {} majors (page {}/{})",
                response.getContent().size(),
                response.getPageNo(),
                response.getTotalPages());

        return response;
    }

    /**
     * Helper method to find a major by ID or throw NotFoundException
     */
    private MajorEntity findMajorById(Long id) {
        return majorRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Major not found with ID: {}", id);
                    return new NotFoundException("Major id " + id + " not found. Please try again.");
                });
    }

    /**
     * Helper method to find a department by ID or throw NotFoundException
     */
    private DepartmentEntity findDepartmentById(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Department not found with ID: {}", id);
                    return new NotFoundException("Department id " + id + " not found");
                });
    }
}