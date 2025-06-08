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

    // Get responses with filters
    @Query("SELECT sr FROM SurveyResponseEntity sr " +
            "JOIN sr.user u " +
            "JOIN sr.schedule s " +
            "WHERE (:scheduleId IS NULL OR sr.schedule.id = :scheduleId) " +
            "AND (:studentId IS NULL OR sr.user.id = :studentId) " +
            "AND (:completedOnly IS NULL OR :completedOnly = false OR sr.isCompleted = true) " +
            "AND (:searchTerm IS NULL OR :searchTerm = '' OR " +
            "LOWER(u.khmerFirstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(u.englishFirstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(s.course.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
            "ORDER BY sr.submittedAt DESC")
    Page<SurveyResponseEntity> findWithFilters(
            @Param("scheduleId") Long scheduleId,
            @Param("studentId") Long studentId,
            @Param("completedOnly") Boolean completedOnly,
            @Param("searchTerm") String searchTerm,
            Pageable pageable
    );
}