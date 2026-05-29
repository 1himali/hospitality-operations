package com.hospitality.operations.domain.restaurant.table;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "dining_tables")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiningTable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "table_number", nullable = false, unique = true, length = 10)
    private String tableNumber;

    @Column(name = "capacity", nullable = false)
    private Integer capacity;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private TableStatus status = TableStatus.AVAILABLE;

    @Enumerated(EnumType.STRING)
    @Column(name = "table_type", nullable = false, length = 30)
    @Builder.Default
    private TableType tableType = TableType.DINING;

    @Column(name = "location", length = 100)
    private String location;

    @Column(name = "seated_at")
    private Instant seatedAt;

    @Column(name = "current_order_id")
    private Long currentOrderId;

    @Column(name = "reservation_name", length = 100)
    private String reservationName;

    @Column(name = "party_size")
    private Integer partySize;

    @Column(name = "eta_minutes")
    private Integer etaMinutes;

    @Column(name = "tenant_schema", nullable = false, length = 100)
    @Builder.Default
    private String tenantSchema = "default";

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
