package com.menghor.ksit.feature.attendance.service.impl;

import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.exceptoins.error.BadRequestException;
import com.menghor.ksit.exceptoins.error.NotFoundException;
import com.menghor.ksit.feature.attendance.dto.request.ScoreConfigurationRequestDto;
import com.menghor.ksit.feature.attendance.dto.response.ScoreConfigurationResponseDto;
import com.menghor.ksit.feature.attendance.mapper.ScoreConfigurationMapper;
import com.menghor.ksit.feature.attendance.models.ScoreConfigurationEntity;
import com.menghor.ksit.feature.attendance.repository.ScoreConfigurationRepository;
import com.menghor.ksit.feature.attendance.service.ScoreConfigurationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScoreConfigurationServiceImpl implements ScoreConfigurationService {

    private final ScoreConfigurationRepository scoreConfigRepository;
    private final ScoreConfigurationMapper scoreConfigMapper;

    @Override
    @Transactional
    public ScoreConfigurationResponseDto createOrUpdateScoreConfiguration(ScoreConfigurationRequestDto requestDto) {
        log.info("Creating or updating score configuration with percentages: {}%/{}%/{}%/{}%",
                requestDto.getAttendancePercentage(),
                requestDto.getAssignmentPercentage(),
                requestDto.getMidtermPercentage(),
                requestDto.getFinalPercentage());

        // Validate percentages total 100%
        if (!validatePercentageTotal(requestDto)) {
            throw new BadRequestException("Score percentages must add up to exactly 100%. Current total: " +
                    getTotalPercentage(requestDto));
        }

        Optional<ScoreConfigurationEntity> existingConfig = scoreConfigRepository.findByStatus(Status.ACTIVE);

        ScoreConfigurationEntity entity;
        if (existingConfig.isPresent()) {
            entity = existingConfig.get();
            log.info("Updating existing score configuration with ID: {}", entity.getId());
            scoreConfigMapper.updateEntityFromDto(requestDto, entity);
        } else {
            log.info("Creating new score configuration");
            entity = scoreConfigMapper.toEntity(requestDto);
        }

        ScoreConfigurationEntity savedEntity = scoreConfigRepository.save(entity);
        log.info("Score configuration saved successfully with ID: {}", savedEntity.getId());

        return scoreConfigMapper.toResponseDto(savedEntity);
    }

    @Override
    public ScoreConfigurationResponseDto getScoreConfiguration() {
        log.info("Fetching current score configuration");

        ScoreConfigurationEntity entity = scoreConfigRepository.findByStatus(Status.ACTIVE)
                .orElseThrow(() -> new NotFoundException("No active score configuration found"));

        log.info("Found score configuration ID: {} with percentages: {}%/{}%/{}%/{}%",
                entity.getId(),
                entity.getAttendancePercentage(),
                entity.getAssignmentPercentage(),
                entity.getMidtermPercentage(),
                entity.getFinalPercentage());

        return scoreConfigMapper.toResponseDto(entity);
    }

    private boolean validatePercentageTotal(ScoreConfigurationRequestDto requestDto) {
        Integer total = getTotalPercentage(requestDto);
        boolean isValid = total.equals(100);
        log.debug("Validating percentages - Total: {}%, Valid: {}", total, isValid);
        return isValid;
    }

    private Integer getTotalPercentage(ScoreConfigurationRequestDto requestDto) {
        return requestDto.getAttendancePercentage() +
                requestDto.getAssignmentPercentage() +
                requestDto.getMidtermPercentage() +
                requestDto.getFinalPercentage();
    }

    @EventListener(ContextRefreshedEvent.class)
    @Transactional
    public void initializeDefaultConfiguration() {
        log.info("Checking for existing score configuration...");

        if (scoreConfigRepository.countByStatus(Status.ACTIVE) == 0) {
            log.info("No score configuration found, creating default configuration");
            ScoreConfigurationEntity defaultConfig = scoreConfigMapper.createDefaultConfiguration();
            ScoreConfigurationEntity savedConfig = scoreConfigRepository.save(defaultConfig);

            log.info("Default score configuration created successfully with ID: {} - Attendance: {}%, Assignment: {}%, Midterm: {}%, Final: {}%",
                    savedConfig.getId(),
                    savedConfig.getAttendancePercentage(),
                    savedConfig.getAssignmentPercentage(),
                    savedConfig.getMidtermPercentage(),
                    savedConfig.getFinalPercentage());
        } else {
            log.info("Score configuration already exists, skipping initialization");
        }
    }
}