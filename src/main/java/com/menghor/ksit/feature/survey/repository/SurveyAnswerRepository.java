package com.menghor.ksit.feature.survey.repository;

import com.menghor.ksit.feature.survey.model.SurveyAnswerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SurveyAnswerRepository extends JpaRepository<SurveyAnswerEntity, Long>, JpaSpecificationExecutor<SurveyAnswerEntity> {

    // Find answers by response ID
    @Query("SELECT sa FROM SurveyAnswerEntity sa WHERE sa.response.id = :responseId ORDER BY sa.question.displayOrder ASC")
    List<SurveyAnswerEntity> findByResponseIdOrderByQuestionOrder(@Param("responseId") Long responseId);

    // Find answers by question ID
    @Query("SELECT sa FROM SurveyAnswerEntity sa WHERE sa.question.id = :questionId")
    List<SurveyAnswerEntity> findByQuestionId(@Param("questionId") Long questionId);

    // Get average rating for a specific question
    @Query("SELECT AVG(sa.ratingAnswer) FROM SurveyAnswerEntity sa WHERE sa.question.id = :questionId AND sa.ratingAnswer IS NOT NULL")
    Double getAverageRatingByQuestionId(@Param("questionId") Long questionId);
}