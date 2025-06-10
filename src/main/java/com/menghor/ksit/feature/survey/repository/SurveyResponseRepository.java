package com.menghor.ksit.feature.survey.repository;

import com.menghor.ksit.feature.survey.model.SurveyResponseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SurveyResponseRepository extends JpaRepository<SurveyResponseEntity, Long> {

    // Get all responses for the survey
    @Query("SELECT sr FROM SurveyResponseEntity sr WHERE sr.survey.id = :surveyId ORDER BY sr.submittedAt DESC")
    Page<SurveyResponseEntity> findBySurveyId(@Param("surveyId") Long surveyId, Pageable pageable);

    // Get responses by schedule
    @Query("SELECT sr FROM SurveyResponseEntity sr WHERE sr.schedule.id = :scheduleId ORDER BY sr.submittedAt DESC")
    Page<SurveyResponseEntity> findByScheduleId(@Param("scheduleId") Long scheduleId, Pageable pageable);

    // Get responses by user and schedule
    @Query("SELECT sr FROM SurveyResponseEntity sr WHERE sr.user.id = :userId AND sr.schedule.id = :scheduleId")
    Optional<SurveyResponseEntity> findByUserIdAndScheduleId(@Param("userId") Long userId, @Param("scheduleId") Long scheduleId);

    // Get responses by user
    Page<SurveyResponseEntity> findByUserId(Long userId, Pageable pageable);

    // Count responses by schedule
    Long countByScheduleId(Long scheduleId);

    // Count completed responses by schedule
    @Query("SELECT COUNT(sr) FROM SurveyResponseEntity sr WHERE sr.schedule.id = :scheduleId AND sr.isCompleted = :completed")
    Long countByScheduleIdAndCompletionStatus(@Param("scheduleId") Long scheduleId, @Param("completed") Boolean completed);

    // Get average overall rating by schedule
    @Query("SELECT AVG(sr.overallRating) FROM SurveyResponseEntity sr WHERE sr.schedule.id = :scheduleId AND sr.overallRating IS NOT NULL")
    Optional<Double> getAverageOverallRatingByScheduleId(@Param("scheduleId") Long scheduleId);

    // Get all responses for a survey with user and schedule info
    @Query("SELECT sr FROM SurveyResponseEntity sr " +
            "JOIN FETCH sr.user u " +
            "JOIN FETCH sr.schedule s " +
            "JOIN FETCH s.course c " +
            "JOIN FETCH s.classes cl " +
            "WHERE sr.survey.id = :surveyId")
    List<SurveyResponseEntity> findAllBySurveyIdWithDetails(@Param("surveyId") Long surveyId);
}