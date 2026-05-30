package com.hospitality.operations.domain.payroll;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

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

@WebMvcTest(PayrollEmployeeController.class)
@Import(SecurityConfig.class)
class PayrollEmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PayrollEmployeeService payrollEmployeeService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private ApiUsageLogService apiUsageLogService;

    @Test
    @WithMockUser(roles = "OWNER")
    void testGetAllAsOwner() throws Exception {
        when(payrollEmployeeService.findAll()).thenReturn(List.of(
                PayrollEmployee.builder().id(1L).name("John").position("Chef")
                        .department("KITCHEN").salary(new BigDecimal("50000")).build()
        ));

        mockMvc.perform(get("/api/v1/admin/payroll")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("John"))
                .andExpect(jsonPath("$[0].salary").value(50000));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAllAsAdminForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/admin/payroll")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void testGetAllAsUserForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/admin/payroll")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "OWNER")
    void testGetById() throws Exception {
        when(payrollEmployeeService.findById(1L)).thenReturn(
                PayrollEmployee.builder().id(1L).name("Jane").position("Manager")
                        .department("MANAGEMENT").salary(new BigDecimal("80000")).build()
        );

        mockMvc.perform(get("/api/v1/admin/payroll/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Jane"));
    }

    @Test
    @WithMockUser(roles = "OWNER")
    void testCreate() throws Exception {
        when(payrollEmployeeService.create(any())).thenReturn(
                PayrollEmployee.builder().id(3L).name("New").position("Staff")
                        .department("SERVICE").salary(new BigDecimal("30000")).build()
        );

        mockMvc.perform(post("/api/v1/admin/payroll")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"New\",\"position\":\"Staff\",\"department\":\"SERVICE\",\"salary\":30000}")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3));
    }

    @Test
    @WithMockUser(roles = "OWNER")
    void testDelete() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/payroll/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "OWNER")
    void testExportCsv() throws Exception {
        when(payrollEmployeeService.exportCsv()).thenReturn("ID,Name\n1,John\n");

        mockMvc.perform(get("/api/v1/admin/payroll/export/csv")
                        .accept(MediaType.TEXT_PLAIN))
                .andExpect(status().isOk());
    }
}
