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

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
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

        WorkSchedule schedule = workScheduleRepository.findByIdAndIsDeletedFalse(request.getWorkScheduleId())
                .orElseThrow(() -> new ResourceNotFoundException("Work schedule not found"));

        if (!schedule.getUserId().equals(userId)) {
            throw new BusinessValidationException("Work schedule does not belong to user");
        }

        LocalDate today = LocalDate.now();

        Attendance attendance = attendanceRepository
                .findByUserIdAndAttendanceDateAndIsDeletedFalse(userId, today)
                .orElseGet(() -> {
                    Attendance newAttendance = Attendance.builder()
                            .userId(userId)
                            .businessId(businessId)
                            .workScheduleId(request.getWorkScheduleId())
                            .attendanceDate(today)
                            .status(AttendanceStatusEnum.ABSENT)
                            .build();
                    return attendanceRepository.save(newAttendance);
                });

        boolean checkInExists = attendance.getCheckIns().stream()
                .anyMatch(c -> c.getCheckInType() == request.getCheckInType());

        if (checkInExists) {
            throw new BusinessValidationException(
                    "Already checked in for type: " + request.getCheckInType());
        }

        validateCheckInSequence(attendance, request.getCheckInType(), schedule.getRequiredCheckIns());

        AttendanceCheckIn checkIn = AttendanceCheckIn.builder()
                .checkInType(request.getCheckInType())
                .checkInTime(LocalDateTime.now())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .remarks(request.getRemarks())
                .build();

        attendance.addCheckIn(checkIn);

        if (request.getCheckInType() == CheckInType.END) {
            calculateAttendanceMetrics(attendance, schedule);
        }

        attendance = attendanceRepository.save(attendance);
        log.info("Check-in successful for user: {}, status: {}", userId, attendance.getStatus());

        return mapper.toResponse(attendance);
    }

    private void validateCheckInSequence(Attendance attendance, CheckInType requestedType, Integer requiredCheckIns) {
        int currentCount = attendance.getCheckIns().size();

        if (currentCount == 0 && requestedType != CheckInType.START) {
            throw new BusinessValidationException("Must START check-in first");
        }

        if (requiredCheckIns == 2) {
            if (currentCount == 1 && requestedType != CheckInType.END) {
                throw new BusinessValidationException("Only START and END check-ins allowed");
            }
        } else if (requiredCheckIns == 3) {
            if (currentCount == 1 && requestedType != CheckInType.MIDDLE_OUT) {
                throw new BusinessValidationException("Next check-in should be MIDDLE_OUT");
            }
            if (currentCount == 2 && requestedType != CheckInType.END) {
                throw new BusinessValidationException("Next check-in should be END");
            }
        } else if (requiredCheckIns == 4) {
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
        Optional<LocalDateTime> startTimeOpt = attendance.getCheckIns().stream()
                .filter(c -> c.getCheckInType() == CheckInType.START)
                .findFirst()
                .map(AttendanceCheckIn::getCheckInTime);

        Optional<LocalDateTime> endTimeOpt = attendance.getCheckIns().stream()
                .filter(c -> c.getCheckInType() == CheckInType.END)
                .findFirst()
                .map(AttendanceCheckIn::getCheckInTime);

        if (startTimeOpt.isPresent() && endTimeOpt.isPresent()) {
            LocalDateTime startTime = startTimeOpt.get();
            LocalDateTime endTime = endTimeOpt.get();

            long totalMinutes = Duration.between(startTime, endTime).toMinutes();

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

            LocalDateTime expectedStart = LocalDateTime.of(attendance.getAttendanceDate(), schedule.getStartTime());

            if (startTime.isAfter(expectedStart)) {
                long lateMinutes = Duration.between(expectedStart, startTime).toMinutes();
                attendance.setLateMinutes((int) lateMinutes);
                attendance.setStatus(AttendanceStatusEnum.LATE);
            } else {
                attendance.setLateMinutes(0);
                attendance.setStatus(AttendanceStatusEnum.PRESENT);
            }
        }
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