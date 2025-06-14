package com.menghor.ksit.feature.survey.specification;

import com.menghor.ksit.enumations.SemesterEnum;
import com.menghor.ksit.enumations.StatusSurvey;
import com.menghor.ksit.feature.survey.dto.filter.SurveyReportFilterDto;
import com.menghor.ksit.feature.survey.model.SurveyResponseEntity;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SurveyResponseSpecification {

    // ===== EXISTING SPECIFICATIONS =====

    public static Specification<SurveyResponseEntity> belongsToSchedule(Long scheduleId) {
        return (root, query, criteriaBuilder) ->
                scheduleId != null ? criteriaBuilder.equal(root.get("schedule").get("id"), scheduleId) : null;
    }

    public static Specification<SurveyResponseEntity> belongsToUser(Long userId) {
        return (root, query, criteriaBuilder) ->
                userId != null ? criteriaBuilder.equal(root.get("user").get("id"), userId) : null;
    }

    public static Specification<SurveyResponseEntity> belongsToSurvey(Long surveyId) {
        return (root, query, criteriaBuilder) ->
                surveyId != null ? criteriaBuilder.equal(root.get("survey").get("id"), surveyId) : null;
    }

    public static Specification<SurveyResponseEntity> isCompleted() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("isCompleted"), true);
    }

    public static Specification<SurveyResponseEntity> isActive() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("status"), StatusSurvey.ACTIVE);
    }

    public static Specification<SurveyResponseEntity> orderBySubmittedAtDesc() {
        return (root, query, criteriaBuilder) -> {
            query.orderBy(criteriaBuilder.desc(root.get("submittedAt")));
            return criteriaBuilder.conjunction();
        };
    }

    // ===== NEW SPECIFICATIONS FOR ACTIVE QUESTIONS FILTERING =====

    /**
     * Filter responses that have answers to ACTIVE questions only
     * This is used for optimized active-only reports
     */
    public static Specification<SurveyResponseEntity> hasAnswersToActiveQuestionsOnly() {
        return (root, query, criteriaBuilder) -> {
            // Join with answers and questions
            var answersJoin = root.join("answers", JoinType.INNER);
            var questionJoin = answersJoin.join("question", JoinType.INNER);

            // Only include answers to active questions
            var activeQuestionCondition = criteriaBuilder.equal(questionJoin.get("status"), StatusSurvey.ACTIVE);

            // Ensure we get distinct responses (avoid duplicates from multiple answers)
            query.distinct(true);

            return activeQuestionCondition;
        };
    }

    /**
     * Comprehensive specification builder for survey report filtering
     * This builds the base filters without the active questions constraint
     */
    public static Specification<SurveyResponseEntity> buildReportFilters(SurveyReportFilterDto filterDto) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Search filter - student name, ID, or course name
            if (filterDto.getSearch() != null && !filterDto.getSearch().trim().isEmpty()) {
                String searchTerm = "%" + filterDto.getSearch().toLowerCase() + "%";
                Predicate searchPredicate = criteriaBuilder.or(
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("user").get("englishFirstName")),
                                searchTerm
                        ),
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("user").get("englishLastName")),
                                searchTerm
                        ),
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("user").get("khmerFirstName")),
                                searchTerm
                        ),
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("user").get("khmerLastName")),
                                searchTerm
                        ),
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("user").get("identifyNumber")),
                                searchTerm
                        ),
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("schedule").get("course").get("nameEn")),
                                searchTerm
                        ),
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("schedule").get("course").get("code")),
                                searchTerm
                        )
                );
                predicates.add(searchPredicate);
            }

            // Filter by specific user (student)
            if (filterDto.getUserId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("user").get("id"), filterDto.getUserId()));
            }

            // Filter by specific schedule/course
            if (filterDto.getScheduleId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("schedule").get("id"), filterDto.getScheduleId()));
            }

            // Filter by course
            if (filterDto.getCourseId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("schedule").get("course").get("id"), filterDto.getCourseId()));
            }

            // Filter by class
            if (filterDto.getClassId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("schedule").get("classes").get("id"), filterDto.getClassId()));
            }

            // Filter by teacher
            if (filterDto.getTeacherId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("schedule").get("user").get("id"), filterDto.getTeacherId()));
            }

            // Filter by department
            if (filterDto.getDepartmentId() != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("schedule").get("course").get("department").get("id"),
                        filterDto.getDepartmentId()
                ));
            }

            // Filter by major
            if (filterDto.getMajorId() != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("schedule").get("classes").get("major").get("id"),
                        filterDto.getMajorId()
                ));
            }

            // Filter by semester
            if (filterDto.getSemester() != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("schedule").get("semester").get("semester"),
                        filterDto.getSemester()
                ));
            }

            // Filter by academy year
            if (filterDto.getAcademyYear() != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("schedule").get("semester").get("academyYear"),
                        filterDto.getAcademyYear()
                ));
            }

            // Filter by submission date range
            if (filterDto.getStartDate() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("submittedAt").as(LocalDate.class),
                        filterDto.getStartDate()
                ));
            }

            if (filterDto.getEndDate() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("submittedAt").as(LocalDate.class),
                        filterDto.getEndDate()
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Complete specification for active-only survey reports
     * Combines base filters + active questions filter + completion/status filters
     */
    public static Specification<SurveyResponseEntity> buildActiveOnlyReportSpecification(SurveyReportFilterDto filterDto) {
        return Specification.where(buildReportFilters(filterDto))
                .and(isCompleted())
                .and(isActive())
                .and(hasAnswersToActiveQuestionsOnly());
    }

    /**
     * Complete specification for regular survey reports (includes all questions)
     * Combines base filters + completion/status filters (without active questions constraint)
     */
    public static Specification<SurveyResponseEntity> buildRegularReportSpecification(SurveyReportFilterDto filterDto) {
        return Specification.where(buildReportFilters(filterDto))
                .and(isCompleted())
                .and(isActive());
    }
}