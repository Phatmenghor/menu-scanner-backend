package com.menghor.ksit.feature.master.service.impl;

import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.exceptoins.error.NotFoundException;
import com.menghor.ksit.feature.master.dto.department.request.DepartmentFilter;
import com.menghor.ksit.feature.master.dto.department.request.DepartmentRequestDto;
import com.menghor.ksit.feature.master.dto.department.response.DepartmentResponseDto;
import com.menghor.ksit.feature.master.mapper.DepartmentMapper;
import com.menghor.ksit.feature.master.model.DepartmentEntity;
import com.menghor.ksit.feature.master.repository.DepartmentRepository;
import com.menghor.ksit.feature.master.service.DepartmentService;
import com.menghor.ksit.feature.master.specification.DepartmentSpecification;
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
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final DepartmentMapper departmentMapper;

    @Override
    public DepartmentResponseDto createDepartment(DepartmentRequestDto departmentRequestDto) {
        DepartmentEntity department = departmentMapper.toEntity(departmentRequestDto);

        DepartmentEntity departmentSave = departmentRepository.save(department);
        return departmentMapper.toResponseDto(departmentSave);

    }

    @Override
    public DepartmentResponseDto getDepartmentById(Long id) {
        DepartmentEntity department = departmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Department " + id + " not found. Please try again."));

        return departmentMapper.toResponseDto(department);
    }

    @Override
    public DepartmentResponseDto updateDepartmentById(DepartmentRequestDto departmentRequestDto, Long id) {
        DepartmentEntity department = departmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Department " + id + " not found. Please try again."));

        department.setName(departmentRequestDto.getName());
        department.setCode(departmentRequestDto.getCode());
        department.setUrl_logo(departmentRequestDto.getUrl_logo());
        department.setStatus(departmentRequestDto.getStatus());

        departmentRepository.save(department);
        return departmentMapper.toResponseDto(department);
    }

    @Override
    public DepartmentResponseDto deleteDepartmentById(Long id) {
        DepartmentEntity department = departmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Department " + id + " not found. Please try again."));

        departmentRepository.delete(department);
        return departmentMapper.toResponseDto(department);
    }

    @Override
    public CustomPaginationResponseDto<DepartmentResponseDto> getAllDepartments(DepartmentFilter departmentFilter) {
        return getDepartmentWithSpecification(departmentFilter, DepartmentSpecification::combine);
    }

    private CustomPaginationResponseDto<DepartmentResponseDto> getDepartmentWithSpecification(
            DepartmentFilter filterDto,
            SpecificationCreator specificationCreator
    ) {
        // Set default pagination
        if (filterDto.getPageNo() == null) filterDto.setPageNo(1);
        if (filterDto.getPageSize() == null) filterDto.setPageSize(10);

        PaginationUtils.validatePagination(filterDto.getPageNo(), filterDto.getPageSize());

        int pageNo = filterDto.getPageNo() - 1;
        int pageSize = filterDto.getPageSize();
        Pageable pageable = PageRequest.of(pageNo, pageSize);

        Specification<DepartmentEntity> spec = specificationCreator.createSpecification(
                filterDto.getSearch(),
                filterDto.getStatus()
        );

        Page<DepartmentEntity> departmentPage = departmentRepository.findAll(spec, pageable);

        // Optional status correction
        departmentPage.getContent().forEach(room -> {
            if (room.getStatus() == null) {
                room.setStatus(Status.ACTIVE);
                departmentRepository.save(room);
            }
        });

        return departmentMapper.toDepartmentAllResponseDto(departmentPage);
    }

    private interface SpecificationCreator {
        Specification<DepartmentEntity> createSpecification(String name, Status status);
    }
}
