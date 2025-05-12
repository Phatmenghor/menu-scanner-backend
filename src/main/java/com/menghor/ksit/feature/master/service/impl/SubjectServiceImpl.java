package com.menghor.ksit.feature.master.service.impl;

import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.exceptoins.error.DuplicateNameException;
import com.menghor.ksit.exceptoins.error.NotFoundException;
import com.menghor.ksit.feature.master.dto.filter.SubjectFilterDto;
import com.menghor.ksit.feature.master.dto.request.SubjectRequestDto;
import com.menghor.ksit.feature.master.dto.response.SubjectResponseDto;
import com.menghor.ksit.feature.master.mapper.SubjectMapper;
import com.menghor.ksit.feature.master.model.SubjectEntity;
import com.menghor.ksit.feature.master.repository.SubjectRepository;
import com.menghor.ksit.feature.master.service.SubjectService;
import com.menghor.ksit.feature.master.specification.SubjectSpecification;
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
public class SubjectServiceImpl implements SubjectService {

    private final SubjectRepository subjectRepository;
    private final SubjectMapper subjectMapper;

    @Override
    @Transactional
    public SubjectResponseDto createSubject(SubjectRequestDto subjectRequest) {
        log.info("Creating new subject with name: {}", subjectRequest.getName());

        // Determine the status (default to ACTIVE if not specified)
        Status status = subjectRequest.getStatus() != null ?
                subjectRequest.getStatus() : Status.ACTIVE;

        // Only check for duplicates if this subject will be ACTIVE
        if (status == Status.ACTIVE) {
            // Check if an ACTIVE subject with the same name already exists
            boolean activeSubjectExists = subjectRepository.existsByNameAndStatus(
                    subjectRequest.getName(), Status.ACTIVE);

            if (activeSubjectExists) {
                throw new DuplicateNameException("Subject with name '" +
                        subjectRequest.getName() + "' already exists");
            }
        }

        // Proceed with subject creation
        SubjectEntity subject = subjectMapper.toEntity(subjectRequest);

        // Ensure status is set if it wasn't specified
        if (subject.getStatus() == null) {
            subject.setStatus(Status.ACTIVE);
        }

        SubjectEntity savedSubject = subjectRepository.save(subject);
        log.info("Subject created successfully with ID: {}", savedSubject.getId());

        return subjectMapper.toResponseDto(savedSubject);
    }

    @Override
    public SubjectResponseDto getSubjectById(Long id) {
        log.info("Fetching subject by ID: {}", id);

        SubjectEntity subject = findSubjectById(id);

        log.info("Retrieved subject with ID: {}", id);
        return subjectMapper.toResponseDto(subject);
    }

    @Override
    @Transactional
    public SubjectResponseDto updateSubjectById(SubjectRequestDto subjectRequestDto, Long id) {
        log.info("Updating subject with ID: {}", id);

        // Find the existing entity
        SubjectEntity existingSubject = findSubjectById(id);

        // Determine what the status will be after the update
        Status newStatus = subjectRequestDto.getStatus() != null ?
                subjectRequestDto.getStatus() : existingSubject.getStatus();

        // If the new status will be ACTIVE and name is changing, check for duplicates
        if (newStatus == Status.ACTIVE &&
                subjectRequestDto.getName() != null &&
                !subjectRequestDto.getName().equals(existingSubject.getName())) {

            boolean activeSubjectExists = subjectRepository.existsByNameAndStatus(
                    subjectRequestDto.getName(), Status.ACTIVE);

            if (activeSubjectExists) {
                throw new DuplicateNameException("Another ACTIVE subject with name '" +
                        subjectRequestDto.getName() + "' already exists");
            }
        }

        // If the status is changing to ACTIVE (from non-ACTIVE) and the name isn't changing,
        // we still need to check if another ACTIVE subject with the same name exists
        if (newStatus == Status.ACTIVE &&
                existingSubject.getStatus() != Status.ACTIVE) {

            boolean activeSubjectWithSameNameExists = subjectRepository.existsByNameAndStatusAndIdNot(
                    existingSubject.getName(), Status.ACTIVE, id);

            if (activeSubjectWithSameNameExists) {
                throw new DuplicateNameException("Subject with name '" +
                        existingSubject.getName() + "' already exists");
            }
        }

        // Proceed with update
        subjectMapper.updateEntityFromDto(subjectRequestDto, existingSubject);

        // Save the updated entity
        SubjectEntity updatedSubject = subjectRepository.save(existingSubject);
        log.info("Subject updated successfully with ID: {}", id);

        return subjectMapper.toResponseDto(updatedSubject);
    }

    @Override
    @Transactional
    public SubjectResponseDto deleteSubjectById(Long id) {
        log.info("Deleting subject with ID: {}", id);

        SubjectEntity subject = findSubjectById(id);
        subject.setStatus(Status.DELETED);

        subject = subjectRepository.save(subject);
        log.info("Subject deleted successfully with ID: {}", id);

        return subjectMapper.toResponseDto(subject);
    }

    @Override
    public CustomPaginationResponseDto<SubjectResponseDto> getAllSubjects(SubjectFilterDto filterDto) {
        log.info("Fetching all subjects with filter: {}", filterDto);

        // Validate and prepare pagination using PaginationUtils
        Pageable pageable = PaginationUtils.createPageable(
                filterDto.getPageNo(),
                filterDto.getPageSize(),
                "createdAt",
                "DESC"
        );

        // Create specification from filter criteria
        Specification<SubjectEntity> spec = SubjectSpecification.combine(
                filterDto.getSearch(),
                filterDto.getStatus()
        );

        // Execute query with specification and pagination
        Page<SubjectEntity> subjectPage = subjectRepository.findAll(spec, pageable);

        // Apply status correction for any null statuses
        subjectPage.getContent().forEach(subject -> {
            if (subject.getStatus() == null) {
                log.debug("Correcting null status to ACTIVE for subject ID: {}", subject.getId());
                subject.setStatus(Status.ACTIVE);
                subjectRepository.save(subject);
            }
        });

        // Map to response DTO
        CustomPaginationResponseDto<SubjectResponseDto> response = subjectMapper.toSubjectAllResponseDto(subjectPage);
        log.info("Retrieved {} subjects (page {}/{})",
                response.getContent().size(),
                response.getPageNo(),
                response.getTotalPages());

        return response;
    }

    /**
     * Helper method to find a subject by ID or throw NotFoundException
     */
    private SubjectEntity findSubjectById(Long id) {
        return subjectRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Subject not found with ID: {}", id);
                    return new NotFoundException("Subject id " + id + " not found. Please try again.");
                });
    }
}