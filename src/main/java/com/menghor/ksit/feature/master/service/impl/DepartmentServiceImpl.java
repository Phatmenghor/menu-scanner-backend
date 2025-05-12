package com.menghor.ksit.feature.master.service.impl;

import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.exceptoins.error.DuplicateNameException;
import com.menghor.ksit.exceptoins.error.NotFoundException;
import com.menghor.ksit.feature.master.dto.filter.DepartmentFilter;
import com.menghor.ksit.feature.master.dto.request.DepartmentRequestDto;
import com.menghor.ksit.feature.master.dto.response.DepartmentResponseDto;
import com.menghor.ksit.feature.master.dto.update.DepartmentUpdateDto;
import com.menghor.ksit.feature.master.mapper.DepartmentMapper;
import com.menghor.ksit.feature.master.model.DepartmentEntity;
import com.menghor.ksit.feature.master.repository.DepartmentRepository;
import com.menghor.ksit.feature.master.service.DepartmentService;
import com.menghor.ksit.feature.master.specification.DepartmentSpecification;
import com.menghor.ksit.utils.database.CustomPaginationResponseDto;
import com.menghor.ksit.utils.pagiantion.PaginationUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final DepartmentMapper departmentMapper;

    @Override
    @Transactional
    public DepartmentResponseDto createDepartment(DepartmentRequestDto departmentRequestDto) {
        log.info("Creating new department with code: {}, name: {}",
                departmentRequestDto.getCode(), departmentRequestDto.getName());

        // Check for existing department with same code
        if (departmentRepository.existsByCode(departmentRequestDto.getCode())) {
            throw new DuplicateNameException("Department with code '" +
                    departmentRequestDto.getCode() + "' already exists");
        }

        // Check for existing department with same name
        if (departmentRepository.existsByName(departmentRequestDto.getName())) {
            throw new DuplicateNameException("Department with name '" +
                    departmentRequestDto.getName() + "' already exists");
        }

        // Proceed with department creation
        DepartmentEntity department = departmentMapper.toEntity(departmentRequestDto);

        // Ensure status is set if it wasn't specified
        if (department.getStatus() == null) {
            department.setStatus(Status.ACTIVE);
        }

        DepartmentEntity savedDepartment = departmentRepository.save(department);

        log.info("Department created successfully with ID: {}", savedDepartment.getId());
        return departmentMapper.toResponseDto(savedDepartment);
    }

    @Override
    public DepartmentResponseDto getDepartmentById(Long id) {
        log.info("Fetching department by ID: {}", id);

        DepartmentEntity department = findDepartmentById(id);

        log.info("Retrieved department with ID: {}", id);
        return departmentMapper.toResponseDto(department);
    }

    @Override
    @Transactional
    public DepartmentResponseDto updateDepartmentById(DepartmentUpdateDto departmentRequestDto, Long id) {
        log.info("Updating department with ID: {}", id);

        // Find the existing entity
        DepartmentEntity existingDepartment = findDepartmentById(id);

        // Check for code conflict only if code is being changed
        if (departmentRequestDto.getCode() != null &&
                !departmentRequestDto.getCode().equals(existingDepartment.getCode()) &&
                departmentRepository.existsByCodeAndIdNot(departmentRequestDto.getCode(), id)) {

            throw new DuplicateNameException("Department with code '" +
                    departmentRequestDto.getCode() + "' already exists");
        }

        // Check for name conflict only if name is being changed
        if (departmentRequestDto.getName() != null &&
                !departmentRequestDto.getName().equals(existingDepartment.getName()) &&
                departmentRepository.existsByNameAndIdNot(departmentRequestDto.getName(), id)) {

            throw new DuplicateNameException("Department with name '" +
                    departmentRequestDto.getName() + "' already exists");
        }

        // Use MapStruct to update only non-null fields
        departmentMapper.updateEntityFromDto(departmentRequestDto, existingDepartment);

        // Save the updated entity
        DepartmentEntity updatedDepartment = departmentRepository.save(existingDepartment);
        log.info("Department updated successfully with ID: {}", id);

        return departmentMapper.toResponseDto(updatedDepartment);
    }

    @Override
    @Transactional
    public DepartmentResponseDto deleteDepartmentById(Long id) {
        log.info("Deleting department with ID: {}", id);

        DepartmentEntity department = findDepartmentById(id);

        department.setStatus(Status.DELETED);

        department= departmentRepository.save(department);

        log.info("Department deleted successfully with ID: {}", id);
        return departmentMapper.toResponseDto(department);
    }

    @Override
    public CustomPaginationResponseDto<DepartmentResponseDto> getAllDepartments(DepartmentFilter filterDto) {
        log.info("Fetching all departments with filter: {}", filterDto);

        // Validate and prepare pagination using PaginationUtils
        Pageable pageable = PaginationUtils.createPageable(
                filterDto.getPageNo(),
                filterDto.getPageSize(),
                "createdAt",
                "DESC"
        );

        // Create specification from filter criteria
        Specification<DepartmentEntity> spec = DepartmentSpecification.combine(
                filterDto.getSearch(),
                filterDto.getStatus()
        );

        // Execute query with specification and pagination
        Page<DepartmentEntity> departmentPage = departmentRepository.findAll(spec, pageable);

        // Apply status correction for any null statuses
        departmentPage.getContent().forEach(dept -> {
            if (dept.getStatus() == null) {
                log.debug("Correcting null status to ACTIVE for department ID: {}", dept.getId());
                dept.setStatus(Status.ACTIVE);
                departmentRepository.save(dept);
            }
        });

        // Map to response DTO
        CustomPaginationResponseDto<DepartmentResponseDto> response = departmentMapper.toDepartmentAllResponseDto(departmentPage);
        log.info("Retrieved {} departments (page {}/{})",
                response.getContent().size(),
                response.getPageNo(),
                response.getTotalPages());

        return response;
    }

    /**
     * Helper method to find a department by ID or throw NotFoundException
     */
    private DepartmentEntity findDepartmentById(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Department not found with ID: {}", id);
                    return new NotFoundException("Department id " + id + " not found. Please try again.");
                });
    }
}