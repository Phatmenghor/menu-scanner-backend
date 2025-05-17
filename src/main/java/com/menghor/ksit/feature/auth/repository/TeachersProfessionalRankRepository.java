package com.menghor.ksit.feature.auth.repository;

import com.menghor.ksit.feature.auth.models.TeachersProfessionalRankEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeachersProfessionalRankRepository extends JpaRepository<TeachersProfessionalRankEntity, Long> {
    // You can add custom query methods here if needed
}