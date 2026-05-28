package com.hospitality.operations.domain.room;

import java.math.BigDecimal;
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
@Table(name = "rooms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_number", nullable = false, unique = true, length = 10)
    private String roomNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private RoomType type;

    @Column(name = "floor", nullable = false)
    private Integer floor;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private RoomStatus status = RoomStatus.VACANT;

    @Column(name = "rate_per_night", nullable = false, precision = 10, scale = 2)
    private BigDecimal ratePerNight;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "issue_notes", length = 500)
    private String issueNotes;

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
