package com.menghor.ksit.feature.master.service.impl;

import com.menghor.ksit.enumations.RoleEnum;
import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.exceptoins.error.DuplicateNameException;
import com.menghor.ksit.exceptoins.error.NotFoundException;
import com.menghor.ksit.feature.auth.models.UserEntity;
import com.menghor.ksit.feature.master.dto.filter.ClassFilterDto;
import com.menghor.ksit.feature.master.dto.request.ClassRequestDto;
import com.menghor.ksit.feature.master.dto.response.ClassResponseDto;
import com.menghor.ksit.feature.master.dto.update.ClassUpdateDto;
import com.menghor.ksit.feature.master.mapper.ClassMapper;
import com.menghor.ksit.feature.master.model.ClassEntity;
import com.menghor.ksit.feature.master.model.DepartmentEntity;
import com.menghor.ksit.feature.master.model.MajorEntity;
import com.menghor.ksit.feature.master.repository.ClassRepository;
import com.menghor.ksit.feature.master.repository.MajorRepository;
import com.menghor.ksit.feature.master.service.ClassService;
import com.menghor.ksit.feature.master.specification.ClassSpecification;
import com.menghor.ksit.utils.database.CustomPaginationResponseDto;
import com.menghor.ksit.utils.database.SecurityUtils;
import com.menghor.ksit.utils.pagiantion.PaginationUtils;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClassServiceImpl implements ClassService {
    private final ClassRepository classRepository;
    private final MajorRepository majorRepository;
    private final ClassMapper classMapper;
    private final SecurityUtils securityUtils;

    @Override
    @Transactional
    public ClassResponseDto createClass(ClassRequestDto classRequestDto) {
        log.info("Creating new class with code: {}, majorId: {}, academyYear: {}",
                classRequestDto.getCode(), classRequestDto.getMajorId(), classRequestDto.getAcademyYear());

        // Determine the status (default to ACTIVE if not specified)
        Status status = classRequestDto.getStatus() != null ?
                classRequestDto.getStatus() : Status.ACTIVE;

        // Only check for duplicates if this class will be ACTIVE
        if (status == Status.ACTIVE) {
            // Check if an ACTIVE class with the same code already exists
            boolean activeClassExists = classRepository.existsByCodeAndStatus(
                    classRequestDto.getCode(), Status.ACTIVE);

            if (activeClassExists) {
                throw new DuplicateNameException("Class with code '" +
                        classRequestDto.getCode() + "' already exists");
            }
        }

        // Proceed with class creation
        ClassEntity classEntity = classMapper.toEntity(classRequestDto);

        // Ensure status is set if it wasn't specified
        if (classEntity.getStatus() == null) {
            classEntity.setStatus(Status.ACTIVE);
        }

        MajorEntity major = findMajorById(classRequestDto.getMajorId());
        classEntity.setMajor(major);

        ClassEntity savedClass = classRepository.save(classEntity);
        log.info("Class created successfully with ID: {}", savedClass.getId());

        return classMapper.toResponseDto(savedClass);
    }


    @Override
    public ClassResponseDto getClassById(Long id) {
        log.info("Fetching class by ID: {}", id);

        ClassEntity classEntity = findClassById(id);

        log.info("Retrieved class with ID: {}", id);
        return classMapper.toResponseDto(classEntity);
    }

    @Override
    @Transactional
    public ClassResponseDto updateClassById(Long id, ClassUpdateDto classUpdateDto) {
        log.info("Updating class with ID: {}", id);

        // Find the existing entity
        ClassEntity existingClass = findClassById(id);

        // Determine what the status will be after the update
        Status newStatus = classUpdateDto.getStatus() != null ?
                classUpdateDto.getStatus() : existingClass.getStatus();

        // If the new status will be ACTIVE and code is changing, check for duplicates
        if (newStatus == Status.ACTIVE &&
                classUpdateDto.getCode() != null &&
                !classUpdateDto.getCode().equals(existingClass.getCode())) {

            boolean activeClassExists = classRepository.existsByCodeAndStatus(
                    classUpdateDto.getCode(), Status.ACTIVE);

            if (activeClassExists) {
                throw new DuplicateNameException("Another ACTIVE class with code '" +
                        classUpdateDto.getCode() + "' already exists");
            }
        }

        // If the status is changing to ACTIVE (from non-ACTIVE) and the code isn't changing,
        // we still need to check if another ACTIVE class with the same code exists
        if (newStatus == Status.ACTIVE &&
                existingClass.getStatus() != Status.ACTIVE) {

            boolean activeClassWithSameCodeExists = classRepository.existsByCodeAndStatusAndIdNot(
                    existingClass.getCode(), Status.ACTIVE, id);

            if (activeClassWithSameCodeExists) {
                throw new DuplicateNameException("Class with code '" +
                        existingClass.getCode() + "' already exists");
            }
        }

        // Proceed with update
        classMapper.updateEntityFromDto(classUpdateDto, existingClass);

        // Handle major relationship separately if provided
        if (classUpdateDto.getMajorId() != null) {
            MajorEntity major = findMajorById(classUpdateDto.getMajorId());
            existingClass.setMajor(major);
        }

        // Save the updated entity
        ClassEntity updatedClass = classRepository.save(existingClass);
        log.info("Class updated successfully with ID: {}", id);

        return classMapper.toResponseDto(updatedClass);
    }

    @Override
    @Transactional
    public ClassResponseDto deleteClassById(Long id) {
        log.info("Deleting class with ID: {}", id);

        ClassEntity classEntity = findClassById(id);

        // Set status to DELETED (soft delete)
        classEntity.setStatus(Status.DELETED);

        classEntity = classRepository.save(classEntity);
        log.info("Class marked as DELETED successfully with ID: {}", id);

        return classMapper.toResponseDto(classEntity);
    }

    @Override
    public CustomPaginationResponseDto<ClassResponseDto> getAllClasses(ClassFilterDto filterDto) {
        log.info("Fetching all classes with filter: {}", filterDto);

        // Validate and prepare pagination using PaginationUtils
        Pageable pageable = PaginationUtils.createPageable(
                filterDto.getPageNo(),
                filterDto.getPageSize(),
                "createdAt",
                "DESC"
        );

        // Create specification from filter criteria
        Specification<ClassEntity> spec = ClassSpecification.combine(
                filterDto.getSearch(),
                filterDto.getAcademyYear(),
                filterDto.getStatus(),
                filterDto.getMajorId()
        );

        // Execute query with specification and pagination
        Page<ClassEntity> classPage = classRepository.findAll(spec, pageable);

        // Map to response DTO
        CustomPaginationResponseDto<ClassResponseDto> response = classMapper.toClassAllResponseDto(classPage);
        log.info("Retrieved {} classes (page {}/{})",
                response.getContent().size(),
                response.getPageNo(),
                response.getTotalPages());

        return response;
    }

    @Override
    public CustomPaginationResponseDto<ClassResponseDto> getMyClasses(ClassFilterDto filterDto) {
        log.info("Fetching user-specific classes with filter: {}", filterDto);

        UserEntity currentUser = securityUtils.getCurrentUser();
        log.info("Current user: {} with roles: {}", currentUser.getUsername(),
                currentUser.getRoles().stream().map(role -> role.getName().name()).collect(Collectors.toList()));

        // Determine user access level
        if (hasAdminAccess(currentUser)) {
            log.info("User has admin access, returning all classes");
            return getAllClasses(filterDto);
        } else if (isTeacherOrStaff(currentUser)) {
            log.info("User is teacher/staff, filtering by department or classes they teach");
            return getClassesForStaff(currentUser, filterDto);
        } else if (isStudent(currentUser)) {
            log.info("User is student, filtering by their own class");
            return getClassesForStudent(currentUser, filterDto);
        } else {
            log.warn("User {} has unknown or no roles, returning empty classes", currentUser.getUsername());
            return createEmptyClassResponse(filterDto);
        }
    }

    // ===== Private Helper Methods =====

    private CustomPaginationResponseDto<ClassResponseDto> getClassesForStaff(UserEntity staff, ClassFilterDto filterDto) {
        // For teachers/staff, show classes from their department
        if (staff.getDepartment() == null) {
            log.warn("Staff {} has no department assigned", staff.getUsername());
            return createEmptyClassResponse(filterDto);
        }

        // Create specification that filters by department
        Pageable pageable = PaginationUtils.createPageable(
                filterDto.getPageNo(),
                filterDto.getPageSize(),
                "createdAt",
                "DESC"
        );

        // Create specification with department filter
        Specification<ClassEntity> spec = ClassSpecification.combine(
                filterDto.getSearch(),
                filterDto.getAcademyYear(),
                filterDto.getStatus(),
                null // Don't filter by majorId directly, we'll use department
        ).and((root, query, criteriaBuilder) -> {
            // Join with major and then department to filter by staff's department
            Join<ClassEntity, MajorEntity> majorJoin = root.join("major", JoinType.INNER);
            Join<MajorEntity, DepartmentEntity> departmentJoin = majorJoin.join("department", JoinType.INNER);
            return criteriaBuilder.equal(departmentJoin.get("id"), staff.getDepartment().getId());
        });

        Page<ClassEntity> classPage = classRepository.findAll(spec, pageable);

        CustomPaginationResponseDto<ClassResponseDto> response = classMapper.toClassAllResponseDto(classPage);
        log.info("Retrieved {} classes for staff (page {}/{})",
                response.getContent().size(), response.getPageNo(), response.getTotalPages());

        return response;
    }

    private CustomPaginationResponseDto<ClassResponseDto> getClassesForStudent(UserEntity student, ClassFilterDto filterDto) {
        if (student.getClasses() == null) {
            log.warn("Student {} has no class assigned", student.getUsername());
            return createEmptyClassResponse(filterDto);
        }

        Long classId = student.getClasses().getId();

        // Create specification that only returns the student's class
        Pageable pageable = PaginationUtils.createPageable(
                filterDto.getPageNo(),
                filterDto.getPageSize(),
                "createdAt",
                "DESC"
        );

        Specification<ClassEntity> spec = ClassSpecification.combine(
                filterDto.getSearch(),
                filterDto.getAcademyYear(),
                filterDto.getStatus(),
                null // Don't filter by majorId here
        ).and((root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("id"), classId)
        );

        Page<ClassEntity> classPage = classRepository.findAll(spec, pageable);

        CustomPaginationResponseDto<ClassResponseDto> response = classMapper.toClassAllResponseDto(classPage);
        log.info("Retrieved {} classes for student (page {}/{})",
                response.getContent().size(), response.getPageNo(), response.getTotalPages());

        return response;
    }

    private CustomPaginationResponseDto<ClassResponseDto> createEmptyClassResponse(ClassFilterDto filterDto) {
        return CustomPaginationResponseDto.<ClassResponseDto>builder()
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
     * Helper method to find a class by ID or throw NotFoundException
     */
    private ClassEntity findClassById(Long id) {
        return classRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Class not found with ID: {}", id);
                    return new NotFoundException("Class id " + id + " not found. Please try again.");
                });
    }

    /**
     * Helper method to find a major by ID or throw NotFoundException
     */
    private MajorEntity findMajorById(Long id) {
        return majorRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Major not found with ID: {}", id);
                    return new NotFoundException("Major id " + id + " not found");
                });
    }
}

