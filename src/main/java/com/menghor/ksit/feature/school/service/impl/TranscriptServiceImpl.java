package com.menghor.ksit.feature.school.service.impl;

import com.menghor.ksit.enumations.*;
import com.menghor.ksit.exceptoins.error.BadRequestException;
import com.menghor.ksit.exceptoins.error.NotFoundException;
import com.menghor.ksit.feature.attendance.models.ScoreSessionEntity;
import com.menghor.ksit.feature.attendance.models.StudentScoreEntity;
import com.menghor.ksit.feature.attendance.repository.ScoreSessionRepository;
import com.menghor.ksit.feature.attendance.repository.StudentScoreRepository;
import com.menghor.ksit.feature.auth.models.UserEntity;
import com.menghor.ksit.feature.auth.repository.UserRepository;
import com.menghor.ksit.feature.school.dto.response.TranscriptCourseDto;
import com.menghor.ksit.feature.school.dto.response.TranscriptResponseDto;
import com.menghor.ksit.feature.school.dto.response.TranscriptSemesterDto;
import com.menghor.ksit.feature.school.mapper.TranscriptMapper;
import com.menghor.ksit.feature.school.model.ScheduleEntity;
import com.menghor.ksit.feature.school.repository.ScheduleRepository;
import com.menghor.ksit.feature.school.service.TranscriptService;
import com.menghor.ksit.utils.database.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TranscriptServiceImpl implements TranscriptService {

    private final ScheduleRepository scheduleRepository;
    private final StudentScoreRepository studentScoreRepository;
    private final ScoreSessionRepository scoreSessionRepository;
    private final UserRepository userRepository;
    private final TranscriptMapper transcriptMapper;
    private final SecurityUtils securityUtils;

    @Override
    public TranscriptResponseDto getMyCompleteTranscript() {
        UserEntity currentUser = securityUtils.getCurrentUser();
        return generateCompleteTranscript(currentUser);
    }

    @Override
    public TranscriptResponseDto getStudentCompleteTranscript(Long studentId) {
        UserEntity student = userRepository.findById(studentId)
                .orElseThrow(() -> new NotFoundException("Student not found with ID: " + studentId));

        return generateCompleteTranscript(student);
    }

    private TranscriptResponseDto generateCompleteTranscript(UserEntity student) {
        log.info("Generating complete transcript for student: {} (ID: {})", student.getUsername(), student.getId());

        // Validate student has a class
        if (student.getClasses() == null) {
            throw new BadRequestException("Student is not assigned to any class");
        }

        // Get ALL schedules for this student's class
        List<ScheduleEntity> allSchedules = getAllStudentSchedules(student);

        if (allSchedules.isEmpty()) {
            log.warn("No schedules found for student {}", student.getId());
            return createEmptyTranscript(student);
        }

        log.info("Found {} total schedules for student", allSchedules.size());

        // Group schedules by semester and year
        Map<String, List<ScheduleEntity>> groupedSchedules = groupSchedulesBySemester(allSchedules);

        // Build complete transcript response
        TranscriptResponseDto transcript = buildCompleteTranscript(student, groupedSchedules);

        log.info("Complete transcript generated for student {} with {} semesters",
                student.getId(), transcript.getSemesters().size());

        return transcript;
    }

    private List<ScheduleEntity> getAllStudentSchedules(UserEntity student) {
        // Get all schedules for student's class (no filters - everything)
        return scheduleRepository.findAll((root, query, criteriaBuilder) ->
                criteriaBuilder.and(
                        criteriaBuilder.equal(root.get("classes").get("id"), student.getClasses().getId()),
                        criteriaBuilder.equal(root.get("status"), Status.ACTIVE)
                ));
    }

    private Map<String, List<ScheduleEntity>> groupSchedulesBySemester(List<ScheduleEntity> schedules) {
        return schedules.stream()
                .filter(schedule -> schedule.getSemester() != null)
                .collect(Collectors.groupingBy(
                        schedule -> schedule.getSemester().getAcademyYear() + "_" +
                                schedule.getSemester().getSemester().name(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));
    }

    private TranscriptResponseDto buildCompleteTranscript(UserEntity student,
                                                          Map<String, List<ScheduleEntity>> groupedSchedules) {

        TranscriptResponseDto transcript = transcriptMapper.toTranscriptResponse(student);

        List<TranscriptSemesterDto> semesters = new ArrayList<>();

        // Running totals for cumulative calculation
        int totalCreditsStudied = 0;
        int totalCreditsEarned = 0;
        BigDecimal cumulativeGradePoints = BigDecimal.ZERO;

        // Sort semesters chronologically
        List<String> sortedKeys = groupedSchedules.keySet().stream()
                .sorted(this::compareSemesterKeys)
                .toList();

        for (String semesterKey : sortedKeys) {
            List<ScheduleEntity> semesterSchedules = groupedSchedules.get(semesterKey);

            TranscriptSemesterDto semesterDto = buildSemesterDto(student, semesterSchedules);

            // Update running totals
            totalCreditsStudied += semesterDto.getTotalCredits();

            // Only count earned credits for completed courses
            int semesterCreditsEarned = semesterDto.getCourses().stream()
                    .filter(course -> course.getStatus() == CourseStatusEnum.COMPLETED &&
                            isPassingGrade(course.getLetterGrade()))
                    .mapToInt(course -> course.getCredit() != null ? course.getCredit() : 0)
                    .sum();

            totalCreditsEarned += semesterCreditsEarned;

            // Calculate cumulative grade points for completed courses only
            BigDecimal semesterGradePoints = calculateSemesterGradePoints(
                    semesterDto.getCourses().stream()
                            .filter(course -> course.getStatus() == CourseStatusEnum.COMPLETED)
                            .collect(Collectors.toList())
            );
            cumulativeGradePoints = cumulativeGradePoints.add(semesterGradePoints);

            // Calculate cumulative GPA (GPAX)
            BigDecimal cumulativeGPA = totalCreditsStudied > 0 ?
                    cumulativeGradePoints.divide(BigDecimal.valueOf(totalCreditsStudied), 2, RoundingMode.HALF_UP) :
                    BigDecimal.ZERO;

            // Set GPAX
            semesterDto.setGpax(cumulativeGPA);

            semesters.add(semesterDto);
        }

        transcript.setSemesters(semesters);

        // Set overall totals
        transcript.setNumberOfCreditsStudied(totalCreditsStudied);
        transcript.setNumberOfCreditsTransferred(0); // Default to 0, can be configured later
        transcript.setTotalNumberOfCreditsEarned(totalCreditsEarned);
        transcript.setCumulativeGradePointAverage(totalCreditsStudied > 0 ?
                cumulativeGradePoints.divide(BigDecimal.valueOf(totalCreditsStudied), 2, RoundingMode.HALF_UP) :
                BigDecimal.ZERO);
        transcript.setAcademicStatus(determineAcademicStatus(transcript.getCumulativeGradePointAverage()));
        transcript.setGeneratedAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        return transcript;
    }

    private TranscriptSemesterDto buildSemesterDto(UserEntity student, List<ScheduleEntity> schedules) {
        TranscriptSemesterDto semesterDto = new TranscriptSemesterDto();

        // Set semester info from first schedule
        if (!schedules.isEmpty()) {
            ScheduleEntity firstSchedule = schedules.get(0);
            if (firstSchedule.getSemester() != null) {
                semesterDto.setAcademyYear(firstSchedule.getSemester().getAcademyYear());
                semesterDto.setSemester(firstSchedule.getSemester().getSemester());
                semesterDto.setSemesterName(firstSchedule.getSemester().getSemester().name() +
                        ", " + firstSchedule.getSemester().getAcademyYear());
            }

            // Set year level from schedule or derive from academy year
            if (firstSchedule.getYearLevel() != null) {
                semesterDto.setYearLevel(firstSchedule.getYearLevel());
            } else {
                // Derive year level from academy year if not set
                assert firstSchedule.getSemester() != null;
                semesterDto.setYearLevel(deriveYearLevel(firstSchedule.getSemester().getAcademyYear()));
            }
        }

        // Build course list
        List<TranscriptCourseDto> courses = schedules.stream()
                .map(schedule -> buildCourseDto(student, schedule))
                .sorted(Comparator.comparing(TranscriptCourseDto::getCourseCode))
                .collect(Collectors.toList());

        semesterDto.setCourses(courses);

        // Calculate semester totals
        int totalCredits = courses.stream()
                .mapToInt(course -> course.getCredit() != null ? course.getCredit() : 0)
                .sum();

        // Calculate GPA for completed courses only
        List<TranscriptCourseDto> completedCourses = courses.stream()
                .filter(course -> course.getStatus() == CourseStatusEnum.COMPLETED)
                .collect(Collectors.toList());

        BigDecimal semesterGradePoints = calculateSemesterGradePoints(completedCourses);
        int completedCredits = completedCourses.stream()
                .mapToInt(course -> course.getCredit() != null ? course.getCredit() : 0)
                .sum();

        BigDecimal semesterGPA = completedCredits > 0 ?
                semesterGradePoints.divide(BigDecimal.valueOf(completedCredits), 2, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;

        semesterDto.setTotalCredits(totalCredits);
        semesterDto.setGpa(semesterGPA);

        return semesterDto;
    }

    private TranscriptCourseDto buildCourseDto(UserEntity student, ScheduleEntity schedule) {
        TranscriptCourseDto courseDto = transcriptMapper.toCourseDto(schedule);

        // Find student's score for this schedule
        Optional<StudentScoreEntity> scoreOpt = findStudentScoreForSchedule(student.getId(), schedule.getId());

        if (scoreOpt.isPresent()) {
            StudentScoreEntity score = scoreOpt.get();
            transcriptMapper.mapScoreToTranscript(score, courseDto);

            // Calculate grade points for GPA
            BigDecimal gradePoints = calculateGradePoints(courseDto.getLetterGrade());
            courseDto.setGradePoints(gradePoints);
            courseDto.setStatus(CourseStatusEnum.COMPLETED);
        } else {
            // No score found - set defaults for in progress
            courseDto.setTotalScore(BigDecimal.ZERO);
            courseDto.setLetterGrade(null); // Show null instead of ---
            courseDto.setGradePoints(BigDecimal.ZERO);
            courseDto.setStatus(CourseStatusEnum.IN_PROGRESS);
            courseDto.setAttendanceScore(BigDecimal.ZERO);
            courseDto.setAssignmentScore(BigDecimal.ZERO);
            courseDto.setMidtermScore(BigDecimal.ZERO);
            courseDto.setFinalScore(BigDecimal.ZERO);
        }

        return courseDto;
    }

    private Optional<StudentScoreEntity> findStudentScoreForSchedule(Long studentId, Long scheduleId) {
        // Find approved score sessions for this schedule
        List<ScoreSessionEntity> scoreSessions = scoreSessionRepository.findAll(
                (root, query, criteriaBuilder) ->
                        criteriaBuilder.and(
                                criteriaBuilder.equal(root.get("schedule").get("id"), scheduleId),
                                criteriaBuilder.equal(root.get("status"), SubmissionStatus.APPROVED)
                        )
        );

        // Find student's score in any of these sessions
        for (ScoreSessionEntity scoreSession : scoreSessions) {
            Optional<StudentScoreEntity> score = studentScoreRepository.findAll(
                    (root, query, criteriaBuilder) ->
                            criteriaBuilder.and(
                                    criteriaBuilder.equal(root.get("scoreSession").get("id"), scoreSession.getId()),
                                    criteriaBuilder.equal(root.get("student").get("id"), studentId)
                            )
            ).stream().findFirst();

            if (score.isPresent()) {
                return score;
            }
        }

        return Optional.empty();
    }

    private YearLevelEnum deriveYearLevel(Integer academyYear) {
        // This is a simple derivation - you might want to implement more complex logic
        // based on your school's academic calendar
        int currentYear = LocalDateTime.now().getYear();
        int yearDifference = currentYear - academyYear;

        return switch (yearDifference) {
            case 0 -> YearLevelEnum.FIRST_YEAR;
            case 1 -> YearLevelEnum.SECOND_YEAR;
            case 2 -> YearLevelEnum.THIRD_YEAR;
            case 3 -> YearLevelEnum.FOURTH_YEAR;
            default -> YearLevelEnum.FIRST_YEAR; // Default
        };
    }

    private BigDecimal calculateGradePoints(String letterGrade) {
        if (letterGrade == null) return BigDecimal.ZERO;

        return switch (letterGrade.toUpperCase()) {
            case "A+", "A" -> BigDecimal.valueOf(4.0);
            case "A-" -> BigDecimal.valueOf(3.7);
            case "B+" -> BigDecimal.valueOf(3.3);
            case "B" -> BigDecimal.valueOf(3.0);
            case "B-" -> BigDecimal.valueOf(2.7);
            case "C+" -> BigDecimal.valueOf(2.3);
            case "C" -> BigDecimal.valueOf(2.0);
            case "C-" -> BigDecimal.valueOf(1.7);
            case "D+" -> BigDecimal.valueOf(1.3);
            case "D" -> BigDecimal.valueOf(1.0);
            case "D-" -> BigDecimal.valueOf(0.7);
            case "F" -> BigDecimal.ZERO;
            default -> BigDecimal.ZERO;
        };
    }

    private boolean isPassingGrade(String letterGrade) {
        if (letterGrade == null) return false;
        return !letterGrade.equals("F");
    }

    private BigDecimal calculateSemesterGradePoints(List<TranscriptCourseDto> courses) {
        return courses.stream()
                .filter(course -> course.getGradePoints() != null &&
                        course.getCredit() != null &&
                        course.getLetterGrade() != null)
                .map(course -> course.getGradePoints().multiply(BigDecimal.valueOf(course.getCredit())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String determineAcademicStatus(BigDecimal gpa) {
        if (gpa.compareTo(BigDecimal.valueOf(3.5)) >= 0) {
            return "Dean's List";
        } else if (gpa.compareTo(BigDecimal.valueOf(3.0)) >= 0) {
            return "Good Standing";
        } else if (gpa.compareTo(BigDecimal.valueOf(2.0)) >= 0) {
            return "Satisfactory";
        } else if (gpa.compareTo(BigDecimal.valueOf(1.0)) >= 0) {
            return "Academic Warning";
        } else {
            return "Academic Probation";
        }
    }

    private TranscriptResponseDto createEmptyTranscript(UserEntity student) {
        TranscriptResponseDto transcript = transcriptMapper.toTranscriptResponse(student);
        transcript.setSemesters(new ArrayList<>());
        transcript.setNumberOfCreditsStudied(0);
        transcript.setNumberOfCreditsTransferred(0);
        transcript.setTotalNumberOfCreditsEarned(0);
        transcript.setCumulativeGradePointAverage(BigDecimal.ZERO);
        transcript.setAcademicStatus("No Records");
        transcript.setGeneratedAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        return transcript;
    }

    private int compareSemesterKeys(String key1, String key2) {
        String[] parts1 = key1.split("_");
        String[] parts2 = key2.split("_");

        if (parts1.length != 2 || parts2.length != 2) {
            return key1.compareTo(key2);
        }

        try {
            int year1 = Integer.parseInt(parts1[0]);
            int year2 = Integer.parseInt(parts2[0]);

            if (year1 != year2) {
                return Integer.compare(year1, year2);
            }

            // Same year, compare semester (SEMESTER_1 before SEMESTER_2)
            return parts1[1].compareTo(parts2[1]);
        } catch (Exception e) {
            return key1.compareTo(key2);
        }
    }
}