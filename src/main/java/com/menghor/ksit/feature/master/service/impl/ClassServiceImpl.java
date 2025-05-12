package com.menghor.ksit.feature.master.service.impl;

import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.exceptoins.error.DuplicateNameException;
import com.menghor.ksit.exceptoins.error.NotFoundException;
import com.menghor.ksit.feature.master.dto.filter.ClassFilterDto;
import com.menghor.ksit.feature.master.dto.request.ClassRequestDto;
import com.menghor.ksit.feature.master.dto.response.ClassResponseDto;
import com.menghor.ksit.feature.master.dto.response.ClassResponseListDto;
import com.menghor.ksit.feature.master.dto.update.ClassUpdateDto;
import com.menghor.ksit.feature.master.mapper.ClassMapper;
import com.menghor.ksit.feature.master.model.ClassEntity;
import com.menghor.ksit.feature.master.model.MajorEntity;
import com.menghor.ksit.feature.master.repository.ClassRepository;
import com.menghor.ksit.feature.master.repository.MajorRepository;
import com.menghor.ksit.feature.master.service.ClassService;
import com.menghor.ksit.feature.master.specification.ClassSpecification;
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
public class ClassServiceImpl implements ClassService {
    private final ClassRepository classRepository;
    private final MajorRepository majorRepository;
    private final ClassMapper classMapper;

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

        // Instead of deleting, update status to DELETED
        classEntity.setStatus(Status.DELETED);

        classEntity = classRepository.save(classEntity);
        log.info("Class marked as DELETED successfully with ID: {}", id);


        classRepository.delete(classEntity);
        log.info("Class deleted successfully with ID: {}", id);
        return classMapper.toResponseDto(classEntity);
    }

    @Override
    public CustomPaginationResponseDto<ClassResponseListDto> getAllClasses(ClassFilterDto filterDto) {
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

        // Apply status correction for any null statuses
        classPage.getContent().forEach(cls -> {
            if (cls.getStatus() == null) {
                log.debug("Correcting null status to ACTIVE for class ID: {}", cls.getId());
                cls.setStatus(Status.ACTIVE);
                classRepository.save(cls);
            }
        });

        // Map to response DTO
        CustomPaginationResponseDto<ClassResponseListDto> response = classMapper.toClassAllResponseDto(classPage);
        log.info("Retrieved {} classes (page {}/{})",
                response.getContent().size(),
                response.getPageNo(),
                response.getTotalPages());

        return response;
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

