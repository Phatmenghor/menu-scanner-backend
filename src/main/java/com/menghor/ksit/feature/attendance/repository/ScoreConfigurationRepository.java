package com.menghor.ksit.feature.attendance.repository;

import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.feature.attendance.models.ScoreConfigurationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ScoreConfigurationRepository extends JpaRepository<ScoreConfigurationEntity, Long> {
    Optional<ScoreConfigurationEntity> findByStatus(Status status);
    long countByStatus(Status status);
}