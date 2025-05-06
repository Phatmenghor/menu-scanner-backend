package com.menghor.ksit.feature.auth.models;

import com.menghor.ksit.utils.database.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;


// ភាសាបរទេស
@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "teacher_language")
public class TeacherLanguageEntity extends BaseEntity {
    private String language; // ផ្នែភាសា
    private String reading; // ការអាន
    private String writing; // ការសរសេរ
    private String speaking; // ការសន្ទនា

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;
}


