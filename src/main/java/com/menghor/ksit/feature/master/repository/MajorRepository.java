package com.menghor.ksit.feature.master.repository;

import com.menghor.ksit.feature.master.model.MajorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface MajorRepository extends JpaRepository<MajorEntity, Long>, JpaSpecificationExecutor<MajorEntity> {
    // Check if a room with the given name exists (case insensitive)
    boolean existsByCodeIgnoreCase(String name);

    // Check if a room with the given name exists excluding a specific ID (for updates)
    boolean existsByCodeIgnoreCaseAndIdNot(String name, Long id);
}