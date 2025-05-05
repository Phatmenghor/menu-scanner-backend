package com.menghor.ksit.feature.master.model;

import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.utils.database.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "rooms")
public class RoomEntity extends BaseEntity {

    private String name;

    @Enumerated(EnumType.STRING)
    private Status status;
}
