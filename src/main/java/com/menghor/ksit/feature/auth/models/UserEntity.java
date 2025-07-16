// Updated UserEntity.java with explicit column mappings to fix naming issues
package com.menghor.ksit.feature.auth.models;

import com.menghor.ksit.enumations.GenderEnum;
import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.utils.database.BaseLongEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
public class UserEntity extends BaseLongEntity {

    @Column(unique = true)
    private String username;

    private String password;

    @Column(name = "english_first_name")
    private String englishFirstName;

    @Column(name = "english_last_name")
    private String englishLastName;

    @Column(name = "profile_url")
    private String profileUrl;

    @Enumerated(EnumType.STRING)
    private GenderEnum gender;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "staff_id")
    private String staffId;

    @Column(name = "phone_number")
    private String phoneNumber;

    private String email;

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"))
    private List<Role> roles = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.ACTIVE;
}