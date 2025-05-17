package com.menghor.ksit.feature.auth.repository;

import com.menghor.ksit.feature.auth.models.TeacherPraiseOrCriticismEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeacherPraiseOrCriticismRepository extends JpaRepository<TeacherPraiseOrCriticismEntity, Long> {
    // You can add custom query methods here if needed
}