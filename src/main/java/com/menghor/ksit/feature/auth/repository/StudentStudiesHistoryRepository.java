package com.menghor.ksit.feature.auth.repository;

import com.menghor.ksit.feature.auth.models.StudentStudiesHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentStudiesHistoryRepository extends JpaRepository<StudentStudiesHistoryEntity, Long> {
    // Custom query methods can be added here if needed
}