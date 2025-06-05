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
                    requestDto.getTotalPercentage());
        }

        Optional<ScoreConfigurationEntity> existingConfig = scoreConfigRepository.findByStatus(Status.ACTIVE);

        ScoreConfigurationEntity entity;
        if (existingConfig.isPresent()) {
            // Use MapStruct to update existing entity
            entity = existingConfig.get();
            log.info("Updating existing score configuration with ID: {}", entity.getId());

            // MapStruct will handle the field mapping and ignore id, createdAt, updatedAt, status
            scoreConfigMapper.updateEntityFromDto(requestDto, entity);

            log.info("Updated configuration: {}%/{}%/{}%/{}% (Total: {}%)",
                    entity.getAttendancePercentage(),
                    entity.getAssignmentPercentage(),
                    entity.getMidtermPercentage(),
                    entity.getFinalPercentage(),
                    entity.getTotalPercentage());
        } else {
            // Use MapStruct to create new entity
            log.info("Creating new score configuration");
            entity = scoreConfigMapper.toEntity(requestDto);

            log.info("Created new configuration: {}%/{}%/{}%/{}% (Total: {}%)",
                    entity.getAttendancePercentage(),
                    entity.getAssignmentPercentage(),
                    entity.getMidtermPercentage(),
                    entity.getFinalPercentage(),
                    entity.getTotalPercentage());
        }

        ScoreConfigurationEntity savedEntity = scoreConfigRepository.save(entity);
        log.info("Score configuration saved successfully with ID: {}", savedEntity.getId());

        // Use MapStruct to convert to response DTO
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

        // Use MapStruct to convert to response DTO
        return scoreConfigMapper.toResponseDto(entity);
    }

    @Override
    public boolean validatePercentageTotal(ScoreConfigurationRequestDto requestDto) {
        BigDecimal total = requestDto.getTotalPercentage();
        boolean isValid = total.compareTo(BigDecimal.valueOf(100)) == 0;

        log.debug("Validating percentages - Total: {}%, Valid: {}", total, isValid);
        return isValid;
    }

    @Override
    @EventListener(ContextRefreshedEvent.class)
    @Transactional
    public void initializeDefaultConfiguration() {
        log.info("Checking for existing score configuration...");

        if (scoreConfigRepository.countByStatus(Status.ACTIVE) == 0) {
            log.info("No score configuration found, creating default configuration");

            // Use MapStruct to create default configuration
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

    // Additional utility methods using MapStruct

    public ScoreConfigurationResponseDto createCustomConfiguration(
            BigDecimal attendancePercentage,
            BigDecimal assignmentPercentage,
            BigDecimal midtermPercentage,
            BigDecimal finalPercentage) {

        log.info("Creating custom configuration: {}%/{}%/{}%/{}%",
                attendancePercentage, assignmentPercentage, midtermPercentage, finalPercentage);

        // Use MapStruct to create entity from individual percentages
        ScoreConfigurationEntity entity = scoreConfigMapper.createConfiguration(
                attendancePercentage, assignmentPercentage, midtermPercentage, finalPercentage);

        // Validate before saving
        if (!entity.isValidConfiguration()) {
            throw new BadRequestException("Custom configuration percentages must add up to exactly 100%. Current total: " +
                    entity.getTotalPercentage());
        }

        ScoreConfigurationEntity savedEntity = scoreConfigRepository.save(entity);
        log.info("Custom configuration saved with ID: {}", savedEntity.getId());

        // Use MapStruct to convert to response DTO
        return scoreConfigMapper.toResponseDto(savedEntity);
    }

    public ScoreConfigurationEntity cloneConfiguration(Long configId) {
        log.info("Cloning score configuration with ID: {}", configId);

        ScoreConfigurationEntity original = scoreConfigRepository.findById(configId)
                .orElseThrow(() -> new NotFoundException("Score configuration not found with ID: " + configId));

        // Use MapStruct to copy entity (creates new entity without id, createdAt, updatedAt)
        ScoreConfigurationEntity cloned = scoreConfigMapper.copyEntity(original);

        log.info("Cloned configuration from ID: {} with percentages: {}%/{}%/{}%/{}%",
                configId,
                cloned.getAttendancePercentage(),
                cloned.getAssignmentPercentage(),
                cloned.getMidtermPercentage(),
                cloned.getFinalPercentage());

        return cloned;
    }
}