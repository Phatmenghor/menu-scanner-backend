package com.emenu.features.order.models;

import com.emenu.shared.domain.BaseUUIDEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "order_status_history")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusHistory extends BaseUUIDEntity {

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", insertable = false, updatable = false)
    private Order order;

    @Column(name = "order_process_status_id", nullable = false)
    private UUID orderProcessStatusId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_process_status_id", insertable = false, updatable = false)
    private OrderProcessStatus orderProcessStatus;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @Column(name = "changed_by")
    private String changedBy; // User who changed the status

    public OrderStatusHistory(UUID orderId, UUID orderProcessStatusId, String note, String changedBy) {
        this.orderId = orderId;
        this.orderProcessStatusId = orderProcessStatusId;
        this.note = note;
        this.changedBy = changedBy;
    }
}