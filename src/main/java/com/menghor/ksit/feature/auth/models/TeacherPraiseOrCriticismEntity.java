package com.menghor.ksit.feature.auth.models;

import com.menghor.ksit.utils.database.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

// ការសរសើរ/ ស្តីបន្ទោស
@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "teachers_praise_or_criticism")
public class TeacherPraiseOrCriticismEntity extends BaseEntity {
    private String typePraiseOrCriticism; // ប្រភេទនៃការសរសើរ/ការស្តីបន្ទោស/ទទួលអធិការកិច្ច
    private String giveBy; // ផ្តល់ដោយ
    private LocalDate dateAccepted; // កាលបរិច្ឆេទទទួល

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;
}