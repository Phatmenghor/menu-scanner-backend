package com.menghor.ksit.feature.master.service.impl;

import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.exceptoins.error.NotFoundException;
import com.menghor.ksit.feature.master.dto.major.request.MajorFilterDto;
import com.menghor.ksit.feature.master.dto.major.request.MajorRequestDto;
import com.menghor.ksit.feature.master.dto.major.response.MajorResponseDto;
import com.menghor.ksit.feature.master.dto.major.response.MajorResponseListDto;
import com.menghor.ksit.feature.master.mapper.MajorMapper;
import com.menghor.ksit.feature.master.model.DepartmentEntity;
import com.menghor.ksit.feature.master.model.MajorEntity;
import com.menghor.ksit.feature.master.repository.DepartmentRepository;
import com.menghor.ksit.feature.master.repository.MajorRepository;
import com.menghor.ksit.feature.master.service.MajorService;
import com.menghor.ksit.feature.master.specification.MajorSpecification;
import com.menghor.ksit.utils.database.CustomPaginationResponseDto;
import com.menghor.ksit.utils.pagiantion.PaginationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MajorServiceImpl implements MajorService {
    private final MajorRepository majorRepository;
    private final DepartmentRepository departmentRepository;
    private final MajorMapper majorMapper;

    @Override
    public MajorResponseDto createMajor(MajorRequestDto majorRequestDto) {
        MajorEntity major = majorMapper.toEntity(majorRequestDto);
        log.info("Creating major: {}", major);

        DepartmentEntity department = departmentRepository.findById(majorRequestDto.getDepartmentId())
                .orElseThrow(() -> new NotFoundException("Department id" + majorRequestDto.getDepartmentId() + " not found"));

        major.setDepartment(department);
        MajorEntity savedMajor = majorRepository.save(major);
        return majorMapper.toResponseDto(savedMajor);
    }

    @Override
    public MajorResponseDto getMajorById(Long id) {
        MajorEntity major = majorRepository.findById(id)
                .orElseThrow(()-> new NotFoundException("Major id " + id + " not found. Please try again."));

        return majorMapper.toResponseDto(major);
    }

    @Transactional
    @Override
    public MajorResponseDto updateMajorById(Long id, MajorRequestDto majorRequestDto) {
        MajorEntity major = majorRepository.findById(id)
                .orElseThrow(()-> new NotFoundException("Major id " + id + " not found. Please try again."));

        DepartmentEntity department = departmentRepository.findById(majorRequestDto.getDepartmentId())
                        .orElseThrow(() -> new NotFoundException("Department " + majorRequestDto.getDepartmentId() + " not found. Please try again."));

        major.setCode(majorRequestDto.getCode());
        major.setName(majorRequestDto.getName());
        major.setDepartment(department);
        major.setStatus(majorRequestDto.getStatus());

        MajorEntity updateMajor = majorRepository.save(major);
        return majorMapper.toResponseDto(updateMajor);
    }

    @Override
    public MajorResponseDto deleteMajorById(Long id) {
        MajorEntity major = majorRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Major id " + id + " not found. Please try again."));

        majorRepository.delete(major);
        return majorMapper.toResponseDto(major);
    }

    @Override
    public CustomPaginationResponseDto<MajorResponseListDto> getAllMajors(MajorFilterDto filterDto) {
        return getMajorWithSpecification(filterDto, MajorSpecification::combine);
    }

    private CustomPaginationResponseDto<MajorResponseListDto> getMajorWithSpecification(
            MajorFilterDto filterDto,
            SpecificationCreator specificationCreator
    ) {
        // Set default pagination
        if (filterDto.getPageNo() == null) filterDto.setPageNo(1);
        if (filterDto.getPageSize() == null) filterDto.setPageSize(10);

        PaginationUtils.validatePagination(filterDto.getPageNo(), filterDto.getPageSize());

        int pageNo = filterDto.getPageNo() - 1;
        int pageSize = filterDto.getPageSize();
        Pageable pageable = PageRequest.of(pageNo, pageSize);

        Specification<MajorEntity> spec = specificationCreator.createSpecification(
                filterDto.getSearch(),
                filterDto.getStatus()
        );

        Page<MajorEntity> majorPage = majorRepository.findAll(spec, pageable);

        // Optional status correction
        majorPage.getContent().forEach(room -> {
            if (room.getStatus() == null) {
                room.setStatus(Status.ACTIVE);
                majorRepository.save(room);
            }
        });

        return majorMapper.toMajorAllResponseDto(majorPage);
    }

    @FunctionalInterface
    private interface SpecificationCreator {
        Specification<MajorEntity> createSpecification(String name, Status status);
    }
}
