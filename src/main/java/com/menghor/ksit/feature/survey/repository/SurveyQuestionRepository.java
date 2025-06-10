package com.menghor.ksit.feature.survey.repository;

import com.menghor.ksit.feature.survey.model.SurveyQuestionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SurveyQuestionRepository extends JpaRepository<SurveyQuestionEntity, Long>, JpaSpecificationExecutor<SurveyQuestionEntity> {

    // Find active questions by section
    @Query("SELECT q FROM SurveyQuestionEntity q WHERE q.section.id = :sectionId AND q.status = 'ACTIVE' ORDER BY q.displayOrder ASC, q.id ASC")
    List<SurveyQuestionEntity> findActiveBySectionId(@Param("sectionId") Long sectionId);

    // Find all questions (including deleted) by section - for admin use
    @Query("SELECT q FROM SurveyQuestionEntity q WHERE q.section.id = :sectionId ORDER BY q.displayOrder ASC, q.id ASC")
    List<SurveyQuestionEntity> findAllBySectionId(@Param("sectionId") Long sectionId);

    // Find active question by ID
    @Query("SELECT q FROM SurveyQuestionEntity q WHERE q.id = :id AND q.status = 'ACTIVE'")
    Optional<SurveyQuestionEntity> findActiveById(@Param("id") Long id);

    // Get max display order for active questions in a section
    @Query("SELECT COALESCE(MAX(q.displayOrder), 0) FROM SurveyQuestionEntity q WHERE q.section.id = :sectionId AND q.status = 'ACTIVE'")
    Integer getMaxDisplayOrderBySectionId(@Param("sectionId") Long sectionId);
}

