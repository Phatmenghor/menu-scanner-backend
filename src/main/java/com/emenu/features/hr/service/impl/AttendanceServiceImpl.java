package com.emenu.features.hr.service.impl;

import com.emenu.enums.hr.CheckInType;
import com.emenu.features.hr.dto.filter.AttendanceFilterRequest;
import com.emenu.features.hr.dto.request.AttendanceCheckInRequest;
import com.emenu.features.hr.dto.response.AttendanceResponse;
import com.emenu.features.hr.dto.update.AttendanceUpdateRequest;
import com.emenu.features.hr.mapper.AttendanceMapper;
import com.emenu.features.hr.models.Attendance;
import com.emenu.features.hr.models.AttendanceCheckIn;
import com.emenu.features.hr.models.WorkSchedule;
import com.emenu.features.hr.repository.AttendanceRepository;
import com.emenu.features.hr.repository.WorkScheduleRepository;
import com.emenu.features.hr.service.AttendanceService;
import com.emenu.exception.custom.BusinessValidationException;
import com.emenu.exception.custom.ResourceNotFoundException;
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.mapper.PaginationMapper;
import com.emenu.shared.pagination.PaginationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final WorkScheduleRepository workScheduleRepository;
    private final AttendanceStatusEnumRepository statusEnumRepository;
    private final AttendanceMapper mapper;
    private final PaginationMapper paginationMapper;

    @Override
    public AttendanceResponse checkIn(AttendanceCheckInRequest request, UUID userId, UUID businessId) {
        log.info("Processing check-in for user: {}, type: {}", userId, request.getCheckInType());

        // Validate work schedule
        WorkSchedule schedule = workScheduleRepository.findByIdAndIsDeletedFalse(request.getWorkScheduleId())
                .orElseThrow(() -> new ResourceNotFoundException("Work schedule not found"));

        // Validate schedule belongs to user
        if (!schedule.getUserId().equals(userId)) {
            throw new BusinessValidationException("Work schedule does not belong to user");
        }

        LocalDate today = LocalDate.now();

        // Get or create attendance for today
        Attendance attendance = attendanceRepository
                .findByUserIdAndAttendanceDateAndIsDeletedFalse(userId, today)
                .orElseGet(() -> {
                    Attendance newAttendance = Attendance.builder()
                            .userId(userId)
                            .businessId(businessId)
                            .workScheduleId(request.getWorkScheduleId())
                            .attendanceDate(today)
                            .build();
                    return attendanceRepository.save(newAttendance);
                });

        // Check if this type of check-in already exists
        Optional<AttendanceCheckIn> existingCheckIn = attendance.getCheckIns().stream()
                .filter(c -> c.getCheckInType() == request.getCheckInType())
                .findFirst();

        if (existingCheckIn.isPresent()) {
            throw new BusinessValidationException(
                    "Already checked in for type: " + request.getCheckInType());
        }

        // Validate check-in sequence
        validateCheckInSequence(attendance, request.getCheckInType(), schedule.getRequiredCheckIns());

        // Create check-in record
        AttendanceCheckIn checkIn = AttendanceCheckIn.builder()
                .checkInType(request.getCheckInType())
                .checkInTime(LocalDateTime.now())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .remarks(request.getRemarks())
                .build();

        attendance.addCheckIn(checkIn);

        // Calculate metrics if this is the END check-in
        if (request.getCheckInType() == CheckInType.END) {
            calculateAttendanceMetrics(attendance, schedule);
        }

        attendance = attendanceRepository.save(attendance);

        return enrichResponse(mapper.toResponse(attendance));
    }

    private void validateCheckInSequence(Attendance attendance,
                                         CheckInType requestedType,
                                         Integer requiredCheckIns) {

        int currentCount = attendance.getCheckIns().size();

        // START must be first
        if (currentCount == 0 && requestedType != CheckInType.START) {
            throw new BusinessValidationException("Must START check-in first");
        }

        // For 2 check-ins: START -> END
        if (requiredCheckIns == 2) {
            if (currentCount == 1 && requestedType != CheckInType.END) {
                throw new BusinessValidationException("Only START and END check-ins allowed");
            }
        }

        // For 3 check-ins: START -> MIDDLE_OUT -> END
        if (requiredCheckIns == 3) {
            if (currentCount == 1 && requestedType != CheckInType.MIDDLE_OUT) {
                throw new BusinessValidationException("Next check-in should be MIDDLE_OUT");
            }
            if (currentCount == 2 && requestedType != CheckInType.END) {
                throw new BusinessValidationException("Next check-in should be END");
            }
        }

        // For 4 check-ins: START -> MIDDLE_OUT -> MIDDLE_IN -> END
        if (requiredCheckIns == 4) {
            if (currentCount == 1 && requestedType != CheckInType.MIDDLE_OUT) {
                throw new BusinessValidationException("Next check-in should be MIDDLE_OUT");
            }
            if (currentCount == 2 && requestedType != CheckInType.MIDDLE_IN) {
                throw new BusinessValidationException("Next check-in should be MIDDLE_IN");
            }
            if (currentCount == 3 && requestedType != CheckInType.END) {
                throw new BusinessValidationException("Next check-in should be END");
            }
        }
    }

    private void calculateAttendanceMetrics(Attendance attendance, WorkSchedule schedule) {
        // Find START check-in time
        Optional<LocalDateTime> startTimeOpt = attendance.getCheckIns().stream()
                .filter(c -> c.getCheckInType() == CheckInType.START)
                .findFirst()
                .map(AttendanceCheckIn::getCheckInTime);

        // Find END check-in time
        Optional<LocalDateTime> endTimeOpt = attendance.getCheckIns().stream()
                .filter(c -> c.getCheckInType() == CheckInType.END)
                .findFirst()
                .map(AttendanceCheckIn::getCheckInTime);

        if (startTimeOpt.isPresent() && endTimeOpt.isPresent()) {
            LocalDateTime startTime = startTimeOpt.get();
            LocalDateTime endTime = endTimeOpt.get();

            // Calculate total work time minus breaks
            long totalMinutes = Duration.between(startTime, endTime).toMinutes();

            // Subtract break time if there are middle check-ins
            if (schedule.getRequiredCheckIns() >= 3) {
                Optional<LocalDateTime> middleOutOpt = attendance.getCheckIns().stream()
                        .filter(c -> c.getCheckInType() == CheckInType.MIDDLE_OUT)
                        .findFirst()
                        .map(AttendanceCheckIn::getCheckInTime);

                Optional<LocalDateTime> middleInOpt = attendance.getCheckIns().stream()
                        .filter(c -> c.getCheckInType() == CheckInType.MIDDLE_IN)
                        .findFirst()
                        .map(AttendanceCheckIn::getCheckInTime);

                if (middleOutOpt.isPresent() && middleInOpt.isPresent()) {
                    long breakMinutes = Duration.between(middleOutOpt.get(), middleInOpt.get()).toMinutes();
                    totalMinutes -= breakMinutes;
                }
            }

            attendance.setTotalWorkMinutes((int) totalMinutes);

            // Calculate late minutes
            LocalDateTime expectedStart = LocalDateTime.of(
                    attendance.getAttendanceDate(),
                    schedule.getStartTime()
            );

            if (startTime.isAfter(expectedStart)) {
                long lateMinutes = Duration.between(expectedStart, startTime).toMinutes();
                attendance.setLateMinutes((int) lateMinutes);
            } else {
                attendance.setLateMinutes(0);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public AttendanceResponse getById(UUID id) {
        Attendance attendance = attendanceRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance not found"));
        return enrichResponse(mapper.toResponse(attendance));
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<AttendanceResponse> getAll(AttendanceFilterRequest filter) {
        Pageable pageable = PaginationUtils.createPageable(
                filter.getPageNo(),
                filter.getPageSize(),
                filter.getSortBy(),
                filter.getSortDirection()
        );

        Page<Attendance> page = attendanceRepository.findWithFilters(
                filter.getBusinessId(),
                filter.getUserId(),
                filter.getStartDate(),
                filter.getEndDate(),
                filter.getStatusEnumId(),
                filter.getSearch(),
                pageable
        );

        return paginationMapper.toPaginationResponse(page,
                attendances -> attendances.stream()
                        .map(mapper::toResponse)
                        .map(this::enrichResponse)
                        .toList());
    }

    @Override
    public AttendanceResponse update(UUID id, AttendanceUpdateRequest request) {
        Attendance attendance = attendanceRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance not found"));

        // Update status enum if provided
        if (request.getStatusEnumName() != null) {
            final UUID businessId = attendance.getBusinessId();
            final String statusEnumName = request.getStatusEnumName();

            AttendanceStatusEnum statusEnum = statusEnumRepository
                    .findByBusinessIdAndEnumNameAndIsDeletedFalse(businessId, statusEnumName)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Status enum not found: " + statusEnumName));
            attendance.setStatusEnumId(statusEnum.getId());
        }

        if (request.getRemarks() != null) {
            attendance.setRemarks(request.getRemarks());
        }

        attendance = attendanceRepository.save(attendance);
        return enrichResponse(mapper.toResponse(attendance));
    }

    @Override
    public AttendanceResponse delete(UUID id) {
        Attendance attendance = attendanceRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance not found"));
        attendance.softDelete();
        attendance = attendanceRepository.save(attendance);
        return enrichResponse(mapper.toResponse(attendance));
    }

    private AttendanceResponse enrichResponse(AttendanceResponse response) {
        if (response.getStatusEnumId() != null) {
            final UUID statusEnumId = response.getStatusEnumId();
            statusEnumRepository.findByIdAndIsDeletedFalse(statusEnumId)
                    .ifPresent(statusEnum -> response.setStatusEnumName(statusEnum.getEnumName()));
        }
        return response;
    }
}