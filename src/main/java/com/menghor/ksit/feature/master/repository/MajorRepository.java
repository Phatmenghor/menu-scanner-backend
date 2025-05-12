package com.menghor.ksit.feature.master.repository;

import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.feature.master.model.MajorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface MajorRepository extends JpaRepository<MajorEntity, Long>, JpaSpecificationExecutor<MajorEntity> {
    boolean existsByCodeAndStatus(String code, Status status);
    boolean existsByCodeAndStatusAndIdNot(String code, Status status, Long id);
}