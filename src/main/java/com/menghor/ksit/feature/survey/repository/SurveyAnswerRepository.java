package com.menghor.ksit.feature.survey.repository;

import com.menghor.ksit.feature.survey.model.SurveyAnswerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface SurveyAnswerRepository extends JpaRepository<SurveyAnswerEntity, Long>, JpaSpecificationExecutor<SurveyAnswerEntity> {
}