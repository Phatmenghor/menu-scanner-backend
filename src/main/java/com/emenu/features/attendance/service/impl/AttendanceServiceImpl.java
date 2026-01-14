package com.emenu.features.attendance.service.impl;

import com.emenu.enums.attendance.AttendanceStatus;
import com.emenu.exception.BadRequestException;
import com.emenu.exception.ResourceNotFoundException;
import com.emenu.features.attendance.dto.filter.AttendanceFilterRequest;
import com.emenu.features.attendance.dto.request.AttendanceCheckInRequest;
import com.emenu.features.attendance.dto.request.AttendanceCheckOutRequest;
import com.emenu.features.attendance.dto.response.AttendanceResponse;
import com.emenu.features.attendance.mapper.AttendanceMapper;
import com.emenu.features.attendance.models.Attendance;
import com.emenu.features.attendance.models.AttendancePolicy;
import com.emenu.features.attendance.models.WorkSchedule;
import com.emenu.features.attendance.repository.AttendanceRepository;
import com.emenu.features.attendance.repository.WorkScheduleRepository;
import com.emenu.features.attendance.service.AttendanceService;
import com.emenu.features.attendance.specification.AttendanceSpecification;
import com.emenu.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final WorkScheduleRepository scheduleRepository;
    private final AttendanceMapper attendanceMapper;

    @Override
    @Transactional
    public AttendanceResponse checkIn(AttendanceCheckInRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        // Check if already checked in today
        attendanceRepository.findByUserIdAndAttendanceDate(userId, today)
                .ifPresent(a -> {
                    throw new BadRequestException("Already checked in today");
                });

        // Get work schedule
        WorkSchedule schedule = scheduleRepository.findById(request.getWorkScheduleId())
                .orElseThrow(() -> new ResourceNotFoundException("Work schedule not found"));

        if (!schedule.getUser().getId().equals(userId)) {
            throw new BadRequestException("Work schedule does not belong to current user");
        }

        // Validate if today is a work day
        DayOfWeek todayDayOfWeek = today.getDayOfWeek();
        if (!schedule.getWorkDays().contains(todayDayOfWeek)) {
            throw new BadRequestException("Today is not a work day according to your schedule");
        }

        AttendancePolicy policy = schedule.getAttendancePolicy();

        // Validate location if required
        if (policy.getRequireLocationCheck()) {
            validateLocation(request.getLatitude(), request.getLongitude(), policy);
        }

        // Determine shift times
        LocalTime shiftStartTime = schedule.getCustomStartTime() != null ?
                schedule.getCustomStartTime() : policy.getStartTime();

        // Calculate late minutes
        LocalTime currentTime = now.toLocalTime();
        int lateMinutes = 0;
        AttendanceStatus status = AttendanceStatus.PRESENT;

        if (currentTime.isAfter(shiftStartTime.plusMinutes(policy.getLateThresholdMinutes()))) {
            lateMinutes = (int) ChronoUnit.MINUTES.between(shiftStartTime, currentTime);
            status = AttendanceStatus.LATE;
        }

        // Create attendance record
        Attendance attendance = Attendance.builder()
                .user(schedule.getUser())
                .workSchedule(schedule)
                .attendanceDate(today)
                .checkInTime(now)
                .checkInLatitude(request.getLatitude())
                .checkInLongitude(request.getLongitude())
                .checkInAddress(request.getAddress())
                .checkInNote(request.getNote())
                .lateMinutes(lateMinutes)
                .status(status)
                .build();

        Attendance savedAttendance = attendanceRepository.save(attendance);
        return attendanceMapper.toResponse(savedAttendance);
    }

    @Override
    @Transactional
    public AttendanceResponse checkOut(Long attendanceId, AttendanceCheckOutRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        LocalDateTime now = LocalDateTime.now();

        Attendance attendance = attendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance record not found"));

        if (!attendance.getUser().getId().equals(userId)) {
            throw new BadRequestException("Attendance record does not belong to current user");
        }

        if (attendance.getCheckOutTime() != null) {
            throw new BadRequestException("Already checked out");
        }

        AttendancePolicy policy = attendance.getWorkSchedule().getAttendancePolicy();

        // Validate location if required
        if (policy.getRequireLocationCheck()) {
            validateLocation(request.getLatitude(), request.getLongitude(), policy);
        }

        // Update check-out details
        attendance.setCheckOutTime(now);
        attendance.setCheckOutLatitude(request.getLatitude());
        attendance.setCheckOutLongitude(request.getLongitude());
        attendance.setCheckOutAddress(request.getAddress());
        attendance.setCheckOutNote(request.getNote());

        // Calculate total work minutes
        long workMinutes = ChronoUnit.MINUTES.between(attendance.getCheckInTime(), now);
        attendance.setTotalWorkMinutes((int) workMinutes);

        // Update status based on total work time
        if (workMinutes < policy.getHalfDayThresholdMinutes()) {
            attendance.setStatus(AttendanceStatus.HALF_DAY);
        }

        Attendance updatedAttendance = attendanceRepository.save(attendance);
        return attendanceMapper.toResponse(updatedAttendance);
    }

    @Override
    @Transactional(readOnly = true)
    public AttendanceResponse getAttendanceById(Long id) {
        Attendance attendance = attendanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance record not found"));

        return attendanceMapper.toResponse(attendance);
    }

    @Override
    @Transactional(readOnly = true)
    public AttendanceResponse getTodayAttendance(Long userId) {
        LocalDate today = LocalDate.now();
        Attendance attendance = attendanceRepository.findByUserIdAndAttendanceDate(userId, today)
                .orElseThrow(() -> new ResourceNotFoundException("No attendance record found for today"));

        return attendanceMapper.toResponse(attendance);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceResponse> getAttendanceByUserId(Long userId, LocalDate startDate, LocalDate endDate) {
        return attendanceRepository.findByUserIdAndAttendanceDateBetween(userId, startDate, endDate)
                .stream()
                .map(attendanceMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AttendanceResponse> getAllAttendances(AttendanceFilterRequest filterRequest, Pageable pageable) {
        Specification<Attendance> spec = AttendanceSpecification.withFilters(filterRequest);
        return attendanceRepository.findAll(spec, pageable)
                .map(attendanceMapper::toResponse);
    }

    private void validateLocation(Double latitude, Double longitude, AttendancePolicy policy) {
        if (latitude == null || longitude == null) {
            throw new BadRequestException("Location coordinates are required");
        }

        double distance = calculateDistance(
                latitude, longitude,
                policy.getOfficeLatitude(), policy.getOfficeLongitude()
        );

        if (distance > policy.getAllowedRadiusMeters()) {
            throw new BadRequestException(
                    String.format("You are %.0f meters away from office location. Maximum allowed: %d meters",
                            distance, policy.getAllowedRadiusMeters())
            );
        }
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int EARTH_RADIUS = 6371000; // meters

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }
}
