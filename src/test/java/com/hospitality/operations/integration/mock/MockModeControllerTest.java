package com.hospitality.operations.integration.mock;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

@WebMvcTest(MockModeController.class)
@Import(SecurityConfig.class)
class MockModeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MockModeService mockModeService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private ApiUsageLogService apiUsageLogService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetStatusWhenDisabled() throws Exception {
        when(mockModeService.isMockMode()).thenReturn(false);

        mockMvc.perform(get("/api/v1/admin/mock-mode")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(false));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetStatusWhenEnabled() throws Exception {
        when(mockModeService.isMockMode()).thenReturn(true);

        mockMvc.perform(get("/api/v1/admin/mock-mode")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(true));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testEnableMockMode() throws Exception {
        when(mockModeService.enableMockMode()).thenReturn(true);
        when(mockModeService.isMockMode()).thenReturn(true);

        mockMvc.perform(post("/api/v1/admin/mock-mode")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"enabled\": true}")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(true));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDisableMockMode() throws Exception {
        when(mockModeService.disableMockMode()).thenReturn(false);
        when(mockModeService.isMockMode()).thenReturn(false);

        mockMvc.perform(post("/api/v1/admin/mock-mode")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"enabled\": false}")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(false));
    }

    @Test
    @WithMockUser(roles = "OWNER")
    void testGetStatusAsOwner() throws Exception {
        when(mockModeService.isMockMode()).thenReturn(true);

        mockMvc.perform(get("/api/v1/admin/mock-mode")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(true));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testGetStatusForbiddenForUser() throws Exception {
        mockMvc.perform(get("/api/v1/admin/mock-mode")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void testGetStatusUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/admin/mock-mode")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}
