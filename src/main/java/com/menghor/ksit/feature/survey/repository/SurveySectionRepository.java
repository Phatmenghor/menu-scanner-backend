package com.menghor.ksit.feature.survey.repository;

import com.menghor.ksit.feature.survey.model.SurveySectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SurveySectionRepository extends JpaRepository<SurveySectionEntity, Long> {
}