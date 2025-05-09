package com.menghor.ksit.feature.school.repository;

import com.menghor.ksit.feature.school.model.CourseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface CourseRepository extends JpaRepository<CourseEntity, Long>, JpaSpecificationExecutor<CourseEntity> {
    List<CourseEntity> findByDepartmentId(Long departmentId);
    List<CourseEntity> findBySubjectId(Long subjectId);
    List<CourseEntity> findByUserId(Long teacherId);
}
