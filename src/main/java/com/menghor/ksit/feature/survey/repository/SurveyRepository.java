package com.menghor.ksit.feature.survey.repository;

import com.menghor.ksit.feature.survey.model.SurveyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface SurveyRepository extends JpaRepository<SurveyEntity, Long>, JpaSpecificationExecutor<SurveyEntity> {
}
