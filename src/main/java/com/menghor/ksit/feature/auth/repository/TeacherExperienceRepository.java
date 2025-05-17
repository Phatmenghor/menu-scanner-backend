package com.menghor.ksit.feature.auth.repository;

import com.menghor.ksit.feature.auth.models.TeacherExperienceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeacherExperienceRepository extends JpaRepository<TeacherExperienceEntity, Long> {
    // You can add custom query methods here if needed
}