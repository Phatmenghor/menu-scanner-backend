package com.menghor.ksit.feature.attendance.repository;

import com.menghor.ksit.enumations.SubmissionStatus;
import com.menghor.ksit.feature.attendance.models.ScoreSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScoreSessionRepository extends JpaRepository<ScoreSessionEntity, Long>, JpaSpecificationExecutor<ScoreSessionEntity> {
}