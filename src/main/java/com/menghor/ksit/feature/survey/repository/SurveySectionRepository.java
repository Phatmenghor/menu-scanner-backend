package com.menghor.ksit.feature.survey.repository;

import com.menghor.ksit.feature.survey.model.SurveySectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SurveySectionRepository extends JpaRepository<SurveySectionEntity, Long>, JpaSpecificationExecutor<SurveySectionEntity> {

    // Find all sections (including deleted) by survey - for admin use
    @Query("SELECT s FROM SurveySectionEntity s WHERE s.survey.id = :surveyId ORDER BY s.displayOrder ASC, s.id ASC")
    List<SurveySectionEntity> findAllBySurveyId(@Param("surveyId") Long surveyId);

    // Get max display order for active sections in a survey
    @Query("SELECT COALESCE(MAX(s.displayOrder), 0) FROM SurveySectionEntity s WHERE s.survey.id = :surveyId AND s.status = 'ACTIVE'")
    Integer getMaxDisplayOrderBySurveyId(@Param("surveyId") Long surveyId);
}
