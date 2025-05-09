package com.menghor.ksit.feature.school.service.impl;

import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.exceptoins.error.BadRequestException;
import com.menghor.ksit.exceptoins.error.NotFoundException;
import com.menghor.ksit.feature.auth.models.UserEntity;
import com.menghor.ksit.feature.auth.repository.UserRepository;
import com.menghor.ksit.feature.school.dto.filter.CourseFilterDto;
import com.menghor.ksit.feature.school.dto.request.CourseRequestDto;
import com.menghor.ksit.feature.school.dto.response.CourseResponseDto;
import com.menghor.ksit.feature.school.dto.response.CourseResponseListDto;
import com.menghor.ksit.feature.school.dto.update.CourseUpdateDto;
import com.menghor.ksit.feature.school.mapper.CourseMapper;
import com.menghor.ksit.feature.school.model.CourseEntity;
import com.menghor.ksit.feature.school.repository.CourseRepository;
import com.menghor.ksit.feature.school.service.CourseService;
import com.menghor.ksit.feature.school.specification.CourseSpecification;
import com.menghor.ksit.feature.master.model.DepartmentEntity;
import com.menghor.ksit.feature.master.model.SubjectEntity;
import com.menghor.ksit.feature.master.repository.DepartmentRepository;
import com.menghor.ksit.feature.master.repository.SubjectRepository;
import com.menghor.ksit.utils.database.CustomPaginationResponseDto;
import com.menghor.ksit.utils.pagiantion.PaginationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service
@RequiredArgsConstructor
@Slf4j
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final DepartmentRepository departmentRepository;
    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;
    private final CourseMapper courseMapper;

    @Override
    @Transactional
    public CourseResponseDto createCourse(CourseRequestDto courseRequestDto) {
        log.info("Creating new course with code: {}, name: {}, departmentId: {}, subjectId: {}, teacherId: {}",
                courseRequestDto.getCode(),
                courseRequestDto.getNameEn(),
                courseRequestDto.getDepartmentId(),
                courseRequestDto.getSubjectId(),
                courseRequestDto.getTeacherId());

        // Validate all IDs before proceeding
        validateCourseRequestIds(courseRequestDto);

        CourseEntity course = courseMapper.toEntity(courseRequestDto);

        // Set department
        DepartmentEntity department = findDepartmentById(courseRequestDto.getDepartmentId());
        course.setDepartment(department);

        // Set subject
        SubjectEntity subject = findSubjectById(courseRequestDto.getSubjectId());
        course.setSubject(subject);

        // Set teacher if provided (optional)
        if (courseRequestDto.getTeacherId() != null) {
            UserEntity teacher = findTeacherById(courseRequestDto.getTeacherId());
            course.setUser(teacher);
        }

        CourseEntity savedCourse = courseRepository.save(course);
        log.info("Course created successfully with ID: {}", savedCourse.getId());

        return courseMapper.toResponseDto(savedCourse);
    }

    @Override
    public CourseResponseDto getCourseById(Long id) {
        log.info("Fetching course by ID: {}", id);

        CourseEntity course = findCourseById(id);

        log.info("Retrieved course with ID: {}", id);
        return courseMapper.toResponseDto(course);
    }

    @Override
    @Transactional
    public CourseResponseDto updateById(Long id, CourseUpdateDto courseRequestDto) {
        log.info("Updating course with ID: {}", id);

        // Find the existing course
        CourseEntity existingCourse = findCourseById(id);

        // Use MapStruct to update only non-null fields
        courseMapper.updateEntityFromDto(courseRequestDto, existingCourse);

        // Handle relationships separately if provided
        if (courseRequestDto.getDepartmentId() != null) {
            if (!departmentRepository.existsById(courseRequestDto.getDepartmentId())) {
                throw new NotFoundException("Department not found with ID: " + courseRequestDto.getDepartmentId());
            }
            DepartmentEntity department = findDepartmentById(courseRequestDto.getDepartmentId());
            existingCourse.setDepartment(department);
        }

        if (courseRequestDto.getSubjectId() != null) {
            if (!subjectRepository.existsById(courseRequestDto.getSubjectId())) {
                throw new NotFoundException("Subject not found with ID: " + courseRequestDto.getSubjectId());
            }
            SubjectEntity subject = findSubjectById(courseRequestDto.getSubjectId());
            existingCourse.setSubject(subject);
        }

        if (courseRequestDto.getTeacherId() != null) {
            if (!userRepository.existsById(courseRequestDto.getTeacherId())) {
                throw new NotFoundException("Teacher not found with ID: " + courseRequestDto.getTeacherId());
            }
            UserEntity teacher = findTeacherById(courseRequestDto.getTeacherId());
            existingCourse.setUser(teacher);
        }

        CourseEntity updatedCourse = courseRepository.save(existingCourse);
        log.info("Course updated successfully with ID: {}", id);

        return courseMapper.toResponseDto(updatedCourse);
    }

    @Override
    @Transactional
    public CourseResponseDto deleteById(Long id) {
        log.info("Deleting course with ID: {}", id);

        CourseEntity course = findCourseById(id);

        courseRepository.delete(course);
        log.info("Course deleted successfully with ID: {}", id);

        return courseMapper.toResponseDto(course);
    }

    @Override
    public CustomPaginationResponseDto<CourseResponseListDto> getAllCourses(CourseFilterDto filterDto) {
        log.info("Fetching all courses with filter: {}", filterDto);

        // Validate and prepare pagination using PaginationUtils
        // Always sort by createdAt DESC by default
        Pageable pageable = PaginationUtils.createPageable(
                filterDto.getPageNo(),
                filterDto.getPageSize()
        );

        // Create specification from filter criteria
        Specification<CourseEntity> spec = Specification.where(null);

        // Add search criteria if provided
        if (StringUtils.hasText(filterDto.getSearch())) {
            spec = spec.and(CourseSpecification.search(filterDto.getSearch()));
        }

        // Add department filter if provided
        if (filterDto.getDepartmentId() != null) {
            spec = spec.and(CourseSpecification.hasDepartment(filterDto.getDepartmentId()));
        }

        // Add status filter if provided
        if (filterDto.getStatus() != null) {
            spec = spec.and(CourseSpecification.hasStatus(filterDto.getStatus()));
        }

        // Execute query with specification and pagination
        Page<CourseEntity> coursePage = courseRepository.findAll(spec, pageable);

        // Apply status correction for any null statuses
        coursePage.getContent().forEach(course -> {
            if (course.getStatus() == null) {
                log.debug("Correcting null status to ACTIVE for course ID: {}", course.getId());
                course.setStatus(Status.ACTIVE);
                courseRepository.save(course);
            }
        });

        // Map to response DTO
        CustomPaginationResponseDto<CourseResponseListDto> response = courseMapper.toCourseAllResponseDto(coursePage);
        log.info("Retrieved {} courses (page {}/{})",
                response.getContent().size(),
                response.getPageNo(),
                response.getTotalPages());

        return response;
    }

    /**
     * Helper method to find a course by ID or throw NotFoundException
     */
    private CourseEntity findCourseById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Course not found with ID: {}", id);
                    return new NotFoundException("Course id " + id + " not found. Please try again.");
                });
    }

    /**
     * Helper method to find a department by ID or throw NotFoundException
     */
    private DepartmentEntity findDepartmentById(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Department not found with ID: {}", id);
                    return new NotFoundException("Department id " + id + " not found. Please try again.");
                });
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

    /**
     * Helper method to find a teacher by ID or throw NotFoundException
     */
    private UserEntity findTeacherById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Teacher not found with ID: {}", id);
                    return new NotFoundException("Teacher id " + id + " not found. Please try again.");
                });
    }

    // First, validate all IDs before proceeding with any database operations
    private void validateCourseRequestIds(CourseRequestDto courseRequestDto) {
        // Check department ID
        if (courseRequestDto.getDepartmentId() == null) {
            throw new BadRequestException("Department ID is required");
        }

        if (!departmentRepository.existsById(courseRequestDto.getDepartmentId())) {
            throw new NotFoundException("Department not found with ID: " + courseRequestDto.getDepartmentId());
        }

        // Check subject ID
        if (courseRequestDto.getSubjectId() == null) {
            throw new BadRequestException("Subject ID is required");
        }

        if (!subjectRepository.existsById(courseRequestDto.getSubjectId())) {
            throw new NotFoundException("Subject not found with ID: " + courseRequestDto.getSubjectId());
        }

        // Check department ID
        if (courseRequestDto.getTeacherId() == null) {
            throw new BadRequestException("Teacher ID is required");
        }

        if (!userRepository.existsById(courseRequestDto.getTeacherId())) {
            throw new NotFoundException("Teacher not found with ID: " + courseRequestDto.getTeacherId());
        }
    }
}