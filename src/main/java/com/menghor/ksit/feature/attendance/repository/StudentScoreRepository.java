package com.menghor.ksit.feature.attendance.repository;

import com.menghor.ksit.feature.attendance.models.StudentScoreEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentScoreRepository extends JpaRepository<StudentScoreEntity, Long>,
        JpaSpecificationExecutor<StudentScoreEntity> {
    
    List<StudentScoreEntity> findByScoreSessionId(Long scoreSessionId);
    
    Optional<StudentScoreEntity> findByScoreSessionIdAndStudentId(Long scoreSessionId, Long studentId);
    
    List<StudentScoreEntity> findByStudentId(Long studentId);
    
    boolean existsByScoreSessionIdAndStudentId(Long scoreSessionId, Long studentId);
}