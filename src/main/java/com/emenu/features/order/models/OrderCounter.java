package com.emenu.features.order.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "order_counters", uniqueConstraints = {
    @UniqueConstraint(columnNames = "counter_date", name = "uk_order_counter_date")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderCounter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "counter_date", nullable = false)
    private LocalDate counterDate;

    @Column(name = "counter_value", nullable = false)
    private Long counterValue = 0L;
}
