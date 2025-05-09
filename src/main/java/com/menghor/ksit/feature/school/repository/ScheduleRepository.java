package com.menghor.ksit.feature.school.repository;

import com.menghor.ksit.feature.school.model.ScheduleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface ScheduleRepository extends JpaRepository<ScheduleEntity, Long>, JpaSpecificationExecutor<ScheduleEntity> {
    
    List<ScheduleEntity> findByClassesId(Long classId);
}