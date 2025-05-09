package com.menghor.ksit.feature.master.service.impl;

import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.exceptoins.error.NotFoundException;
import com.menghor.ksit.feature.master.dto.filter.SemesterFilterDto;
import com.menghor.ksit.feature.master.dto.request.SemesterRequestDto;
import com.menghor.ksit.feature.master.dto.response.SemesterResponseDto;
import com.menghor.ksit.feature.master.dto.update.SemesterUpdateDto;
import com.menghor.ksit.feature.master.mapper.SemesterMapper;
import com.menghor.ksit.feature.master.model.SemesterEntity;
import com.menghor.ksit.feature.master.repository.SemesterRepository;
import com.menghor.ksit.feature.master.service.SemesterService;
import com.menghor.ksit.feature.master.specification.SemesterSpecification;
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
public class SemesterServiceImpl implements SemesterService {
    private final SemesterRepository semesterRepository;
    private final SemesterMapper semesterMapper;

    @Override
    @Transactional
    public SemesterResponseDto createSemester(SemesterRequestDto semesterRequestDto) {
        log.info("Creating new semester with name: {}, academyYear: {}",
                semesterRequestDto.getName(), semesterRequestDto.getAcademyYear());

        SemesterEntity semester = semesterMapper.toEntity(semesterRequestDto);
        SemesterEntity savedSemester = semesterRepository.save(semester);

        log.info("Semester created successfully with ID: {}", savedSemester.getId());
        return semesterMapper.toResponseDto(savedSemester);
    }

    @Override
    public SemesterResponseDto getSemesterById(Long id) {
        log.info("Fetching semester by ID: {}", id);

        SemesterEntity semester = findSemesterById(id);

        log.info("Retrieved semester with ID: {}", id);
        return semesterMapper.toResponseDto(semester);
    }

    @Override
    @Transactional
    public SemesterResponseDto updateSemesterById(Long id, SemesterUpdateDto semesterRequestDto) {
        log.info("Updating semester with ID: {}", id);

        // Find the existing entity
        SemesterEntity existingSemester = findSemesterById(id);

        // Use MapStruct to update only non-null fields
        semesterMapper.updateEntityFromDto(semesterRequestDto, existingSemester);

        // Save the updated entity
        SemesterEntity updatedSemester = semesterRepository.save(existingSemester);
        log.info("Semester updated successfully with ID: {}", id);

        return semesterMapper.toResponseDto(updatedSemester);
    }

    @Override
    @Transactional
    public SemesterResponseDto deleteSemesterById(Long id) {
        log.info("Deleting semester with ID: {}", id);

        SemesterEntity semester = findSemesterById(id);

        semesterRepository.delete(semester);
        log.info("Semester deleted successfully with ID: {}", id);

        return semesterMapper.toResponseDto(semester);
    }

    @Override
    public CustomPaginationResponseDto<SemesterResponseDto> getAllSemesters(SemesterFilterDto filterDto) {
        log.info("Fetching all semesters with filter: {}", filterDto);

        // Validate and prepare pagination using PaginationUtils
        Pageable pageable = PaginationUtils.createPageable(
                filterDto.getPageNo(),
                filterDto.getPageSize(),
                "createdAt",
                "DESC"
        );

        // Create specification from filter criteria
        Specification<SemesterEntity> spec = SemesterSpecification.combine(
                filterDto.getSearch(),
                filterDto.getAcademyYear(),
                filterDto.getStatus()
        );

        // Execute query with specification and pagination
        Page<SemesterEntity> semesterPage = semesterRepository.findAll(spec, pageable);

        // Apply status correction for any null statuses
        semesterPage.getContent().forEach(semester -> {
            if (semester.getStatus() == null) {
                log.debug("Correcting null status to ACTIVE for semester ID: {}", semester.getId());
                semester.setStatus(Status.ACTIVE);
                semesterRepository.save(semester);
            }
        });

        // Map to response DTO
        CustomPaginationResponseDto<SemesterResponseDto> response = semesterMapper.toSemesterAllResponseDto(semesterPage);
        log.info("Retrieved {} semesters (page {}/{})",
                response.getContent().size(),
                response.getPageNo(),
                response.getTotalPages());

        return response;
    }

    /**
     * Helper method to find a semester by ID or throw NotFoundException
     */
    private SemesterEntity findSemesterById(Long id) {
        return semesterRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Semester not found with ID: {}", id);
                    return new NotFoundException("Semester id " + id + " not found. Please try again.");
                });
    }
}
