package com.menghor.ksit.feature.survey.specification;

import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.feature.survey.model.SurveySectionEntity;
import org.springframework.data.jpa.domain.Specification;

public class SurveySectionSpecification {

    public static Specification<SurveySectionEntity> isActive() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("status"), Status.ACTIVE);
    }

    public static Specification<SurveySectionEntity> belongsToSurvey(Long surveyId) {
        return (root, query, criteriaBuilder) ->
                surveyId != null ? criteriaBuilder.equal(root.get("survey").get("id"), surveyId) : null;
    }

    public static Specification<SurveySectionEntity> hasId(Long id) {
        return (root, query, criteriaBuilder) ->
                id != null ? criteriaBuilder.equal(root.get("id"), id) : null;
    }

    public static Specification<SurveySectionEntity> orderByDisplayOrder() {
        return (root, query, criteriaBuilder) -> {
            query.orderBy(criteriaBuilder.asc(root.get("displayOrder")), criteriaBuilder.asc(root.get("id")));
            return criteriaBuilder.conjunction();
        };
    }
}
