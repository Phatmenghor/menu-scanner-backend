package com.menghor.ksit.feature.auth.repository;

import com.menghor.ksit.feature.auth.models.TeacherFamilyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeacherFamilyRepository extends JpaRepository<TeacherFamilyEntity, Long> {
    // You can add custom query methods here if needed
}