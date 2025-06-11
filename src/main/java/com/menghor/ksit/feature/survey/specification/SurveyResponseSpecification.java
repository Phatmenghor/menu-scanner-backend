package com.menghor.ksit.feature.survey.specification;

import com.menghor.ksit.enumations.StatusSurvey;
import com.menghor.ksit.feature.survey.model.SurveyResponseEntity;
import org.springframework.data.jpa.domain.Specification;

public class SurveyResponseSpecification {

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
}