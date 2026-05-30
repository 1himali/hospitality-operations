package com.hospitality.operations.dashboard.metrics;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "api_usage_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiUsageLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "endpoint", nullable = false, length = 200)
    private String endpoint;

    @Column(name = "http_method", nullable = false, length = 10)
    private String httpMethod;

    @Column(name = "status_code")
    private Integer statusCode;

    @Column(name = "duration_ms")
    private Long durationMs;

    @Column(name = "tenant_schema", length = 100)
    private String tenantSchema;

    @Column(name = "called_at", nullable = false, updatable = false)
    private Instant calledAt;

    @Column(name = "username", length = 100)
    private String username;
}
