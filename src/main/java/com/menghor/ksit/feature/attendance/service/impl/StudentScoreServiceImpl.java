package com.menghor.ksit.feature.attendance.service.impl;

import com.menghor.ksit.enumations.AttendanceFinalizationStatus;
import com.menghor.ksit.enumations.AttendanceStatus;
import com.menghor.ksit.exceptoins.error.NotFoundException;
import com.menghor.ksit.feature.attendance.dto.response.StudentScoreResponseDto;
import com.menghor.ksit.feature.attendance.dto.update.StudentScoreUpdateDto;
import com.menghor.ksit.feature.attendance.mapper.StudentScoreMapper;
import com.menghor.ksit.feature.attendance.models.AttendanceEntity;
import com.menghor.ksit.feature.attendance.models.ScoreSessionEntity;
import com.menghor.ksit.feature.attendance.models.StudentScoreEntity;
import com.menghor.ksit.feature.attendance.repository.AttendanceRepository;
import com.menghor.ksit.feature.attendance.repository.AttendanceSessionRepository;
import com.menghor.ksit.feature.attendance.repository.ScoreSessionRepository;
import com.menghor.ksit.feature.attendance.repository.StudentScoreRepository;
import com.menghor.ksit.feature.attendance.service.StudentScoreService;
import com.menghor.ksit.feature.attendance.specification.AttendanceSpecification;
import com.menghor.ksit.feature.auth.models.UserEntity;
import com.menghor.ksit.feature.auth.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

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

        // Update fields if provided - use direct point values
        if (updateDto.getAssignmentScore() != null) {
            log.info("Assignment score updated from {} to {} for studentScoreId={}",
                    studentScore.getAssignmentScore(), updateDto.getAssignmentScore(), updateDto.getId());
            studentScore.setAssignmentScore(updateDto.getAssignmentScore());
        }

        if (updateDto.getMidtermScore() != null) {
            log.info("Midterm score updated from {} to {} for studentScoreId={}",
                    studentScore.getMidtermScore(), updateDto.getMidtermScore(), updateDto.getId());
            studentScore.setMidtermScore(updateDto.getMidtermScore());
        }

        if (updateDto.getFinalScore() != null) {
            log.info("Final score updated from {} to {} for studentScoreId={}",
                    studentScore.getFinalScore(), updateDto.getFinalScore(), updateDto.getId());
            studentScore.setFinalScore(updateDto.getFinalScore());
        }

        if (updateDto.getComments() != null) {
            studentScore.setComments(updateDto.getComments());
            log.info("Comments updated for studentScoreId={}", updateDto.getId());
        }

        StudentScoreEntity updatedScore = studentScoreRepository.save(studentScore);

        log.info("Student score update completed for studentScoreId={} - Attendance: {}, Assignment: {}, Midterm: {}, Final: {}, Total: {}",
                updateDto.getId(),
                updatedScore.getAttendanceScore(),
                updatedScore.getAssignmentScore(),
                updatedScore.getMidtermScore(),
                updatedScore.getFinalScore(),
                updatedScore.getTotalScore());

        return studentScoreMapper.toDto(updatedScore);
    }
}