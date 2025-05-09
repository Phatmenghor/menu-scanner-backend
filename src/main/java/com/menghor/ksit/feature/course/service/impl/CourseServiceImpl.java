package com.menghor.ksit.feature.course.service.impl;

import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.exceptoins.error.NotFoundException;
import com.menghor.ksit.feature.course.dto.request.CourseFilterDto;
import com.menghor.ksit.feature.course.dto.request.CourseRequestDto;
import com.menghor.ksit.feature.course.dto.response.CourseResponseDto;
import com.menghor.ksit.feature.course.dto.response.CourseResponseListDto;
import com.menghor.ksit.feature.course.mapper.CourseMapper;
import com.menghor.ksit.feature.course.model.CourseEntity;
import com.menghor.ksit.feature.course.repository.CourseRepository;
import com.menghor.ksit.feature.course.service.CourseService;
import com.menghor.ksit.feature.course.specification.CourseSpecification;
import com.menghor.ksit.feature.master.model.DepartmentEntity;
import com.menghor.ksit.feature.master.model.SubjectEntity;
import com.menghor.ksit.feature.master.repository.DepartmentRepository;
import com.menghor.ksit.feature.master.repository.SubjectRepository;
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
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final DepartmentRepository departmentRepository;
    private final SubjectRepository subjectRepository;
    private final CourseMapper courseMapper;

    @Transactional
    @Override
    public CourseResponseDto createCourse(CourseRequestDto courseRequestDto) {
        CourseEntity courseEntity = courseMapper.toEntity(courseRequestDto);

        DepartmentEntity department = departmentRepository.findById(courseRequestDto.getDepartmentId())
                .orElseThrow(() -> new NotFoundException("Department id " + courseRequestDto.getDepartmentId() + " not found. Please try again."));

        SubjectEntity subject = subjectRepository.findById(courseRequestDto.getSubjectId())
                .orElseThrow(() -> new NotFoundException("Subject id " + courseRequestDto.getSubjectId() + " not found. Please try again."));

        courseEntity.setDepartment(department);
        courseEntity.setSubject(subject);
        CourseEntity saveCourse = courseRepository.save(courseEntity);
        return courseMapper.toResponseDto(saveCourse);
    }

    @Override
    public CourseResponseDto getCourseById(Long id) {
        CourseEntity course = courseRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Course id " + id + " not found. Please try again."));

        return courseMapper.toResponseDto(course);
    }

    @Transactional
    @Override
    public CourseResponseDto updateById(Long id, CourseRequestDto courseRequestDto) {
        CourseEntity course = courseRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Course id " + id + " not found. Please try again."));

        DepartmentEntity department = departmentRepository.findById(courseRequestDto.getDepartmentId())
                .orElseThrow(() -> new NotFoundException("Department id " + id + " not found. Please try again."));

        SubjectEntity subject = subjectRepository.findById(courseRequestDto.getSubjectId())
                .orElseThrow(() -> new NotFoundException("Subject id " + id + " not found. Please try again."));

        course.setDepartment(department);
        course.setSubject(subject);
        course.setStatus(courseRequestDto.getStatus());
        course.setCode(courseRequestDto.getCode());
        course.setNameEn(courseRequestDto.getNameEn());
        course.setNameKH(courseRequestDto.getNameKH());
        course.setApply(courseRequestDto.getApply());
        course.setCredit(courseRequestDto.getCredit());
        course.setTheory(courseRequestDto.getTheory());
        course.setExecute(courseRequestDto.getExecute());
        course.setTotalHour(courseRequestDto.getTotalHour());
        course.setDescription(courseRequestDto.getDescription());
        course.setPurpose(courseRequestDto.getPurpose());
        course.setExpectedOutcome(courseRequestDto.getExpectedOutcome());

        CourseEntity saveCourse = courseRepository.save(course);
        return courseMapper.toResponseDto(saveCourse);
    }

    @Override
    public CourseResponseDto deleteById(Long id) {
        CourseEntity course = courseRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Course id " + id + " not found. Please try again."));

        courseRepository.delete(course);
        return courseMapper.toResponseDto(course);
    }

    @Override
    public CustomPaginationResponseDto<CourseResponseListDto> getAllCourses(CourseFilterDto courseFilterDto) {
        return getCourseWithSpecification(courseFilterDto, CourseSpecification::combine);
    }

    private CustomPaginationResponseDto<CourseResponseListDto> getCourseWithSpecification(
            CourseFilterDto filterDto,
            SpecificationCreator specificationCreator
    ) {
        // Set default pagination
        if (filterDto.getPageNo() == null) filterDto.setPageNo(1);
        if (filterDto.getPageSize() == null) filterDto.setPageSize(10);

        PaginationUtils.validatePagination(filterDto.getPageNo(), filterDto.getPageSize());

        int pageNo = filterDto.getPageNo() - 1;
        int pageSize = filterDto.getPageSize();
        Pageable pageable = PageRequest.of(pageNo, pageSize);

        Specification<CourseEntity> spec = specificationCreator.createSpecification(
                filterDto.getSearch(),
                filterDto.getStatus()
        );

        Page<CourseEntity> coursePage = courseRepository.findAll(spec, pageable);

        // Optional status correction
        coursePage.getContent().forEach(room -> {
            if (room.getStatus() == null) {
                room.setStatus(Status.ACTIVE);
                courseRepository.save(room);
            }
        });

        return courseMapper.toCourseAllResponseDto(coursePage);
    }

    private interface SpecificationCreator {
        Specification<CourseEntity> createSpecification(String name, Status status);
    }
}
