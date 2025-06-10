package com.menghor.ksit.feature.school.repository;

import com.menghor.ksit.feature.school.model.ScheduleEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<ScheduleEntity, Long>, JpaSpecificationExecutor<ScheduleEntity> {

    /**
     * Find schedules for a specific student (based on class enrollment)
     */
    @Query("SELECT s FROM ScheduleEntity s " +
            "JOIN s.classes c " +
            "JOIN c.students st " +
            "WHERE st.id = :studentId " +
            "ORDER BY s.day ASC, s.startTime ASC")
    Page<ScheduleEntity> findByStudentId(@Param("studentId") Long studentId, Pageable pageable);

    /**
     * Check if schedule exists and student is enrolled
     */
    @Query("SELECT COUNT(s) > 0 FROM ScheduleEntity s " +
            "JOIN s.classes c " +
            "JOIN c.students st " +
            "WHERE s.id = :scheduleId AND st.id = :studentId")
    boolean existsByIdAndClassesStudentsId(@Param("scheduleId") Long scheduleId, @Param("studentId") Long studentId);

    /**
     * Count students in a schedule
     */
    @Query("SELECT COUNT(DISTINCT st.id) FROM ScheduleEntity s " +
            "JOIN s.classes c " +
            "JOIN c.students st " +
            "WHERE s.id = :scheduleId")
    Integer countStudentsByScheduleId(@Param("scheduleId") Long scheduleId);

    /**
     * Count total students across all schedules
     */
    @Query("SELECT COUNT(DISTINCT st.id) FROM ScheduleEntity s " +
            "JOIN s.classes c " +
            "JOIN c.students st")
    Long countTotalStudents();

    /**
     * Find schedules by student ID
     */
    @Query("SELECT s FROM ScheduleEntity s " +
            "JOIN s.classes c " +
            "JOIN c.students st " +
            "WHERE st.id = :studentId " +
            "ORDER BY s.day, s.startTime")
    List<ScheduleEntity> findSchedulesByStudentId(@Param("studentId") Long studentId);
}