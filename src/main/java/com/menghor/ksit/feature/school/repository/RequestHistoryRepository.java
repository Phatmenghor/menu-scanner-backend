package com.menghor.ksit.feature.school.repository;

import com.menghor.ksit.feature.school.model.RequestHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface RequestHistoryRepository extends JpaRepository<RequestHistoryEntity, Long>, JpaSpecificationExecutor<RequestHistoryEntity> {
}