package com.menghor.ksit.feature.master.service.impl;

import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.exceptoins.error.NotFoundException;
import com.menghor.ksit.feature.master.dto.subject.request.SubjectFilterDto;
import com.menghor.ksit.feature.master.dto.subject.request.SubjectRequestDto;
import com.menghor.ksit.feature.master.dto.subject.response.SubjectResponseDto;
import com.menghor.ksit.feature.master.mapper.SubjectMapper;
import com.menghor.ksit.feature.master.model.SubjectEntity;
import com.menghor.ksit.feature.master.repository.SubjectRepository;
import com.menghor.ksit.feature.master.service.SubjectService;
import com.menghor.ksit.feature.master.specification.SubjectSpecification;
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
public class SubjectServiceImpl implements SubjectService {

    private final SubjectRepository subjectRepository;
    private final SubjectMapper subjectMapper;

    @Override
    public SubjectResponseDto createSubject(SubjectRequestDto subjectRequestDto) {
        SubjectEntity subject= subjectMapper.toEntity(subjectRequestDto);
        log.info("Create subject:{}",subject);

        SubjectEntity savedSubject= subjectRepository.save(subject);
        return subjectMapper.toResponseDto(savedSubject);
    }

    @Override
    public SubjectResponseDto getSubjectById(Long id) {
        SubjectEntity subject= subjectRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Subject " + id + " not found. Please try again."));

        return subjectMapper.toResponseDto(subject);
    }

    @Override
    public SubjectResponseDto updateSubjectById(SubjectRequestDto subjectRequestDto, Long id) {
        SubjectEntity subject = subjectRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Subject " + id + " not found. Please try again."));

        subject.setName(subjectRequestDto.getName());
        subject.setStatus(subjectRequestDto.getStatus());

        SubjectEntity savedSubject= subjectRepository.save(subject);
        return subjectMapper.toResponseDto(savedSubject);
    }

    @Override
    public SubjectResponseDto deleteSubjectById(Long id) {
        SubjectEntity subject = subjectRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Subject " + id + " not found. Please try again."));

        subjectRepository.delete(subject);
        return subjectMapper.toResponseDto(subject);
    }

    // Get all subjects
    @Override
    public CustomPaginationResponseDto<SubjectResponseDto> getAllSubjects(SubjectFilterDto subjectFilterDto) {
        return getSubjectWithSpecification(subjectFilterDto, SubjectSpecification::combine);

    }

    private CustomPaginationResponseDto<SubjectResponseDto> getSubjectWithSpecification(
            SubjectFilterDto filterDto,
            SpecificationCreator specificationCreator
    ) {
        // Set default pagination
        if (filterDto.getPageNo() == null) filterDto.setPageNo(1);
        if (filterDto.getPageSize() == null) filterDto.setPageSize(10);

        PaginationUtils.validatePagination(filterDto.getPageNo(), filterDto.getPageSize());

        int pageNo = filterDto.getPageNo() - 1;
        int pageSize = filterDto.getPageSize();
        Pageable pageable = PageRequest.of(pageNo, pageSize);

        Specification<SubjectEntity> spec = specificationCreator.createSpecification(
                filterDto.getSearch(),
                filterDto.getStatus()
        );

        Page<SubjectEntity> subjectPage = subjectRepository.findAll(spec, pageable);

        // Optional status correction
        subjectPage.getContent().forEach(room -> {
            if (room.getStatus() == null) {
                room.setStatus(Status.ACTIVE);
                subjectRepository.save(room);
            }
        });

        return subjectMapper.toSubjectAllResponseDto(subjectPage);
    }

    @FunctionalInterface
    private interface SpecificationCreator {
        Specification<SubjectEntity> createSpecification(String name, Status status);
    }
}
