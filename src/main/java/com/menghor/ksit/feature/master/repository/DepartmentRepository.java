package com.menghor.ksit.feature.master.repository;

import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.feature.master.model.DepartmentEntity;
import com.menghor.ksit.feature.master.model.RoomEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface DepartmentRepository extends JpaRepository<DepartmentEntity, Long>, JpaSpecificationExecutor<DepartmentEntity> {
        boolean existsByCodeAndStatus(String code, Status status);

        boolean existsByNameAndStatus(String name, Status status);

        boolean existsByCodeAndStatusAndIdNot(String code, Status status, Long id);

        boolean existsByNameAndStatusAndIdNot(String name, Status status, Long id);

        long countByStatus(Status status);
}