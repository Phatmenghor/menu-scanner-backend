package com.menghor.ksit.feature.master.service.impl;

import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.exceptoins.error.NotFoundException;
import com.menghor.ksit.feature.master.dto.classes.request.ClassFilterDto;
import com.menghor.ksit.feature.master.dto.classes.request.ClassRequestDto;
import com.menghor.ksit.feature.master.dto.classes.response.ClassResponseDto;
import com.menghor.ksit.feature.master.dto.classes.response.ClassResponseListDto;
import com.menghor.ksit.feature.master.mapper.ClassMapper;
import com.menghor.ksit.feature.master.model.ClassEntity;
import com.menghor.ksit.feature.master.model.MajorEntity;
import com.menghor.ksit.feature.master.repository.ClassRepository;
import com.menghor.ksit.feature.master.repository.MajorRepository;
import com.menghor.ksit.feature.master.service.ClassService;
import com.menghor.ksit.feature.master.specification.ClassSpecification;
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
public class ClassServiceImpl implements ClassService {
    private final ClassRepository classRepository;
    private final MajorRepository majorRepository;
    private final ClassMapper classMapper;

    @Override
    public ClassResponseDto createClass(ClassRequestDto classRequestDto) {
        ClassEntity classEntity = classMapper.toEntity(classRequestDto);

        MajorEntity major = majorRepository.findById(classRequestDto.getMajorId())
                .orElseThrow(()-> new NotFoundException("Major id " + classRequestDto.getMajorId() + " not found"));

        classEntity.setMajor(major);
        ClassEntity classSave =  classRepository.save(classEntity);
        return classMapper.toResponseDto(classSave);
    }

    @Override
    public ClassResponseDto getClassById(Long id) {
        ClassEntity classEntity = classRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Class id " + id + " not found. Please try again."));

        return classMapper.toResponseDto(classEntity);
    }

    @Override
    public ClassResponseDto updateClassById(Long id, ClassRequestDto classRequestDto) {
        ClassEntity classEntity = classRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Class id " + id + " not found. Please try again."));

        MajorEntity major = majorRepository.findById(classRequestDto.getMajorId())
                .orElseThrow(()-> new NotFoundException("Major id " + classRequestDto.getMajorId() + " not found"));

        classEntity.setCode(classRequestDto.getCode());
        classEntity.setDegree(classRequestDto.getDegree());
        classEntity.setYearLevel(classRequestDto.getYearLevel());
        classEntity.setAcademyYear(classRequestDto.getAcademyYear());
        classEntity.setStatus(classRequestDto.getStatus());
        classEntity.setMajor(major);
        ClassEntity classSave = classRepository.save(classEntity);

        return classMapper.toResponseDto(classSave);
    }

    @Override
    public ClassResponseDto deleteClassById(Long id) {
        ClassEntity classEntity = classRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Class id " + id + " not found. Please try again."));

        classRepository.delete(classEntity);
        return classMapper.toResponseDto(classEntity);
    }

    @Override
    public CustomPaginationResponseDto<ClassResponseListDto> getAllClasses(ClassFilterDto filterDto) {
        return getClassWithSpecification(filterDto, ClassSpecification::combine);
    }

    private CustomPaginationResponseDto<ClassResponseListDto> getClassWithSpecification(
            ClassFilterDto filterDto,
            SpecificationCreator specificationCreator
    ) {
        // Set default pagination
        if (filterDto.getPageNo() == null) filterDto.setPageNo(1);
        if (filterDto.getPageSize() == null) filterDto.setPageSize(10);

        PaginationUtils.validatePagination(filterDto.getPageNo(), filterDto.getPageSize());

        int pageNo = filterDto.getPageNo() - 1;
        int pageSize = filterDto.getPageSize();
        Pageable pageable = PageRequest.of(pageNo, pageSize);

        Specification<ClassEntity> spec = specificationCreator.createSpecification(
                filterDto.getSearch(),
                filterDto.getAcademyYear(),
                filterDto.getStatus()
        );

        Page<ClassEntity> classPage = classRepository.findAll(spec, pageable);

        // Optional status correction
        classPage.getContent().forEach(room -> {
            if (room.getStatus() == null) {
                room.setStatus(Status.ACTIVE);
                classRepository.save(room);
            }
        });

        return classMapper.toClassAllResponseDto(classPage);
    }

    private interface SpecificationCreator {
        Specification<ClassEntity> createSpecification(String name, Integer academyYear, Status status);
    }
}
