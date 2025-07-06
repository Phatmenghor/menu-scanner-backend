package com.menghor.ksit.feature.attendance.service.impl;

import com.menghor.ksit.enumations.AttendanceStatus;
import com.menghor.ksit.feature.attendance.dto.request.AttendanceHistoryFilterDto;
import com.menghor.ksit.feature.attendance.dto.response.AttendanceDto;
import com.menghor.ksit.feature.attendance.dto.update.AttendanceUpdateRequest;
import com.menghor.ksit.feature.attendance.mapper.AttendanceMapper;
import com.menghor.ksit.feature.attendance.models.AttendanceEntity;
import com.menghor.ksit.feature.attendance.repository.AttendanceRepository;
import com.menghor.ksit.feature.attendance.repository.AttendanceSessionRepository;
import com.menghor.ksit.feature.attendance.service.AttendanceService;
import com.menghor.ksit.feature.attendance.specification.AttendanceSpecification;
import com.menghor.ksit.utils.database.CustomPaginationResponseDto;
import com.menghor.ksit.utils.pagiantion.PaginationUtils;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final AttendanceSessionRepository sessionRepository;
    private final AttendanceMapper attendanceMapper;

    @Override
    public AttendanceDto findById(Long id) {
        return attendanceRepository.findById(id)
                .map(attendanceMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Attendance not found with id: " + id));
    }

    @Override
    public Page<AttendanceDto> findAll(Long studentId, Long sessionId, AttendanceStatus status,
                                       Pageable pageable) {
        Specification<AttendanceEntity> spec = Specification.where(AttendanceSpecification.hasStudentId(studentId))
                .and(AttendanceSpecification.hasSessionId(sessionId))
                .and(AttendanceSpecification.hasStatus(status));

        return attendanceRepository.findAll(spec, pageable)
                .map(attendanceMapper::toDto);
    }

    @Override
    @Transactional
    public AttendanceDto updateAttendance(AttendanceUpdateRequest request) {
        AttendanceEntity attendance = attendanceRepository.findById(request.getId())
                .orElseThrow(() -> new EntityNotFoundException("Attendance not found with id: " + request.getId()));

        // Only update if not null
        if (request.getStatus() != null) {
            attendance.setStatus(request.getStatus());
        }

        if (request.getAttendanceType() != null) {
            attendance.setAttendanceType(request.getAttendanceType());
        }

        if (request.getComment() != null) {
            attendance.setComment(request.getComment());
        }

        // Always update recorded time when any field is modified
        attendance.setRecordedTime(LocalDateTime.now());
        return attendanceMapper.toDto(attendanceRepository.save(attendance));
    }

    @Override
    public List<AttendanceDto> findAllAttendanceHistory(AttendanceHistoryFilterDto filterDto) {
        log.info("Fetching all attendance history (no pagination) with filter: {}", filterDto);

        Specification<AttendanceEntity> spec = AttendanceSpecification.combine(filterDto);

        // Sort by recordedTime descending for consistency
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");

        List<AttendanceEntity> attendanceList = attendanceRepository.findAll(spec, sort);

        List<AttendanceDto> result = attendanceList.stream()
                .map(attendanceMapper::toDto)
                .collect(Collectors.toList());

        log.info("Retrieved {} total attendance records without pagination", result.size());

        return result;
    }

    @Override
    public Long countAttendanceHistory(AttendanceHistoryFilterDto filterDto) {
        log.info("Counting attendance history with filter: {}", filterDto);

        Specification<AttendanceEntity> spec = AttendanceSpecification.combine(filterDto);

        Long count = attendanceRepository.count(spec);

        log.info("Found {} total attendance records matching filter criteria", count);

        return count;
    }

    // Implementation
    @Override
    public CustomPaginationResponseDto<AttendanceDto> findAttendanceHistory(AttendanceHistoryFilterDto filterDto) {
        log.info("Fetching attendance history with filter: {}", filterDto);

        Pageable pageable = PaginationUtils.createPageable(
                filterDto.getPageNo(),
                filterDto.getPageSize(),
                "createdAt",
                "DESC"
        );

        Specification<AttendanceEntity> spec = AttendanceSpecification.combine(filterDto);

        Page<AttendanceEntity> attendancePage = attendanceRepository.findAll(spec, pageable);

        List<AttendanceDto> content = attendancePage.getContent().stream()
                .map(attendanceMapper::toDto)
                .collect(Collectors.toList());

        log.info("Retrieved {} attendance records (page {}/{})",
                content.size(), attendancePage.getNumber() + 1, attendancePage.getTotalPages());

        return new CustomPaginationResponseDto<>(
                content,
                attendancePage.getNumber() + 1,
                attendancePage.getSize(),
                attendancePage.getTotalElements(),
                attendancePage.getTotalPages(),
                attendancePage.isLast()
        );
    }
}