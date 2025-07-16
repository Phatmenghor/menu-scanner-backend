package com.menghor.ksit.feature.auth.models;


import com.menghor.ksit.enumations.RoleEnum;
import com.menghor.ksit.utils.database.BaseLongEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "roles")
public class Role extends BaseLongEntity {

    @Enumerated(EnumType.STRING)
    private RoleEnum name;

    public Role() {
    }

    public Role(RoleEnum name) {
        this.name = name;
    }
}
