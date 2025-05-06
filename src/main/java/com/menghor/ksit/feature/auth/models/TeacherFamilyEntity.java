package com.menghor.ksit.feature.auth.models;

import com.menghor.ksit.enumations.GenderEnum;
import com.menghor.ksit.utils.database.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

// ភាសាបរទេស
@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "teacher_family")
public class TeacherFamilyEntity extends BaseEntity {
    private String nameChild; // ឈ្មោះកូន
    private GenderEnum gender; // ភេទ
    private LocalDate dateOfBirth; // ថ្ងៃខែឆ្នាំកំណើត
    private String working; // 	មុខរបរ

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;
}