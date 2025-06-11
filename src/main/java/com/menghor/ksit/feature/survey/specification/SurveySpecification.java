package com.menghor.ksit.feature.survey.specification;

import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.enumations.StatusSurvey;
import com.menghor.ksit.feature.survey.model.SurveyEntity;
import org.springframework.data.jpa.domain.Specification;

public class SurveySpecification {

    public static Specification<SurveyEntity> isActive() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("status"), StatusSurvey.ACTIVE);
    }

    public static Specification<SurveyEntity> isDeleted() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("status"), StatusSurvey.DELETED);
    }

    public static Specification<SurveyEntity> hasId(Long id) {
        return (root, query, criteriaBuilder) ->
                id != null ? criteriaBuilder.equal(root.get("id"), id) : null;
    }
}
