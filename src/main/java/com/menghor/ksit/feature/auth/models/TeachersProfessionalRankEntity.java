package com.menghor.ksit.feature.auth.models;

import com.menghor.ksit.utils.database.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

// ឋានៈវិជ្ជាជីវៈគ្រូបង្រៀន
@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "teachers_professional_rank")
public class TeachersProfessionalRankEntity extends BaseEntity {
    private String typeOfProfessionalRank; // ប្រភេទឋានៈវិជ្ជាជីវៈ
    private String description; // បរិយាយ
    private String announcementNumber; // ប្រកាសលេខ
    private LocalDate dateAccepted; // កាលបរិច្ឆេទទទួល

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;
}
