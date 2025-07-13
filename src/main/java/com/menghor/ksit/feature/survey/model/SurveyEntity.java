package com.menghor.ksit.feature.survey.model;

import com.menghor.ksit.enumations.StatusSurvey;
import com.menghor.ksit.feature.school.model.ScheduleEntity;
import com.menghor.ksit.utils.database.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "surveys")
public class SurveyEntity extends BaseEntity {

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "text") // Fixed: lowercase 'text'
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusSurvey status = StatusSurvey.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id")
    private ScheduleEntity schedule;

    // All sections including deleted ones
    @OneToMany(mappedBy = "survey", cascade = CascadeType.ALL, orphanRemoval = false, fetch = FetchType.LAZY)
    @OrderBy("displayOrder ASC, id ASC")
    private List<SurveySectionEntity> sections = new ArrayList<>();

    @OneToMany(mappedBy = "survey", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SurveyResponseEntity> responses = new ArrayList<>();

    // Helper method to get only active sections
    public List<SurveySectionEntity> getActiveSections() {
        return sections.stream()
                .filter(section -> section.getStatus() == StatusSurvey.ACTIVE)
                .toList();
    }

    // Helper method to get all sections (including deleted)
    public List<SurveySectionEntity> getAllSections() {
        return sections;
    }
}