package com.emenu.features.hr.service.impl;

import com.emenu.enums.hr.AttendanceStatusEnum;
import com.emenu.enums.hr.CheckInType;
import com.emenu.exception.custom.BusinessValidationException;
import com.emenu.exception.custom.ResourceNotFoundException;
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
import com.emenu.shared.dto.PaginationResponse;
import com.emenu.shared.mapper.PaginationMapper;
import com.emenu.shared.pagination.PaginationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final WorkScheduleRepository workScheduleRepository;
    private final AttendanceMapper mapper;
    private final PaginationMapper paginationMapper;

    @Override
    public AttendanceResponse checkIn(AttendanceCheckInRequest request, UUID userId, UUID businessId) {
        log.info("Processing check-in for user: {}, type: {}", userId, request.getCheckInType());

        // Validate work schedule exists and belongs to user
        WorkSchedule schedule = workScheduleRepository.findByIdAndIsDeletedFalse(request.getWorkScheduleId())
                .orElseThrow(() -> new ResourceNotFoundException("Work schedule not found"));

        if (!schedule.getUserId().equals(userId)) {
            throw new BusinessValidationException("Work schedule does not belong to user");
        }

        LocalDate today = LocalDate.now();
        DayOfWeek dayOfWeek = today.getDayOfWeek();

        // Check if today is a working day for the schedule
        if (!schedule.getWorkDays().contains(dayOfWeek)) {
            throw new BusinessValidationException("Today is not a working day according to your schedule");
        }

        // Get or create attendance record for today
        Attendance attendance = attendanceRepository
                .findByUserIdAndAttendanceDateAndIsDeletedFalse(userId, today)
                .orElseGet(() -> createNewAttendance(userId, businessId, request.getWorkScheduleId(), today));

        // Validate check-in sequence
        validateCheckInSequence(attendance, request.getCheckInType());

        // Create check-in record
        AttendanceCheckIn checkIn = AttendanceCheckIn.builder()
                .checkInType(request.getCheckInType())
                .checkInTime(LocalDateTime.now())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .remarks(request.getRemarks())
                .build();

        attendance.addCheckIn(checkIn);

        // Calculate attendance status when checking out
        if (request.getCheckInType() == CheckInType.END) {
            calculateAttendanceStatus(attendance, schedule);
        } else {
            // Update status to present on check-in
            attendance.setStatus(AttendanceStatusEnum.PRESENT);
        }

        attendance = attendanceRepository.save(attendance);
        log.info("Check-in successful for user: {}, type: {}, status: {}",
                userId, request.getCheckInType(), attendance.getStatus());

        return mapper.toResponse(attendance);
    }

    private Attendance createNewAttendance(UUID userId, UUID businessId, UUID workScheduleId, LocalDate date) {
        Attendance newAttendance = Attendance.builder()
                .userId(userId)
                .businessId(businessId)
                .workScheduleId(workScheduleId)
                .attendanceDate(date)
                .status(AttendanceStatusEnum.ABSENT)
                .build();
        return attendanceRepository.save(newAttendance);
    }

    private void validateCheckInSequence(Attendance attendance, CheckInType requestedType) {
        int currentCount = attendance.getCheckIns().size();

        // Check for duplicate check-in type
        boolean checkInExists = attendance.getCheckIns().stream()
                .anyMatch(c -> c.getCheckInType() == requestedType);

        if (checkInExists) {
            throw new BusinessValidationException(
                    "Already checked in for type: " + requestedType);
        }

        // Validate sequence: must start with START, then END
        if (currentCount == 0 && requestedType != CheckInType.START) {
            throw new BusinessValidationException("Must clock in (START) first");
        }

        if (currentCount == 1 && requestedType != CheckInType.END) {
            throw new BusinessValidationException("Can only clock out (END) after clocking in");
        }

        if (currentCount >= 2) {
            throw new BusinessValidationException("Already completed check-in for today");
        }
    }

    private void calculateAttendanceStatus(Attendance attendance, WorkSchedule schedule) {
        AttendanceCheckIn startCheckIn = attendance.getCheckIns().stream()
                .filter(c -> c.getCheckInType() == CheckInType.START)
                .findFirst()
                .orElseThrow(() -> new BusinessValidationException("Start check-in not found"));

        AttendanceCheckIn endCheckIn = attendance.getCheckIns().stream()
                .filter(c -> c.getCheckInType() == CheckInType.END)
                .findFirst()
                .orElseThrow(() -> new BusinessValidationException("End check-in not found"));

        LocalDateTime startTime = startCheckIn.getCheckInTime();
        LocalDateTime endTime = endCheckIn.getCheckInTime();
        LocalDateTime expectedStart = LocalDateTime.of(attendance.getAttendanceDate(), schedule.getStartTime());

        // Calculate if late
        boolean isLate = startTime.isAfter(expectedStart);

        // Calculate total work duration
        Duration workDuration = Duration.between(startTime, endTime);
        long totalWorkMinutes = workDuration.toMinutes();

        // Deduct break time if configured
        if (schedule.getBreakStartTime() != null && schedule.getBreakEndTime() != null) {
            Duration breakDuration = Duration.between(schedule.getBreakStartTime(), schedule.getBreakEndTime());
            totalWorkMinutes -= breakDuration.toMinutes();
        }

        // Calculate expected work hours
        Duration expectedWorkDuration = Duration.between(schedule.getStartTime(), schedule.getEndTime());
        long expectedWorkMinutes = expectedWorkDuration.toMinutes();

        if (schedule.getBreakStartTime() != null && schedule.getBreakEndTime() != null) {
            Duration breakDuration = Duration.between(schedule.getBreakStartTime(), schedule.getBreakEndTime());
            expectedWorkMinutes -= breakDuration.toMinutes();
        }

        // Determine final status
        // Consider half-day if worked less than 60% of expected hours
        double workPercentage = (double) totalWorkMinutes / expectedWorkMinutes * 100;

        if (isLate) {
            attendance.setStatus(AttendanceStatusEnum.LATE);
        } else if (workPercentage < 60) {
            attendance.setStatus(AttendanceStatusEnum.HALF_DAY);
        } else {
            attendance.setStatus(AttendanceStatusEnum.PRESENT);
        }

        log.info("Calculated attendance status: {}, worked: {} minutes, expected: {} minutes, percentage: {}%",
                attendance.getStatus(), totalWorkMinutes, expectedWorkMinutes, String.format("%.2f", workPercentage));
    }

    @Override
    @Transactional(readOnly = true)
    public AttendanceResponse getById(UUID id) {
        Attendance attendance = attendanceRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance not found"));
        return mapper.toResponse(attendance);
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
                filter.getSearch(),
                pageable
        );

        return paginationMapper.toPaginationResponse(page,
                attendances -> attendances.stream().map(mapper::toResponse).toList());
    }

    @Override
    public AttendanceResponse update(UUID id, AttendanceUpdateRequest request) {
        Attendance attendance = attendanceRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance not found"));

        if (request.getRemarks() != null) {
            attendance.setRemarks(request.getRemarks());
        }

        attendance = attendanceRepository.save(attendance);
        return mapper.toResponse(attendance);
    }

    @Override
    public AttendanceResponse delete(UUID id) {
        Attendance attendance = attendanceRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance not found"));
        attendance.softDelete();
        attendance = attendanceRepository.save(attendance);
        return mapper.toResponse(attendance);
    }
}
