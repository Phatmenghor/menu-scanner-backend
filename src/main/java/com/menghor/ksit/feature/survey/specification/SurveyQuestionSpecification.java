package com.menghor.ksit.feature.survey.specification;

import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.enumations.StatusSurvey;
import com.menghor.ksit.feature.survey.model.SurveyQuestionEntity;
import org.springframework.data.jpa.domain.Specification;

public class SurveyQuestionSpecification {

    public static Specification<SurveyQuestionEntity> isActive() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("status"), StatusSurvey.ACTIVE);
    }

    public static Specification<SurveyQuestionEntity> isDeleted() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("status"), StatusSurvey.DELETED);
    }

    public static Specification<SurveyQuestionEntity> belongsToSection(Long sectionId) {
        return (root, query, criteriaBuilder) ->
                sectionId != null ? criteriaBuilder.equal(root.get("section").get("id"), sectionId) : null;
    }

    public static Specification<SurveyQuestionEntity> hasId(Long id) {
        return (root, query, criteriaBuilder) ->
                id != null ? criteriaBuilder.equal(root.get("id"), id) : null;
    }

    public static Specification<SurveyQuestionEntity> orderByDisplayOrder() {
        return (root, query, criteriaBuilder) -> {
            query.orderBy(criteriaBuilder.asc(root.get("displayOrder")), criteriaBuilder.asc(root.get("id")));
            return criteriaBuilder.conjunction();
        };
    }
}
