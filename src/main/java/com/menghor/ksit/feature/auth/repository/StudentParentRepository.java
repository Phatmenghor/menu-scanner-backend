package com.menghor.ksit.feature.auth.repository;

import com.menghor.ksit.feature.auth.models.StudentParentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentParentRepository extends JpaRepository<StudentParentEntity, Long> {
    // Custom query methods can be added here if needed
}
