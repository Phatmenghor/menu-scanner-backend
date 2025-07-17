package com.emenu.feature.auth.models;


import com.emenu.enumations.RoleEnum;
import com.emenu.utils.database.BaseLongEntity;
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
