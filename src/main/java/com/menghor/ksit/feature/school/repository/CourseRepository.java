package com.menghor.ksit.feature.school.repository;

import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.feature.school.model.CourseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseRepository extends JpaRepository<CourseEntity, Long>, JpaSpecificationExecutor<CourseEntity> {
    long countByStatus(Status status);
}
