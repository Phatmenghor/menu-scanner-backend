package com.menghor.ksit.feature.survey.service.impl;

import com.menghor.ksit.enumations.QuestionTypeEnum;
import com.menghor.ksit.enumations.StatusSurvey;
import com.menghor.ksit.exceptoins.error.NotFoundException;
import com.menghor.ksit.feature.auth.models.UserEntity;
import com.menghor.ksit.feature.school.model.ScheduleEntity;
import com.menghor.ksit.feature.survey.dto.filter.SurveyReportFilterDto;
import com.menghor.ksit.feature.survey.dto.filter.SurveyReportHeaderFilterDto;
import com.menghor.ksit.feature.survey.dto.response.SurveyReportHeaderDto;
import com.menghor.ksit.feature.survey.dto.response.SurveyReportRowDto;
import com.menghor.ksit.feature.survey.model.*;
import com.menghor.ksit.feature.survey.repository.SurveyRepository;
import com.menghor.ksit.feature.survey.repository.SurveyResponseRepository;
import com.menghor.ksit.feature.survey.service.SurveyReportService;
import com.menghor.ksit.feature.survey.specification.SurveyResponseSpecification;
import com.menghor.ksit.feature.survey.specification.SurveySpecification;
import com.menghor.ksit.utils.database.CustomPaginationResponseDto;
import com.menghor.ksit.utils.pagiantion.PaginationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SurveyReportServiceImpl implements SurveyReportService {

    private final SurveyResponseRepository surveyResponseRepository;
    private final SurveyRepository surveyRepository;

    // ===== ORIGINAL REPORT METHODS (ALL QUESTIONS INCLUDING DELETED) =====

    @Override
    public CustomPaginationResponseDto<SurveyReportRowDto> getSurveyReportWithPagination(SurveyReportFilterDto filterDto) {
        log.info("Generating survey report with pagination - ALL QUESTIONS, filters: {}", filterDto);

        // Validate and prepare pagination
        Pageable pageable = PaginationUtils.createPageable(
                filterDto.getPageNo(),
                filterDto.getPageSize(),
                "submittedAt",
                "DESC"
        );

        // Use specification for regular reports (includes all questions)
        Specification<SurveyResponseEntity> spec = SurveyResponseSpecification.buildRegularReportSpecification(filterDto);

        // Get paginated responses
        Page<SurveyResponseEntity> responsePage = surveyResponseRepository.findAll(spec, pageable);

        // Build report rows with all questions
        List<SurveyReportRowDto> reportRows = buildReportRowsAllQuestions(responsePage.getContent());

        // Create pagination response
        CustomPaginationResponseDto<SurveyReportRowDto> response = new CustomPaginationResponseDto<>();
        response.setContent(reportRows);
        response.setPageNo(responsePage.getNumber() + 1);
        response.setPageSize(responsePage.getSize());
        response.setTotalElements(responsePage.getTotalElements());
        response.setTotalPages(responsePage.getTotalPages());
        response.setLast(responsePage.isLast());

        log.info("Survey report with pagination generated successfully. Total: {}", response.getTotalElements());
        return response;
    }

    @Override
    public List<SurveyReportRowDto> getSurveyReportForExport(SurveyReportFilterDto filterDto) {
        log.info("Generating survey report for export - ALL QUESTIONS, filters: {}", filterDto);

        // Use specification for regular reports (includes all questions)
        Specification<SurveyResponseEntity> spec = SurveyResponseSpecification.buildRegularReportSpecification(filterDto);

        // Get all responses (no pagination for export)
        List<SurveyResponseEntity> responses = surveyResponseRepository.findAll(spec);

        // Build report rows with all questions
        List<SurveyReportRowDto> reportRows = buildReportRowsAllQuestions(responses);

        log.info("Survey report for export generated successfully. Total rows: {}", reportRows.size());
        return reportRows;
    }

    @Override
    public List<SurveyReportHeaderDto> getSurveyReportHeaders() {
        log.info("Generating survey report headers for ALL QUESTIONS");

        List<SurveyReportHeaderDto> headers = new ArrayList<>();

        // Add standard columns
        addStandardHeaders(headers);

        // Add ALL question headers (including deleted ones)
        addAllQuestionHeaders(headers);

        log.info("Survey report headers generated successfully. Total headers: {}", headers.size());
        return headers;
    }

    @Override
    public List<SurveyReportHeaderDto> getFilteredSurveyReportHeaders(SurveyReportHeaderFilterDto filterDto) {
        log.info("Generating FILTERED survey report headers - ALL QUESTIONS with filter: {}", filterDto);

        // Get all headers first
        List<SurveyReportHeaderDto> allHeaders = getSurveyReportHeaders();

        // Apply SIMPLIFIED filtering logic - only hiddenHeaders
        List<SurveyReportHeaderDto> filteredHeaders = applySimplifiedHeaderFilter(allHeaders, filterDto);

        log.info("Filtered headers count: {} from total: {}", filteredHeaders.size(), allHeaders.size());
        return filteredHeaders;
    }

    // ===== NEW ACTIVE-ONLY REPORT METHODS =====

    @Override
    public CustomPaginationResponseDto<SurveyReportRowDto> getSurveyReportWithPaginationActiveOnly(SurveyReportFilterDto filterDto) {
        log.info("Generating survey report with pagination - ACTIVE QUESTIONS ONLY, filters: {}", filterDto);

        // Validate and prepare pagination
        Pageable pageable = PaginationUtils.createPageable(
                filterDto.getPageNo(),
                filterDto.getPageSize(),
                "submittedAt",
                "DESC"
        );

        // Use specification for active-only reports (optimized query)
        Specification<SurveyResponseEntity> spec = SurveyResponseSpecification.buildActiveOnlyReportSpecification(filterDto);

        // Get paginated responses with active questions only
        Page<SurveyResponseEntity> responsePage = surveyResponseRepository.findAll(spec, pageable);

        // Build report rows (questions already filtered at query level)
        List<SurveyReportRowDto> reportRows = buildReportRowsActiveOnly(responsePage.getContent());

        // Create pagination response
        CustomPaginationResponseDto<SurveyReportRowDto> response = new CustomPaginationResponseDto<>();
        response.setContent(reportRows);
        response.setPageNo(responsePage.getNumber() + 1);
        response.setPageSize(responsePage.getSize());
        response.setTotalElements(responsePage.getTotalElements());
        response.setTotalPages(responsePage.getTotalPages());
        response.setLast(responsePage.isLast());

        log.info("Survey report with pagination (active only) generated successfully. Total: {}", response.getTotalElements());
        return response;
    }

    @Override
    public List<SurveyReportRowDto> getSurveyReportForExportActiveOnly(SurveyReportFilterDto filterDto) {
        log.info("Generating survey report for export - ACTIVE QUESTIONS ONLY, filters: {}", filterDto);

        // Use specification for active-only reports (optimized query)
        Specification<SurveyResponseEntity> spec = SurveyResponseSpecification.buildActiveOnlyReportSpecification(filterDto);

        // Get responses with active questions only (filtered at query level)
        List<SurveyResponseEntity> responses = surveyResponseRepository.findAll(spec);

        // Build report rows (questions already filtered at query level)
        List<SurveyReportRowDto> reportRows = buildReportRowsActiveOnly(responses);

        log.info("Survey report for export (active only) generated successfully. Total rows: {}", reportRows.size());
        return reportRows;
    }

    @Override
    public List<SurveyReportHeaderDto> getSurveyReportHeadersActiveOnly() {
        log.info("Generating survey report headers for ACTIVE QUESTIONS ONLY");

        List<SurveyReportHeaderDto> headers = new ArrayList<>();

        // Add standard columns (same as original)
        addStandardHeaders(headers);

        // Add ONLY active question headers
        addActiveQuestionHeaders(headers);

        log.info("Survey report headers (active only) generated successfully. Total headers: {}", headers.size());
        return headers;
    }

    @Override
    public List<SurveyReportHeaderDto> getFilteredSurveyReportHeadersActiveOnly(SurveyReportHeaderFilterDto filterDto) {
        log.info("Generating FILTERED survey report headers - ACTIVE QUESTIONS ONLY with filter: {}", filterDto);

        // Get active headers first
        List<SurveyReportHeaderDto> activeHeaders = getSurveyReportHeadersActiveOnly();

        // Apply SIMPLIFIED filtering logic - only hiddenHeaders
        List<SurveyReportHeaderDto> filteredHeaders = applySimplifiedHeaderFilter(activeHeaders, filterDto);

        log.info("Filtered active headers count: {} from total: {}", filteredHeaders.size(), activeHeaders.size());
        return filteredHeaders;
    }

    /**
     * SIMPLIFIED header filtering - only supports hiddenHeaders
     * Much cleaner and easier to use
     */
    private List<SurveyReportHeaderDto> applySimplifiedHeaderFilter(List<SurveyReportHeaderDto> allHeaders,
                                                                    SurveyReportHeaderFilterDto filterDto) {
        // If no filter provided, return all headers
        if (filterDto == null || filterDto.getHiddenHeaders() == null || filterDto.getHiddenHeaders().isEmpty()) {
            log.info("No hidden headers specified, returning all {} headers", allHeaders.size());
            return allHeaders;
        }

        // Convert hidden headers to Set for faster lookup
        Set<String> hiddenHeadersSet = new HashSet<>(filterDto.getHiddenHeaders());

        log.info("Filtering out {} hidden headers: {}", hiddenHeadersSet.size(), hiddenHeadersSet);

        // Filter out hidden headers
        List<SurveyReportHeaderDto> filteredHeaders = allHeaders.stream()
                .filter(header -> !hiddenHeadersSet.contains(header.getKey()))
                .collect(Collectors.toList());

        log.info("Filtered headers: {} hidden, {} remaining",
                hiddenHeadersSet.size(), filteredHeaders.size());

        return filteredHeaders;
    }

    // ===== HELPER METHODS FOR ALL QUESTIONS (ORIGINAL FUNCTIONALITY) =====

    private List<SurveyReportRowDto> buildReportRowsAllQuestions(List<SurveyResponseEntity> responses) {
        List<SurveyReportRowDto> reportRows = new ArrayList<>();

        // Get all questions (including deleted) for building dynamic columns
        List<SurveyQuestionEntity> allQuestions = getAllQuestionsFromMainSurvey();

        for (SurveyResponseEntity response : responses) {
            SurveyReportRowDto row = new SurveyReportRowDto();

            // Set standard fields
            setStandardFields(row, response);

            // Set dynamic answers for ALL questions
            setAllQuestionAnswers(row, response, allQuestions);

            reportRows.add(row);
        }

        return reportRows;
    }

    private void addAllQuestionHeaders(List<SurveyReportHeaderDto> headers) {
        // Get all questions from main survey (including deleted ones)
        List<SurveyQuestionEntity> allQuestions = getAllQuestionsFromMainSurvey();

        int displayOrder = 1000; // Start after standard headers

        for (SurveyQuestionEntity question : allQuestions) {
            SurveyReportHeaderDto header = new SurveyReportHeaderDto();
            header.setKey("Q" + question.getId() + "_Answer");
            header.setLabel(question.getQuestionText());
            header.setType(question.getQuestionType() == QuestionTypeEnum.RATING ? "RATING" : "TEXT");
            header.setCategory("ANSWER");
            header.setQuestionId(question.getId());
            header.setDisplayOrder(displayOrder++);

            headers.add(header);
        }
    }

    private void setAllQuestionAnswers(SurveyReportRowDto row, SurveyResponseEntity response, List<SurveyQuestionEntity> allQuestions) {
        // Create answer map for quick lookup
        Map<Long, SurveyAnswerEntity> answerMap = response.getAnswers().stream()
                .collect(Collectors.toMap(
                        answer -> answer.getQuestion().getId(),
                        answer -> answer,
                        (existing, replacement) -> existing
                ));

        // Set answers for ALL questions
        for (SurveyQuestionEntity question : allQuestions) {
            String questionKey = "Q" + question.getId() + "_Answer";
            SurveyAnswerEntity answer = answerMap.get(question.getId());

            if (answer != null) {
                Object answerValue;
                if (question.getQuestionType() == QuestionTypeEnum.RATING) {
                    answerValue = answer.getRatingAnswer();
                } else {
                    answerValue = answer.getTextAnswer();
                }
                row.addAnswer(questionKey, answerValue);
            } else {
                row.addAnswer(questionKey, null);
            }
        }
    }

    private List<SurveyQuestionEntity> getAllQuestionsFromMainSurvey() {
        SurveyEntity mainSurvey = getMainSurveyEntity();
        List<SurveyQuestionEntity> allQuestions = new ArrayList<>();

        for (SurveySectionEntity section : mainSurvey.getAllSections()) {
            allQuestions.addAll(section.getAllQuestions());
        }

        allQuestions.sort(Comparator.comparingInt((SurveyQuestionEntity q) -> q.getSection().getDisplayOrder() != null ? q.getSection().getDisplayOrder() : 0).thenComparingInt(q -> q.getDisplayOrder() != null ? q.getDisplayOrder() : 0));

        return allQuestions;
    }

    // ===== HELPER METHODS FOR ACTIVE QUESTIONS ONLY =====

    private List<SurveyReportRowDto> buildReportRowsActiveOnly(List<SurveyResponseEntity> responses) {
        List<SurveyReportRowDto> reportRows = new ArrayList<>();

        // Get active questions only for building dynamic columns (cached for performance)
        List<SurveyQuestionEntity> activeQuestions = getActiveQuestionsFromMainSurvey();
        Set<Long> activeQuestionIds = activeQuestions.stream()
                .map(SurveyQuestionEntity::getId)
                .collect(Collectors.toSet());

        for (SurveyResponseEntity response : responses) {
            SurveyReportRowDto row = new SurveyReportRowDto();

            // Set standard fields
            setStandardFields(row, response);

            // Set dynamic answers for ONLY active questions
            setActiveQuestionAnswers(row, response, activeQuestions, activeQuestionIds);

            reportRows.add(row);
        }

        return reportRows;
    }

    private void addActiveQuestionHeaders(List<SurveyReportHeaderDto> headers) {
        // Get active questions from main survey
        List<SurveyQuestionEntity> activeQuestions = getActiveQuestionsFromMainSurvey();

        int displayOrder = 1000; // Start after standard headers

        for (SurveyQuestionEntity question : activeQuestions) {
            SurveyReportHeaderDto header = new SurveyReportHeaderDto();
            header.setKey("Q" + question.getId() + "_Answer");
            header.setLabel(question.getQuestionText());
            header.setType(question.getQuestionType() == QuestionTypeEnum.RATING ? "RATING" : "TEXT");
            header.setCategory("ANSWER");
            header.setQuestionId(question.getId());
            header.setDisplayOrder(displayOrder++);

            headers.add(header);
        }
    }

    private void setActiveQuestionAnswers(SurveyReportRowDto row, SurveyResponseEntity response,
                                          List<SurveyQuestionEntity> activeQuestions, Set<Long> activeQuestionIds) {
        // Create answer map for quick lookup - ONLY for active questions
        Map<Long, SurveyAnswerEntity> answerMap = response.getAnswers().stream()
                .filter(answer -> activeQuestionIds.contains(answer.getQuestion().getId())) // Filter at stream level
                .collect(Collectors.toMap(
                        answer -> answer.getQuestion().getId(),
                        answer -> answer,
                        (existing, replacement) -> existing
                ));

        // Set answers for ONLY active questions
        for (SurveyQuestionEntity question : activeQuestions) {
            String questionKey = "Q" + question.getId() + "_Answer";
            SurveyAnswerEntity answer = answerMap.get(question.getId());

            if (answer != null) {
                Object answerValue;
                if (question.getQuestionType() == QuestionTypeEnum.RATING) {
                    answerValue = answer.getRatingAnswer();
                } else {
                    answerValue = answer.getTextAnswer();
                }
                row.addAnswer(questionKey, answerValue);
            } else {
                row.addAnswer(questionKey, null);
            }
        }
    }

    private List<SurveyQuestionEntity> getActiveQuestionsFromMainSurvey() {
        // Get main survey
        SurveyEntity mainSurvey = getMainSurveyEntity();

        // Collect all active questions from all active sections
        List<SurveyQuestionEntity> activeQuestions = new ArrayList<>();

        for (SurveySectionEntity section : mainSurvey.getActiveSections()) {
            activeQuestions.addAll(section.getActiveQuestions());
        }

        // Sort by section display order, then question display order
        activeQuestions.sort(Comparator.comparingInt((SurveyQuestionEntity q) -> q.getSection().getDisplayOrder() != null ? q.getSection().getDisplayOrder() : 0).thenComparingInt(q -> q.getDisplayOrder() != null ? q.getDisplayOrder() : 0));

        return activeQuestions;
    }

    // ===== SHARED HELPER METHODS =====

    private SurveyEntity getMainSurveyEntity() {
        // Use specification to find active survey
        Specification<SurveyEntity> spec = SurveySpecification.isActive();
        return surveyRepository.findOne(spec)
                .orElseThrow(() -> new NotFoundException("Main survey not found. Please contact administrator."));
    }

    private void setStandardFields(SurveyReportRowDto row, SurveyResponseEntity response) {
        // Response information
        row.setResponseId(response.getId());
        row.setSubmittedAt(response.getSubmittedAt());
        row.setOverallComment(response.getOverallComment());
        row.setCreatedAt(response.getCreatedAt());

        // Student information
        UserEntity student = response.getUser();
        if (student != null) {
            row.setStudentId(student.getId());
            row.setIdentifyNumber(student.getIdentifyNumber());
            row.setStudentNameEnglish(getFormattedEnglishName(student));
            row.setStudentNameKhmer(getFormattedKhmerName(student));
            row.setStudentEmail(student.getEmail());
            row.setStudentPhone(student.getPhoneNumber());

            // Class and academic information
            if (student.getClasses() != null) {
                row.setClassName(student.getClasses().getCode());
                if (student.getClasses().getMajor() != null) {
                    row.setMajorName(student.getClasses().getMajor().getName());
                    if (student.getClasses().getMajor().getDepartment() != null) {
                        row.setDepartmentName(student.getClasses().getMajor().getDepartment().getName());
                    }
                }
            }
        }

        // Schedule/Course information
        ScheduleEntity schedule = response.getSchedule();
        if (schedule != null) {
            row.setScheduleId(schedule.getId());

            if (schedule.getCourse() != null) {
                row.setCourseCode(schedule.getCourse().getCode());
                row.setCourseName(schedule.getCourse().getNameEn());
            }

            if (schedule.getUser() != null) {
                row.setTeacherName(getFormattedEnglishName(schedule.getUser()));
            }

            if (schedule.getRoom() != null) {
                row.setRoomName(schedule.getRoom().getName());
            }

            if (schedule.getDay() != null) {
                row.setDayOfWeek(schedule.getDay().name());
            }

            if (schedule.getStartTime() != null && schedule.getEndTime() != null) {
                row.setTimeSlot(schedule.getStartTime() + " - " + schedule.getEndTime());
            }

            if (schedule.getSemester() != null) {
                row.setSemester(schedule.getSemester().getSemester().name());
                row.setAcademyYear(schedule.getSemester().getAcademyYear());
            }
        }

        // Survey information
        row.setSurveyTitle(response.getSurveyTitleSnapshot());
    }

    private void addStandardHeaders(List<SurveyReportHeaderDto> headers) {
        int order = 1;

        // Response info headers
        headers.add(createHeader("responseId", "Response ID", "NUMBER", "SURVEY", null, order++));
        headers.add(createHeader("submittedAt", "Submitted At", "DATE", "SURVEY", null, order++));

        // Student info headers
        headers.add(createHeader("studentId", "Student ID", "NUMBER", "STUDENT", null, order++));
        headers.add(createHeader("identifyNumber", "Identify Number", "TEXT", "STUDENT", null, order++));
        headers.add(createHeader("studentNameEnglish", "Student Name (English)", "TEXT", "STUDENT", null, order++));
        headers.add(createHeader("studentNameKhmer", "Student Name (Khmer)", "TEXT", "STUDENT", null, order++));
        headers.add(createHeader("studentEmail", "Student Email", "TEXT", "STUDENT", null, order++));
        headers.add(createHeader("studentPhone", "Student Phone", "TEXT", "STUDENT", null, order++));
        headers.add(createHeader("className", "Class", "TEXT", "STUDENT", null, order++));
        headers.add(createHeader("majorName", "Major", "TEXT", "STUDENT", null, order++));
        headers.add(createHeader("departmentName", "Department", "TEXT", "STUDENT", null, order++));

        // Course info headers
        headers.add(createHeader("scheduleId", "Schedule ID", "NUMBER", "COURSE", null, order++));
        headers.add(createHeader("courseCode", "Course Code", "TEXT", "COURSE", null, order++));
        headers.add(createHeader("courseName", "Course Name", "TEXT", "COURSE", null, order++));
        headers.add(createHeader("teacherName", "Teacher Name", "TEXT", "COURSE", null, order++));
        headers.add(createHeader("roomName", "Room", "TEXT", "COURSE", null, order++));
        headers.add(createHeader("dayOfWeek", "Day of Week", "TEXT", "COURSE", null, order++));
        headers.add(createHeader("timeSlot", "Time Slot", "TEXT", "COURSE", null, order++));
        headers.add(createHeader("semester", "Semester", "TEXT", "COURSE", null, order++));
        headers.add(createHeader("academyYear", "Academy Year", "NUMBER", "COURSE", null, order++));

        // Survey info headers
        headers.add(createHeader("surveyTitle", "Survey Title", "TEXT", "SURVEY", null, order++));
        headers.add(createHeader("overallComment", "Overall Comment", "TEXT", "SURVEY", null, order++));
        headers.add(createHeader("createdAt", "Created At", "DATE", "SURVEY", null, order++));
    }

    private SurveyReportHeaderDto createHeader(String key, String label, String type, String category, Long questionId, int order) {
        SurveyReportHeaderDto header = new SurveyReportHeaderDto();
        header.setKey(key);
        header.setLabel(label);
        header.setType(type);
        header.setCategory(category);
        header.setQuestionId(questionId);
        header.setDisplayOrder(order);
        return header;
    }

    private String getFormattedEnglishName(UserEntity user) {
        if (user.getEnglishFirstName() != null && user.getEnglishLastName() != null) {
            return user.getEnglishFirstName() + " " + user.getEnglishLastName();
        }
        if (user.getKhmerFirstName() != null && user.getKhmerLastName() != null) {
            return user.getKhmerFirstName() + " " + user.getKhmerLastName();
        }
        return user.getUsername();
    }

    private String getFormattedKhmerName(UserEntity user) {
        if (user.getKhmerFirstName() != null && user.getKhmerLastName() != null) {
            return user.getKhmerFirstName() + " " + user.getKhmerLastName();
        }
        return null;
    }
}