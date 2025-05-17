package com.menghor.ksit.feature.auth.repository;

import com.menghor.ksit.feature.auth.models.TeacherVocationalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeacherVocationalRepository extends JpaRepository<TeacherVocationalEntity, Long> {
    // You can add custom query methods here if needed
}