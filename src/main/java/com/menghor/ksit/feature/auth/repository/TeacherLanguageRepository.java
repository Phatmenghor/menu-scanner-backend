package com.menghor.ksit.feature.auth.repository;

import com.menghor.ksit.feature.auth.models.TeacherLanguageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeacherLanguageRepository extends JpaRepository<TeacherLanguageEntity, Long> {
    // You can add custom query methods here if needed
}