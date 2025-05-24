package com.menghor.ksit.feature.auth.models;

import com.menghor.ksit.enumations.GenderEnum;
import com.menghor.ksit.enumations.Status;
import com.menghor.ksit.enumations.StudentTypePayment;
import com.menghor.ksit.utils.database.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "payment")
public class PaymentEntity extends BaseEntity {
    private String item;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private StudentTypePayment type;

    private String amount;

    private String percentage;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    private String commend;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;
}
