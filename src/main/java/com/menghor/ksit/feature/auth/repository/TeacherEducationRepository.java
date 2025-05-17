package com.menghor.ksit.feature.auth.repository;

import com.menghor.ksit.feature.auth.models.TeacherEducationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeacherEducationRepository extends JpaRepository<TeacherEducationEntity, Long> {
    // You can add custom query methods here if needed
}