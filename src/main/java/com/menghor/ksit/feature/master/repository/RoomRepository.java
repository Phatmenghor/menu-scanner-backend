package com.menghor.ksit.feature.master.repository;

import com.menghor.ksit.feature.master.model.RoomEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<RoomEntity, Long>, JpaSpecificationExecutor<RoomEntity> {
    // Check if a room with the given name exists (case insensitive)
    boolean existsByNameIgnoreCase(String name);

    // Check if a room with the given name exists excluding a specific ID (for updates)
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}