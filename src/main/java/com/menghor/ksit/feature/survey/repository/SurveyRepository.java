package com.menghor.ksit.feature.survey.repository;

import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.feature.survey.model.SurveyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SurveyRepository extends JpaRepository<SurveyEntity, Long> {
    
    // Find the single active survey
    Optional<SurveyEntity> findByStatus(Status status);
    
    @Query("SELECT COUNT(sr) FROM SurveyResponseEntity sr WHERE sr.survey.id = :surveyId")
    Integer countResponsesBySurveyId(@Param("surveyId") Long surveyId);
    
    @Query("SELECT COUNT(sr) > 0 FROM SurveyResponseEntity sr WHERE sr.survey.id = :surveyId AND sr.user.id = :userId")
    Boolean hasUserResponded(@Param("surveyId") Long surveyId, @Param("userId") Long userId);
}
