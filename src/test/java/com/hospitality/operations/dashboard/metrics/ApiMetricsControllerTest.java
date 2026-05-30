package com.hospitality.operations.dashboard.metrics;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.hospitality.operations.auth.JwtTokenProvider;
import com.hospitality.operations.config.SecurityConfig;

@WebMvcTest(ApiMetricsController.class)
@Import(SecurityConfig.class)
class ApiMetricsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ApiUsageLogService apiUsageLogService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetSummary() throws Exception {
        when(apiUsageLogService.getSummary()).thenReturn(Map.of(
                "totalRequests", 100L,
                "successRequests", 95L,
                "failedRequests", 5L,
                "errorRate", 0.05
        ));

        mockMvc.perform(get("/api/v1/metrics/summary")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRequests").value(100))
                .andExpect(jsonPath("$.errorRate").value(0.05));
    }

    @Test
    @WithMockUser(roles = "OWNER")
    void testGetSummaryAsOwner() throws Exception {
        when(apiUsageLogService.getSummary()).thenReturn(Map.of(
                "totalRequests", 50L,
                "successRequests", 48L,
                "failedRequests", 2L,
                "errorRate", 0.04
        ));

        mockMvc.perform(get("/api/v1/metrics/summary")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRequests").value(50));
    }

    @Test
    void testGetSummaryUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/metrics/summary")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void testGetSummaryForbiddenForUser() throws Exception {
        mockMvc.perform(get("/api/v1/metrics/summary")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetEndpointStats() throws Exception {
        when(apiUsageLogService.getEndpointStats()).thenReturn(List.of(
                Map.of("endpoint", "/api/v1/rooms", "count", 50L, "avgDurationMs", 35L, "failures", 2L)
        ));

        mockMvc.perform(get("/api/v1/metrics/endpoints")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].endpoint").value("/api/v1/rooms"))
                .andExpect(jsonPath("$[0].count").value(50));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testExportCsv() throws Exception {
        when(apiUsageLogService.exportCsv()).thenReturn("ID,Endpoint\n1,/api/v1/rooms\n");

        mockMvc.perform(get("/api/v1/metrics/export/csv")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "OWNER")
    void testExportJson() throws Exception {
        when(apiUsageLogService.exportJson()).thenReturn("[{\"id\":1,\"endpoint\":\"/api/v1/rooms\"}]");

        mockMvc.perform(get("/api/v1/metrics/export/json")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void testExportForbiddenForUser() throws Exception {
        mockMvc.perform(get("/api/v1/metrics/export/csv")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}
