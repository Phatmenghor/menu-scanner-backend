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
            studentScore.setAssignmentScore(BigDecimal.valueOf(updateDto.getAssignmentScore()));
            log.info("Assignment score updated to {} for studentScoreId={}",
                    updateDto.getAssignmentScore(), updateDto.getId());
        }

        if (updateDto.getMidtermScore() != null) {
            validateScoreLimit(updateDto.getMidtermScore(), config.getMidtermPercentage(), "Midterm");
            studentScore.setMidtermScore(BigDecimal.valueOf(updateDto.getMidtermScore()));
            log.info("Midterm score updated to {} for studentScoreId={}",
                    updateDto.getMidtermScore(), updateDto.getId());
        }

        if (updateDto.getFinalScore() != null) {
            validateScoreLimit(updateDto.getFinalScore(), config.getFinalPercentage(), "Final");
            studentScore.setFinalScore(BigDecimal.valueOf(updateDto.getFinalScore()));
            log.info("Final score updated to {} for studentScoreId={}",
                    updateDto.getFinalScore(), updateDto.getId());
        }

        if (updateDto.getComments() != null) {
            studentScore.setComments(updateDto.getComments());
            log.info("Comments updated for studentScoreId={}", updateDto.getId());
        }

        // Calculate total score
        calculateTotalScore(studentScore);

        StudentScoreEntity updatedScore = studentScoreRepository.save(studentScore);

        log.info("Student score update completed for studentScoreId={} - Scores: Assignment={}, Midterm={}, Final={}, Total={}",
                updateDto.getId(),
                updatedScore.getAssignmentScore(),
                updatedScore.getMidtermScore(),
                updatedScore.getFinalScore(),
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
        if (score < 0) {
            throw new IllegalArgumentException(
                    String.format("%s score cannot be negative. You entered: %.2f", scoreType, score)
            );
        }
        log.info("Score validation passed: {} score {} is within limit of {}", scoreType, score, maxPercentage);
    }

    private void calculateTotalScore(StudentScoreEntity studentScore) {
        // Simple addition since scores are already in their final weighted form
        BigDecimal totalScore = safeAdd(studentScore.getAttendanceScore())
                .add(safeAdd(studentScore.getAssignmentScore()))
                .add(safeAdd(studentScore.getMidtermScore()))
                .add(safeAdd(studentScore.getFinalScore()));

        studentScore.setTotalScore(totalScore);

        log.info("Total score calculated: Assignment={}, Midterm={}, Final={}, Attendance={}, Total={}",
                studentScore.getAssignmentScore(),
                studentScore.getMidtermScore(),
                studentScore.getFinalScore(),
                studentScore.getAttendanceScore(),
                totalScore);
    }

    private BigDecimal safeAdd(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}