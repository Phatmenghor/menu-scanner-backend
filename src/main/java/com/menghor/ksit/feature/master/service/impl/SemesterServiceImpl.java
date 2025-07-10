package com.menghor.ksit.feature.master.service.impl;

import com.menghor.ksit.enumations.SemesterEnum;
import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.exceptoins.error.BadRequestException;
import com.menghor.ksit.exceptoins.error.DuplicateNameException;
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

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SemesterServiceImpl implements SemesterService {
    private final SemesterRepository semesterRepository;
    private final SemesterMapper semesterMapper;

    @Override
    @Transactional
    public SemesterResponseDto createSemester(SemesterRequestDto semesterRequestDto) {
        log.info("Creating new semester: {}, academyYear: {}, startDate: {}, endDate: {}",
                semesterRequestDto.getSemester(), semesterRequestDto.getAcademyYear(),
                semesterRequestDto.getStartDate(), semesterRequestDto.getEndDate());

        // Validate semester doesn't already exist
        validateSemesterCreation(semesterRequestDto);

        SemesterEntity semester = semesterMapper.toEntity(semesterRequestDto);

        // Set default status if not provided
        if (semester.getStatus() == null) {
            semester.setStatus(Status.ACTIVE);
        }

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

        // Validate semester update doesn't create duplicates
        validateSemesterUpdate(id, semesterRequestDto, existingSemester);

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
        semester.setStatus(Status.DELETED);

        semester = semesterRepository.save(semester);
        log.info("SemesterEnum deleted successfully with ID: {}", id);

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

    // Add validation methods to SemesterServiceImpl.java
    private void validateSemesterCreation(SemesterRequestDto requestDto) {
        Integer academyYear = requestDto.getAcademyYear();
        SemesterEnum semester = requestDto.getSemester();
        LocalDate startDate = requestDto.getStartDate();
        LocalDate endDate = requestDto.getEndDate();

        // 1. Check if same semester type already exists in same academy year
        if (semester != null && semesterRepository.existsByAcademyYearAndSemesterAndStatus(academyYear, semester, Status.ACTIVE)) {
            throw new DuplicateNameException(String.format("Semester %s for academy year %d already exists",
                    semester.name(), academyYear));
        }

        // 2. Validate date range
        validateDateRange(startDate, endDate);

        // 3. Check for overlapping date ranges in same academy year
        if (startDate != null && endDate != null) {
            if (semesterRepository.existsOverlappingSemester(academyYear, startDate, endDate, Status.ACTIVE)) {
                throw new DuplicateNameException(String.format("Semester dates (%s to %s) overlap with existing semester in academy year %d",
                        startDate, endDate, academyYear));
            }
        }

        // 4. Business rule: Maximum 2 semesters per academy year
        validateMaxSemestersPerYear(academyYear);
    }

    private void validateSemesterUpdate(Long id, SemesterUpdateDto updateDto, SemesterEntity existingSemester) {
        Integer academyYear = updateDto.getAcademyYear() != null ? updateDto.getAcademyYear() : existingSemester.getAcademyYear();
        SemesterEnum semester = updateDto.getSemester() != null ? updateDto.getSemester() : existingSemester.getSemester();
        LocalDate startDate = updateDto.getStartDate() != null ? updateDto.getStartDate() : existingSemester.getStartDate();
        LocalDate endDate = updateDto.getEndDate() != null ? updateDto.getEndDate() : existingSemester.getEndDate();
        Status status = updateDto.getStatus() != null ? updateDto.getStatus() : existingSemester.getStatus();

        // Only validate if status will be ACTIVE
        if (status != Status.ACTIVE) {
            return;
        }

        // 1. Check if same semester type already exists in same academy year (excluding current)
        if (semester != null && semesterRepository.existsByAcademyYearAndSemesterAndStatusAndIdNot(academyYear, semester, Status.ACTIVE, id)) {
            throw new DuplicateNameException(String.format("Semester %s for academy year %d already exists",
                    semester.name(), academyYear));
        }

        // 2. Validate date range
        validateDateRange(startDate, endDate);

        // 3. Check for overlapping date ranges in same academy year (excluding current)
        if (startDate != null && endDate != null) {
            if (semesterRepository.existsOverlappingSemesterExcludingId(academyYear, startDate, endDate, Status.ACTIVE, id)) {
                throw new DuplicateNameException(String.format("Semester dates (%s to %s) overlap with existing semester in academy year %d",
                        startDate, endDate, academyYear));
            }
        }
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null) {
            if (startDate.isAfter(endDate)) {
                throw new BadRequestException("Start date cannot be after end date");
            }

            if (startDate.isEqual(endDate)) {
                throw new BadRequestException("Start date cannot be the same as end date");
            }
        }
    }

    private void validateMaxSemestersPerYear(Integer academyYear) {
        List<SemesterEntity> existingSemesters = semesterRepository.findByAcademyYearAndStatus(academyYear, Status.ACTIVE);

        if (existingSemesters.size() >= 3) {
            throw new BadRequestException(String.format("Maximum 3 semesters allowed per academy year. Academy year %d already has %d semesters",
                    academyYear, existingSemesters.size()));
        }

        // Additional validation: If one semester exists, ensure they're different types
        if (existingSemesters.size() == 1) {
            SemesterEntity existing = existingSemesters.get(0);
            // This validation will be handled by the duplicate semester type check above
        }
    }


    /**
     * Helper method to find a semester by ID or throw NotFoundException
     */
    private SemesterEntity findSemesterById(Long id) {
        return semesterRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("SemesterEnum not found with ID: {}", id);
                    return new NotFoundException("SemesterEnum id " + id + " not found. Please try again.");
                });
    }
}
