package com.emenu.features.hr.service.impl;

import com.emenu.enums.hr.AttendanceStatusEnum;
import com.emenu.enums.hr.CheckInType;
import com.emenu.exception.custom.BusinessValidationException;
import com.emenu.exception.custom.ResourceNotFoundException;
import com.emenu.features.auth.mapper.UserMapper;
import com.emenu.features.hr.dto.filter.AttendanceFilterRequest;
import com.emenu.features.hr.dto.helper.AttendanceCheckInCreateHelper;
import com.emenu.features.hr.dto.helper.AttendanceCreateHelper;
import com.emenu.features.hr.dto.request.AttendanceCheckInRequest;
import com.emenu.features.hr.dto.response.AttendanceResponse;
import com.emenu.features.hr.dto.update.AttendanceUpdateRequest;
import com.emenu.features.hr.mapper.AttendanceCheckInMapper;
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
import com.emenu.shared.utils.DateTimeUtils;
import com.emenu.shared.utils.StringFormatUtils;
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
    private final AttendanceCheckInMapper checkInMapper;
    private final PaginationMapper paginationMapper;
    private final UserMapper userMapper;

    @Override
    public AttendanceResponse checkIn(AttendanceCheckInRequest request, UUID userId, UUID businessId) {
        log.info("Processing check-in for user: {}, type: {}", userId, request.getCheckInType());

        WorkSchedule schedule = workScheduleRepository.findByIdAndIsDeletedFalse(request.getWorkScheduleId())
                .orElseThrow(() -> new ResourceNotFoundException("Work schedule not found"));

        if (!schedule.getUserId().equals(userId)) {
            throw new BusinessValidationException("Work schedule does not belong to user");
        }

        LocalDate today = LocalDate.now();
        DayOfWeek dayOfWeek = today.getDayOfWeek();

        if (!schedule.getWorkDays().contains(dayOfWeek)) {
            throw new BusinessValidationException("Today is not a working day according to your schedule");
        }

        Attendance attendance = attendanceRepository
                .findByUserIdAndAttendanceDateAndIsDeletedFalse(userId, today)
                .orElseGet(() -> createNewAttendance(userId, businessId, request.getWorkScheduleId(), today));

        validateCheckInSequence(attendance, request.getCheckInType());

        AttendanceCheckInCreateHelper checkInHelper = AttendanceCheckInCreateHelper.builder()
                .checkInType(request.getCheckInType())
                .checkInTime(LocalDateTime.now())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .remarks(request.getRemarks())
                .build();

        AttendanceCheckIn checkIn = checkInMapper.createFromHelper(checkInHelper);
        attendance.addCheckIn(checkIn);

        if (request.getCheckInType() == CheckInType.END) {
            calculateAttendanceStatus(attendance, schedule);
        } else {
            attendance.setStatus(AttendanceStatusEnum.PRESENT);
        }

        attendance = attendanceRepository.save(attendance);
        log.info("Check-in successful for user: {}, type: {}, status: {}",
                userId, request.getCheckInType(), attendance.getStatus());

        return enrichWithUserInfo(mapper.toResponse(attendance), attendance);
    }

    private Attendance createNewAttendance(UUID userId, UUID businessId, UUID workScheduleId, LocalDate date) {
        AttendanceCreateHelper helper = AttendanceCreateHelper.builder()
                .userId(userId)
                .businessId(businessId)
                .workScheduleId(workScheduleId)
                .attendanceDate(date)
                .status(AttendanceStatusEnum.ABSENT)
                .build();

        Attendance newAttendance = mapper.createFromHelper(helper);
        return attendanceRepository.save(newAttendance);
    }

    private void validateCheckInSequence(Attendance attendance, CheckInType requestedType) {
        int currentCount = attendance.getCheckIns().size();

        boolean checkInExists = attendance.getCheckIns().stream()
                .anyMatch(c -> c.getCheckInType() == requestedType);

        if (checkInExists) {
            throw new BusinessValidationException("Already checked in for type: " + requestedType);
        }

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

        boolean isLate = startTime.isAfter(expectedStart);

        long totalWorkMinutes = DateTimeUtils.calculateDurationMinutes(startTime, endTime);

        if (schedule.getBreakStartTime() != null && schedule.getBreakEndTime() != null) {
            Duration breakDuration = Duration.between(schedule.getBreakStartTime(), schedule.getBreakEndTime());
            totalWorkMinutes -= breakDuration.toMinutes();
        }

        Duration expectedWorkDuration = Duration.between(schedule.getStartTime(), schedule.getEndTime());
        long expectedWorkMinutes = expectedWorkDuration.toMinutes();

        if (schedule.getBreakStartTime() != null && schedule.getBreakEndTime() != null) {
            Duration breakDuration = Duration.between(schedule.getBreakStartTime(), schedule.getBreakEndTime());
            expectedWorkMinutes -= breakDuration.toMinutes();
        }

        double workPercentage = DateTimeUtils.calculateWorkPercentage(totalWorkMinutes, expectedWorkMinutes);

        if (isLate) {
            attendance.setStatus(AttendanceStatusEnum.LATE);
        } else if (workPercentage < 60) {
            attendance.setStatus(AttendanceStatusEnum.HALF_DAY);
        } else {
            attendance.setStatus(AttendanceStatusEnum.PRESENT);
        }

        log.info("Calculated attendance status: {}, worked: {} minutes, expected: {} minutes, percentage: {}",
                attendance.getStatus(), totalWorkMinutes, expectedWorkMinutes,
                StringFormatUtils.formatPercentage(workPercentage));
    }

    @Override
    @Transactional(readOnly = true)
    public AttendanceResponse getById(UUID id) {
        Attendance attendance = attendanceRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance not found"));
        return enrichWithUserInfo(mapper.toResponse(attendance), attendance);
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
                attendances -> attendances.stream()
                        .map(att -> enrichWithUserInfo(mapper.toResponse(att), att))
                        .toList());
    }

    @Override
    public AttendanceResponse update(UUID id, AttendanceUpdateRequest request) {
        Attendance attendance = attendanceRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance not found"));

        mapper.updateEntity(request, attendance);
        attendance = attendanceRepository.save(attendance);

        return enrichWithUserInfo(mapper.toResponse(attendance), attendance);
    }

    @Override
    public AttendanceResponse delete(UUID id) {
        Attendance attendance = attendanceRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance not found"));
        attendance.softDelete();
        attendance = attendanceRepository.save(attendance);
        return enrichWithUserInfo(mapper.toResponse(attendance), attendance);
    }

    private AttendanceResponse enrichWithUserInfo(AttendanceResponse response, Attendance attendance) {
        if (attendance.getUser() != null) {
            response.setUserInfo(userMapper.toUserBasicInfo(attendance.getUser()));
        }
        return response;
    }
}
