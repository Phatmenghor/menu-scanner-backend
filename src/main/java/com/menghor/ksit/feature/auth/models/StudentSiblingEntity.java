package com.menghor.ksit.feature.auth.models;

import com.menghor.ksit.enumations.GenderEnum;
import com.menghor.ksit.enumations.ParentEnum;
import com.menghor.ksit.utils.database.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

// សមាជិកគ្រួសារ
@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "student_sibling")
public class StudentSiblingEntity extends BaseEntity {

    private String name; // ឈ្មោះឪពុកម្តាយ
    private GenderEnum gender; // ភេទ
    private LocalDate dateOfBirth; // ថ្ងៃខែឆ្នាំកំណើត
    private String occupation; // មុខរបរ
    private String phoneNumber; // លេខទូរស័ព្ទ

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;
}