package com.menghor.ksit.feature.master.service.impl;

import com.menghor.ksit.enumations.RoleEnum;
import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.exceptoins.error.DuplicateNameException;
import com.menghor.ksit.exceptoins.error.NotFoundException;
import com.menghor.ksit.feature.auth.models.UserEntity;
import com.menghor.ksit.feature.master.dto.filter.MajorFilterDto;
import com.menghor.ksit.feature.master.dto.request.MajorRequestDto;
import com.menghor.ksit.feature.master.dto.response.MajorResponseDto;
import com.menghor.ksit.feature.master.dto.update.MajorUpdateDto;
import com.menghor.ksit.feature.master.mapper.MajorMapper;
import com.menghor.ksit.feature.master.model.DepartmentEntity;
import com.menghor.ksit.feature.master.model.MajorEntity;
import com.menghor.ksit.feature.master.repository.DepartmentRepository;
import com.menghor.ksit.feature.master.repository.MajorRepository;
import com.menghor.ksit.feature.master.service.MajorService;
import com.menghor.ksit.feature.master.specification.MajorSpecification;
import com.menghor.ksit.utils.database.CustomPaginationResponseDto;
import com.menghor.ksit.utils.database.SecurityUtils;
import com.menghor.ksit.utils.pagiantion.PaginationUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.stream.Collectors;

import static com.menghor.ksit.feature.auth.specification.UserSpecification.isStudent;

@Service
@RequiredArgsConstructor
@Slf4j
public class MajorServiceImpl implements MajorService {
    private final MajorRepository majorRepository;
    private final DepartmentRepository departmentRepository;
    private final MajorMapper majorMapper;
    private final SecurityUtils securityUtils;

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
    public CustomPaginationResponseDto<MajorResponseDto> getAllMajors(MajorFilterDto filterDto) {
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
        CustomPaginationResponseDto<MajorResponseDto> response = majorMapper.toMajorAllResponseDto(majorPage);
        log.info("Retrieved {} majors (page {}/{})",
                response.getContent().size(),
                response.getPageNo(),
                response.getTotalPages());

        return response;
    }

    @Override
    public CustomPaginationResponseDto<MajorResponseDto> getMyMajors(MajorFilterDto filterDto) {
        log.info("Fetching user-specific majors with filter: {}", filterDto);

        UserEntity currentUser = securityUtils.getCurrentUser();
        log.info("Current user: {} with roles: {}", currentUser.getUsername(),
                currentUser.getRoles().stream().map(role -> role.getName().name()).collect(Collectors.toList()));

        // Determine user access level
        if (hasAdminAccess(currentUser)) {
            log.info("User has admin access, returning all majors");
            return getAllMajors(filterDto);
        } else if (isTeacherOrStaff(currentUser)) {
            log.info("User is teacher/staff, filtering by department ID: {}",
                    currentUser.getDepartment() != null ? currentUser.getDepartment().getId() : "none");
            return getMajorsForStaff(currentUser, filterDto);
        } else if (isStudent(currentUser)) {
            log.info("User is student, filtering by class's major");
            return getMajorsForStudent(currentUser, filterDto);
        } else {
            log.warn("User {} has unknown or no roles, returning empty majors", currentUser.getUsername());
            return createEmptyMajorResponse(filterDto);
        }
    }

    private CustomPaginationResponseDto<MajorResponseDto> getMajorsForStaff(UserEntity staff, MajorFilterDto filterDto) {
        if (staff.getDepartment() == null) {
            log.warn("Staff {} has no department assigned", staff.getUsername());
            return createEmptyMajorResponse(filterDto);
        }

        // Create a copy of the filter with department ID constraint
        MajorFilterDto staffFilter = new MajorFilterDto();
        staffFilter.setSearch(filterDto.getSearch());
        staffFilter.setStatus(filterDto.getStatus());
        staffFilter.setDepartmentId(staff.getDepartment().getId()); // Override with staff's department
        staffFilter.setPageNo(filterDto.getPageNo());
        staffFilter.setPageSize(filterDto.getPageSize());

        // Use the existing getAllMajors method with the modified filter
        CustomPaginationResponseDto<MajorResponseDto> response = getAllMajors(staffFilter);
        log.info("Retrieved {} majors for staff (page {}/{})",
                response.getContent().size(), response.getPageNo(), response.getTotalPages());

        return response;
    }

    private CustomPaginationResponseDto<MajorResponseDto> getMajorsForStudent(UserEntity student, MajorFilterDto filterDto) {
        if (student.getClasses() == null || student.getClasses().getMajor() == null) {
            log.warn("Student {} has no class/major assigned", student.getUsername());
            return createEmptyMajorResponse(filterDto);
        }

        Long majorId = student.getClasses().getMajor().getId();

        // Create specification that only returns the student's major
        Pageable pageable = PaginationUtils.createPageable(
                filterDto.getPageNo(),
                filterDto.getPageSize(),
                "createdAt",
                "DESC"
        );

        Specification<MajorEntity> spec = MajorSpecification.combine(
                filterDto.getSearch(),
                filterDto.getStatus(),
                null // Don't filter by department here since we're filtering by specific major
        ).and((root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("id"), majorId)
        );

        Page<MajorEntity> majorPage = majorRepository.findAll(spec, pageable);

        CustomPaginationResponseDto<MajorResponseDto> response = majorMapper.toMajorAllResponseDto(majorPage);
        log.info("Retrieved {} majors for student (page {}/{})",
                response.getContent().size(), response.getPageNo(), response.getTotalPages());

        return response;
    }

    private CustomPaginationResponseDto<MajorResponseDto> createEmptyMajorResponse(MajorFilterDto filterDto) {
        return CustomPaginationResponseDto.<MajorResponseDto>builder()
                .content(Collections.emptyList())
                .pageNo(filterDto.getPageNo() != null ? filterDto.getPageNo() : 1)
                .pageSize(filterDto.getPageSize() != null ? filterDto.getPageSize() : 10)
                .totalElements(0L)
                .totalPages(0)
                .last(true)
                .build();
    }

    // Role checking methods
    private boolean hasAdminAccess(UserEntity user) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getName() == RoleEnum.ADMIN || role.getName() == RoleEnum.DEVELOPER);
    }

    private boolean isTeacherOrStaff(UserEntity user) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getName() == RoleEnum.TEACHER || role.getName() == RoleEnum.STAFF);
    }

    private boolean isStudent(UserEntity user) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getName() == RoleEnum.STUDENT);
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