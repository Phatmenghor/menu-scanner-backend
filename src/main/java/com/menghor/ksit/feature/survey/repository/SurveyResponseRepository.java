// Updated SurveyResponseRepository.java with all required methods
package com.menghor.ksit.feature.survey.repository;

import com.menghor.ksit.feature.survey.model.SurveyResponseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SurveyResponseRepository extends JpaRepository<SurveyResponseEntity, Long>, JpaSpecificationExecutor<SurveyResponseEntity> {

    @Query("SELECT sr FROM SurveyResponseEntity sr WHERE sr.user.id = :userId AND sr.schedule.id = :scheduleId")
    Optional<SurveyResponseEntity> findByUserIdAndScheduleId(@Param("userId") Long userId, @Param("scheduleId") Long scheduleId);

    @Query("SELECT COUNT(sr) > 0 FROM SurveyResponseEntity sr WHERE sr.user.id = :userId AND sr.schedule.id = :scheduleId AND sr.survey.id = :surveyId")
    Boolean existsByUserIdAndScheduleIdAndSurveyId(@Param("userId") Long userId, @Param("scheduleId") Long scheduleId, @Param("surveyId") Long surveyId);

    // NEW METHODS FOR SURVEY PROGRESS TRACKING

    /**
     * Find all completed survey responses for a specific schedule
     * This helps track which students have completed the survey
     */
    @Query("SELECT sr FROM SurveyResponseEntity sr " +
            "WHERE sr.schedule.id = :scheduleId " +
            "AND sr.isCompleted = true " +
            "AND sr.status = 'ACTIVE'")
    List<SurveyResponseEntity> findCompletedResponsesByScheduleId(@Param("scheduleId") Long scheduleId);

    /**
     * Count completed survey responses for a specific schedule
     */
    @Query("SELECT COUNT(sr) FROM SurveyResponseEntity sr " +
            "WHERE sr.schedule.id = :scheduleId " +
            "AND sr.isCompleted = true " +
            "AND sr.status = 'ACTIVE'")
    Long countCompletedResponsesByScheduleId(@Param("scheduleId") Long scheduleId);

    /**
     * Check if a specific student has completed survey for a schedule
     */
    @Query("SELECT COUNT(sr) > 0 FROM SurveyResponseEntity sr " +
            "WHERE sr.schedule.id = :scheduleId " +
            "AND sr.user.id = :userId " +
            "AND sr.isCompleted = true " +
            "AND sr.status = 'ACTIVE'")
    Boolean hasStudentCompletedSurveyForSchedule(@Param("scheduleId") Long scheduleId, @Param("userId") Long userId);

    /**
     * Get survey responses for multiple schedules
     * Useful for batch progress checking
     */
    @Query("SELECT sr FROM SurveyResponseEntity sr " +
            "WHERE sr.schedule.id IN :scheduleIds " +
            "AND sr.isCompleted = true " +
            "AND sr.status = 'ACTIVE'")
    List<SurveyResponseEntity> findCompletedResponsesByScheduleIds(@Param("scheduleIds") List<Long> scheduleIds);

    /**
     * Alternative simpler method if the above complex queries don't work
     * Find all responses by schedule ID (you can filter completed in service layer)
     */
    @Query("SELECT sr FROM SurveyResponseEntity sr WHERE sr.schedule.id = :scheduleId")
    List<SurveyResponseEntity> findByScheduleId(@Param("scheduleId") Long scheduleId);
}