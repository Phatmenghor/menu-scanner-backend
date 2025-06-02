package com.menghor.ksit.feature.setting.service.impl;

import com.menghor.ksit.enumations.RoleEnum;
import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.feature.auth.repository.UserRepository;
import com.menghor.ksit.feature.master.repository.ClassRepository;
import com.menghor.ksit.feature.master.repository.DepartmentRepository;
import com.menghor.ksit.feature.master.repository.MajorRepository;
import com.menghor.ksit.feature.master.repository.RoomRepository;
import com.menghor.ksit.feature.school.repository.CourseRepository;
import com.menghor.ksit.feature.setting.dto.response.StatisticsResponseDto;
import com.menghor.ksit.feature.setting.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatisticsServiceImpl implements StatisticsService {

    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final ClassRepository classRepository;
    private final MajorRepository majorRepository;
    private final DepartmentRepository departmentRepository;

    @Override
    public StatisticsResponseDto getOverallStatistics() {
        log.info("Fetching overall system statistics for active entities");

        try {
            // Count active rooms
            long totalRooms = roomRepository.countByStatus(Status.ACTIVE);
            
            // Count active students (users with STUDENT role and ACTIVE status)
            long totalStudents = userRepository.countActiveUsersByRole(RoleEnum.STUDENT, Status.ACTIVE);
            
            // Count active teachers (users with TEACHER role and ACTIVE status)
            long totalTeachers = userRepository.countActiveUsersByRole(RoleEnum.TEACHER, Status.ACTIVE);
            
            // Count active courses
            long totalCourses = courseRepository.countByStatus(Status.ACTIVE);
            
            // Count active classes
            long totalClasses = classRepository.countByStatus(Status.ACTIVE);
            
            // Count active majors
            long totalMajors = majorRepository.countByStatus(Status.ACTIVE);
            
            // Count active departments
            long totalDepartments = departmentRepository.countByStatus(Status.ACTIVE);

            StatisticsResponseDto statistics = StatisticsResponseDto.builder()
                    .totalRooms(totalRooms)
                    .totalStudents(totalStudents)
                    .totalTeachers(totalTeachers)
                    .totalCourses(totalCourses)
                    .totalClasses(totalClasses)
                    .totalMajors(totalMajors)
                    .totalDepartments(totalDepartments)
                    .build();

            log.info("Statistics fetched successfully: Rooms={}, Students={}, Teachers={}, Courses={}, Classes={}, Majors={}, Departments={}",
                    totalRooms, totalStudents, totalTeachers, totalCourses, totalClasses, totalMajors, totalDepartments);

            return statistics;

        } catch (Exception e) {
            log.error("Error fetching system statistics", e);
            throw new RuntimeException("Failed to fetch system statistics: " + e.getMessage());
        }
    }
}