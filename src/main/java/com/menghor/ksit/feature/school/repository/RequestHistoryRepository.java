package com.menghor.ksit.feature.school.repository;

import com.menghor.ksit.feature.school.model.RequestHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequestHistoryRepository extends JpaRepository<RequestHistoryEntity, Long> {
    
    List<RequestHistoryEntity> findByRequestIdOrderByCreatedAtDesc(Long requestId);
    
    List<RequestHistoryEntity> findByUserIdOrderByCreatedAtDesc(Long userId);
}