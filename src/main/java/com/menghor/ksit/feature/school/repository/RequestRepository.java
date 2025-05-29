package com.menghor.ksit.feature.school.repository;

import com.menghor.ksit.enumations.RequestStatus;
import com.menghor.ksit.feature.school.model.RequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequestRepository extends JpaRepository<RequestEntity, Long>, JpaSpecificationExecutor<RequestEntity> {
    
    List<RequestEntity> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    List<RequestEntity> findByStatusOrderByCreatedAtDesc(RequestStatus status);
    
    @Query("SELECT COUNT(r) FROM RequestEntity r WHERE r.status = :status")
    long countByStatus(@Param("status") RequestStatus status);
    
    @Query("SELECT COUNT(r) FROM RequestEntity r WHERE r.user.id = :userId AND r.status = :status")
    long countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") RequestStatus status);
    
    @Query("SELECT r FROM RequestEntity r LEFT JOIN FETCH r.user LEFT JOIN FETCH r.history WHERE r.id = :id")
    RequestEntity findByIdWithDetails(@Param("id") Long id);
}