package com.menghor.ksit.feature.survey.repository;

import com.menghor.ksit.feature.survey.model.SurveyResponseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
}