package com.menghor.ksit.feature.master.repository;

import com.menghor.ksit.feature.master.model.DepartmentEntity;
import com.menghor.ksit.feature.master.model.RoomEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface DepartmentRepository extends JpaRepository<DepartmentEntity, Long>, JpaSpecificationExecutor<DepartmentEntity> {
        boolean existsByCode(String code);
        boolean existsByName(String name);
        boolean existsByCodeAndIdNot(String code, Long id);
        boolean existsByNameAndIdNot(String name, Long id);
}