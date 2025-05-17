package com.menghor.ksit.feature.attendance.repository;

import com.menghor.ksit.feature.school.model.ScheduleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<ScheduleEntity, Long>, JpaSpecificationExecutor<ScheduleEntity> {
    List<ScheduleEntity> findByClassesId(Long classId);
    List<ScheduleEntity> findByCourseId(Long courseId);
    List<ScheduleEntity> findBySemesterId(Long semesterId);
    List<ScheduleEntity> findByUserId(Long teacherId);
    List<ScheduleEntity> findByCourseIdAndSemesterId(Long courseId, Long semesterId);
}