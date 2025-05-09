package com.menghor.ksit.feature.master.service.impl;

import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.exceptoins.error.NotFoundException;
import com.menghor.ksit.feature.master.dto.department.request.DepartmentFilter;
import com.menghor.ksit.feature.master.dto.department.response.DepartmentResponseDto;
import com.menghor.ksit.feature.master.dto.semester.request.SemesterFilterDto;
import com.menghor.ksit.feature.master.dto.semester.request.SemesterRequestDto;
import com.menghor.ksit.feature.master.dto.semester.response.SemesterResponseDto;
import com.menghor.ksit.feature.master.mapper.SemesterMapper;
import com.menghor.ksit.feature.master.model.DepartmentEntity;
import com.menghor.ksit.feature.master.model.SemesterEntity;
import com.menghor.ksit.feature.master.repository.SemesterRepository;
import com.menghor.ksit.feature.master.service.SemesterService;
import com.menghor.ksit.feature.master.specification.SemesterSpecification;
import com.menghor.ksit.utils.database.CustomPaginationResponseDto;
import com.menghor.ksit.utils.pagiantion.PaginationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
    public SemesterResponseDto createSemester(SemesterRequestDto semesterRequestDto) {
        SemesterEntity semester = semesterMapper.toEntity(semesterRequestDto);

        SemesterEntity save = semesterRepository.save(semester);
        return semesterMapper.toResponseDto(save);
    }

    @Override
    public SemesterResponseDto getSemesterById(Long id) {
        SemesterEntity semester = semesterRepository.findById(id)
                .orElseThrow(()-> new NotFoundException("Semester id " + id + " not found. Please try again."));

        return semesterMapper.toResponseDto(semester);
    }

    @Override
    public SemesterResponseDto updateSemesterById(Long id, SemesterRequestDto semesterRequestDto) {
        SemesterEntity semester = semesterRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Semester id " + id + " not found. Please try again."));

        semester.setName(semesterRequestDto.getName());
        semester.setStartDate(semesterRequestDto.getStartDate());
        semester.setEndDate(semesterRequestDto.getEndDate());
        semester.setAcademyYear(semesterRequestDto.getAcademyYear());
        semester.setStatus(semesterRequestDto.getStatus());
        semesterRepository.save(semester);

        return semesterMapper.toResponseDto(semester);
    }

    @Override
    public SemesterResponseDto deleteSemesterById(Long id) {
        SemesterEntity semester = semesterRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Semester id " + id + " not found. Please try again."));

        semesterRepository.delete(semester);
        return semesterMapper.toResponseDto(semester);
    }

    @Override
    public CustomPaginationResponseDto<SemesterResponseDto> getAllSemesters(SemesterFilterDto semesterFilterDto) {
        return getSemesterWithSpecification(semesterFilterDto, SemesterSpecification::combine);
    }

    private CustomPaginationResponseDto<SemesterResponseDto> getSemesterWithSpecification(
            SemesterFilterDto filterDto,
            SpecificationCreator specificationCreator
    ) {
        // Set default pagination
        if (filterDto.getPageNo() == null) filterDto.setPageNo(1);
        if (filterDto.getPageSize() == null) filterDto.setPageSize(10);

        PaginationUtils.validatePagination(filterDto.getPageNo(), filterDto.getPageSize());

        int pageNo = filterDto.getPageNo() - 1;
        int pageSize = filterDto.getPageSize();
        Pageable pageable = PageRequest.of(pageNo, pageSize);

        Specification<SemesterEntity> spec = specificationCreator.createSpecification(
                filterDto.getSearch(),
                filterDto.getAcademyYear(),
                filterDto.getStatus()
        );

        Page<SemesterEntity> semesterPage = semesterRepository.findAll(spec, pageable);

        // Optional status correction
        semesterPage.getContent().forEach(room -> {
            if (room.getStatus() == null) {
                room.setStatus(Status.ACTIVE);
                semesterRepository.save(room);
            }
        });

        return semesterMapper.toSemesterAllResponseDto(semesterPage);
    }


    private interface SpecificationCreator {
        Specification<SemesterEntity> createSpecification(String name, Integer academyYear, Status status);
    }
}
