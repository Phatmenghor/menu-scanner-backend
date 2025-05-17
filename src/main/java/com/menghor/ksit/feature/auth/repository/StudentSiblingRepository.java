package com.menghor.ksit.feature.auth.repository;

import com.menghor.ksit.feature.auth.models.StudentSiblingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentSiblingRepository extends JpaRepository<StudentSiblingEntity, Long> {
    // Custom query methods can be added here if needed
}