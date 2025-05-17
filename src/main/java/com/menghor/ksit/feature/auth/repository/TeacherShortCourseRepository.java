package com.menghor.ksit.feature.auth.repository;

import com.menghor.ksit.feature.auth.models.TeacherShortCourseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeacherShortCourseRepository extends JpaRepository<TeacherShortCourseEntity, Long> {
    // You can add custom query methods here if needed
}