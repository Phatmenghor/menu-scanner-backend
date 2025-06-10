package com.menghor.ksit.feature.survey.repository;

import com.menghor.ksit.feature.survey.model.SurveyResponseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SurveyResponseRepository extends JpaRepository<SurveyResponseEntity, Long>, JpaSpecificationExecutor<SurveyResponseEntity> {

    // Get responses by schedule
    @Query("SELECT sr FROM SurveyResponseEntity sr WHERE sr.schedule.id = :scheduleId ORDER BY sr.submittedAt DESC")
    Page<SurveyResponseEntity> findByScheduleId(@Param("scheduleId") Long scheduleId, Pageable pageable);

    // Get responses by user and schedule
    @Query("SELECT sr FROM SurveyResponseEntity sr WHERE sr.user.id = :userId AND sr.schedule.id = :scheduleId")
    Optional<SurveyResponseEntity> findByUserIdAndScheduleId(@Param("userId") Long userId, @Param("scheduleId") Long scheduleId);

    // Check if user has responded to survey for specific schedule
    @Query("SELECT COUNT(sr) > 0 FROM SurveyResponseEntity sr WHERE sr.user.id = :userId AND sr.schedule.id = :scheduleId AND sr.survey.id = :surveyId")
    Boolean existsByUserIdAndScheduleIdAndSurveyId(@Param("userId") Long userId, @Param("scheduleId") Long scheduleId, @Param("surveyId") Long surveyId);
}