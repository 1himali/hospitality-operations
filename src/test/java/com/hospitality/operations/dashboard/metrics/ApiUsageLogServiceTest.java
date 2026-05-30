package com.hospitality.operations.dashboard.metrics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ApiUsageLogServiceTest {

    @Mock
    private ApiUsageLogRepository repository;

    private ApiUsageLogService service;

    @BeforeEach
    void setUp() {
        service = new ApiUsageLogService(repository);
    }

    @Test
    void testLog() {
        service.log("/api/v1/rooms", "GET", 200, 42L, "default", "admin");

        ArgumentCaptor<ApiUsageLog> captor = ArgumentCaptor.forClass(ApiUsageLog.class);
        verify(repository).save(captor.capture());

        ApiUsageLog saved = captor.getValue();
        assertEquals("/api/v1/rooms", saved.getEndpoint());
        assertEquals("GET", saved.getHttpMethod());
        assertEquals(200, saved.getStatusCode());
        assertEquals(42L, saved.getDurationMs());
        assertEquals("default", saved.getTenantSchema());
        assertEquals("admin", saved.getUsername());
        assertNotNull(saved.getCalledAt());
    }

    @Test
    void testGetSummary() {
        when(repository.count()).thenReturn(100L);
        when(repository.countByStatusCodeGreaterThanEqual(500)).thenReturn(5L);
        when(repository.countByStatusCodeIsNotNull()).thenReturn(100L);

        Map<String, Object> summary = service.getSummary();

        assertEquals(100L, summary.get("totalRequests"));
        assertEquals(100L, summary.get("successRequests"));
        assertEquals(5L, summary.get("failedRequests"));
        assertEquals(0.05, (Double) summary.get("errorRate"), 0.001);
    }

    @Test
    void testGetSummaryEmpty() {
        when(repository.count()).thenReturn(0L);
        when(repository.countByStatusCodeGreaterThanEqual(500)).thenReturn(0L);
        when(repository.countByStatusCodeIsNotNull()).thenReturn(0L);

        Map<String, Object> summary = service.getSummary();

        assertEquals(0L, summary.get("totalRequests"));
        assertEquals(0.0, (Double) summary.get("errorRate"), 0.001);
    }

    @Test
    void testGetEndpointStats() {
        when(repository.findEndpointStats()).thenReturn(List.of(
                new Object[]{"/api/v1/rooms", 50L, 35.0, 2L},
                new Object[]{"/api/v1/bills", 30L, 120.0, 1L}
        ));

        List<Map<String, Object>> stats = service.getEndpointStats();

        assertEquals(2, stats.size());
        assertEquals("/api/v1/rooms", stats.get(0).get("endpoint"));
        assertEquals(50L, stats.get(0).get("count"));
        assertEquals(35L, stats.get(0).get("avgDurationMs"));
        assertEquals(2L, stats.get(0).get("failures"));
    }
}
