package com.menghor.ksit.feature.attendance.service.impl;

import com.menghor.ksit.exceptoins.error.NotFoundException;
import com.menghor.ksit.feature.attendance.dto.response.StudentScoreResponseDto;
import com.menghor.ksit.feature.attendance.dto.update.StudentScoreUpdateDto;
import com.menghor.ksit.feature.attendance.mapper.StudentScoreMapper;
import com.menghor.ksit.feature.attendance.models.ScoreConfigurationEntity;
import com.menghor.ksit.feature.attendance.models.StudentScoreEntity;
import com.menghor.ksit.feature.attendance.repository.StudentScoreRepository;
import com.menghor.ksit.feature.attendance.service.StudentScoreService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentScoreServiceImpl implements StudentScoreService {

    private final StudentScoreRepository studentScoreRepository;
    private final StudentScoreMapper studentScoreMapper;

    @Override
    public StudentScoreResponseDto getStudentScoreById(Long id) {
        log.info("Retrieving student score studentScoreId={}", id);

        StudentScoreEntity studentScore = studentScoreRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Student score not found with ID: " + id));

        log.info("Successfully retrieved student score studentScoreId={} for studentId={} in sessionId={}",
                id, studentScore.getStudent().getId(), studentScore.getScoreSession().getId());

        return studentScoreMapper.toDto(studentScore);
    }

    @Override
    @Transactional
    public StudentScoreResponseDto updateStudentScore(StudentScoreUpdateDto updateDto) {
        log.info("Starting update for student score studentScoreId={}", updateDto.getId());

        StudentScoreEntity studentScore = studentScoreRepository.findById(updateDto.getId())
                .orElseThrow(() -> new NotFoundException("Student score not found with ID: " + updateDto.getId()));

        log.info("Found student score for studentId={} in sessionId={}",
                studentScore.getStudent().getId(), studentScore.getScoreSession().getId());

        // Get score configuration for validation
        ScoreConfigurationEntity config = studentScore.getScoreConfiguration();
        if (config == null) {
            throw new IllegalStateException("No score configuration found for this student score");
        }

        // Validate and update scores with percentage limits
        if (updateDto.getAssignmentScore() != null) {
            validateScoreLimit(updateDto.getAssignmentScore(), config.getAssignmentPercentage(), "Assignment");
            BigDecimal assignmentRawScore = BigDecimal.valueOf(updateDto.getAssignmentScore());
            log.info("Assignment raw score updated from {} to {} for studentScoreId={}",
                    studentScore.getAssignmentRawScore(), assignmentRawScore, updateDto.getId());
            studentScore.setAssignmentRawScore(assignmentRawScore);
        }

        if (updateDto.getMidtermScore() != null) {
            validateScoreLimit(updateDto.getMidtermScore(), config.getMidtermPercentage(), "Midterm");
            BigDecimal midtermRawScore = BigDecimal.valueOf(updateDto.getMidtermScore());
            log.info("Midterm raw score updated from {} to {} for studentScoreId={}",
                    studentScore.getMidtermRawScore(), midtermRawScore, updateDto.getId());
            studentScore.setMidtermRawScore(midtermRawScore);
        }

        if (updateDto.getFinalScore() != null) {
            validateScoreLimit(updateDto.getFinalScore(), config.getFinalPercentage(), "Final");
            BigDecimal finalRawScore = BigDecimal.valueOf(updateDto.getFinalScore());
            log.info("Final raw score updated from {} to {} for studentScoreId={}",
                    studentScore.getFinalRawScore(), finalRawScore, updateDto.getId());
            studentScore.setFinalRawScore(finalRawScore);
        }

        if (updateDto.getComments() != null) {
            studentScore.setComments(updateDto.getComments());
            log.info("Comments updated for studentScoreId={}", updateDto.getId());
        }

        // Calculate weighted scores
        calculateWeightedScores(studentScore);

        StudentScoreEntity updatedScore = studentScoreRepository.save(studentScore);

        log.info("Student score update completed for studentScoreId={} - Raw Scores: Assignment={}, Midterm={}, Final={}, Total={}",
                updateDto.getId(),
                updatedScore.getAssignmentRawScore(),
                updatedScore.getMidtermRawScore(),
                updatedScore.getFinalRawScore(),
                updatedScore.getTotalScore());

        return studentScoreMapper.toDto(updatedScore);
    }

    private void validateScoreLimit(Double score, Integer maxPercentage, String scoreType) {
        if (score > maxPercentage) {
            throw new IllegalArgumentException(
                    String.format("%s score cannot exceed %d (current percentage limit). You entered: %.2f",
                            scoreType, maxPercentage, score)
            );
        }
        log.info("Score validation passed: {} score {} is within limit of {}", scoreType, score, maxPercentage);
    }

    private void calculateWeightedScores(StudentScoreEntity studentScore) {
        ScoreConfigurationEntity config = studentScore.getScoreConfiguration();

        if (config == null) {
            log.warn("No score configuration found for studentScoreId={}, skipping weighted calculation", studentScore.getId());
            return;
        }

        log.info("Calculating weighted scores using config: Attendance={}%, Assignment={}%, Midterm={}%, Final={}%",
                config.getAttendancePercentage(), config.getAssignmentPercentage(),
                config.getMidtermPercentage(), config.getFinalPercentage());

        // In this system, raw score IS the weighted score (since max raw = percentage)
        studentScore.setAttendanceScore(studentScore.getAttendanceRawScore());
        studentScore.setAssignmentScore(studentScore.getAssignmentRawScore());
        studentScore.setMidtermScore(studentScore.getMidtermRawScore());
        studentScore.setFinalScore(studentScore.getFinalRawScore());

        // Calculate total score
        BigDecimal totalScore = safeAdd(studentScore.getAttendanceRawScore())
                .add(safeAdd(studentScore.getAssignmentRawScore()))
                .add(safeAdd(studentScore.getMidtermRawScore()))
                .add(safeAdd(studentScore.getFinalRawScore()));

        studentScore.setTotalScore(totalScore);

        log.info("Scores calculated - Assignment: {}, Midterm: {}, Final: {}, Total: {}",
                studentScore.getAssignmentRawScore(),
                studentScore.getMidtermRawScore(),
                studentScore.getFinalRawScore(),
                totalScore);
    }

    private BigDecimal safeAdd(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}