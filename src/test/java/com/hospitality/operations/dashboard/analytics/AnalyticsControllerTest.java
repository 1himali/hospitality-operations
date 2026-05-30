package com.hospitality.operations.dashboard.analytics;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import com.hospitality.operations.dashboard.metrics.ApiUsageLogService;

@WebMvcTest(AnalyticsController.class)
@Import(SecurityConfig.class)
class AnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AnalyticsService analyticsService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private ApiUsageLogService apiUsageLogService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAnalyticsAsAdmin() throws Exception {
        when(analyticsService.getAnalytics()).thenReturn(Map.of(
                "revenue", Map.of("totalRevenue", 10000, "paidRevenue", 8000, "unpaidRevenue", 2000, "averageOrderValue", 500),
                "rooms", Map.of("totalRooms", 10, "occupiedRooms", 6, "vacantRooms", 3, "occupancyRate", 0.6),
                "tables", Map.of("totalTables", 8, "availableTables", 3, "occupiedTables", 4, "utilizationRate", 0.625),
                "inventory", Map.of("totalItems", 20, "inStockItems", 15, "lowStockItems", 3, "itemsBelowReorderLevel", 1)
        ));

        mockMvc.perform(get("/api/v1/admin/analytics")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.revenue.totalRevenue").value(10000))
                .andExpect(jsonPath("$.rooms.totalRooms").value(10))
                .andExpect(jsonPath("$.tables.totalTables").value(8))
                .andExpect(jsonPath("$.inventory.totalItems").value(20));
    }

    @Test
    @WithMockUser(roles = "OWNER")
    void testGetAnalyticsAsOwner() throws Exception {
        when(analyticsService.getAnalytics()).thenReturn(Map.of(
                "revenue", Map.of("totalRevenue", 5000, "paidRevenue", 5000, "unpaidRevenue", 0, "averageOrderValue", 250)
        ));

        mockMvc.perform(get("/api/v1/admin/analytics")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.revenue.totalRevenue").value(5000));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testGetAnalyticsForbiddenForUser() throws Exception {
        mockMvc.perform(get("/api/v1/admin/analytics")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void testGetAnalyticsUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/admin/analytics")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}
