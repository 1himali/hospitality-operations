package com.hospitality.operations.domain.payroll;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

@WebMvcTest(PayrollRecordController.class)
@Import(SecurityConfig.class)
class PayrollRecordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PayrollRecordService payrollRecordService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private ApiUsageLogService apiUsageLogService;

    @Test
    @WithMockUser(roles = "OWNER")
    void testGetRecordsByEmployee() throws Exception {
        when(payrollRecordService.getRecordsByEmployee(1L)).thenReturn(List.of(
                PayrollRecord.builder().id(10L).employeeId(1L)
                        .baseSalary(new BigDecimal("50000")).bonus(BigDecimal.ZERO)
                        .deductions(BigDecimal.ZERO).netPay(new BigDecimal("50000"))
                        .paymentDate(Instant.now()).status("PAID").build()
        ));

        mockMvc.perform(get("/api/v1/admin/payroll/records/employee/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].netPay").value(50000));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetRecordsAsAdminForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/admin/payroll/records/employee/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void testGetRecordsAsUserForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/admin/payroll/records/employee/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "OWNER")
    void testGetRecordById() throws Exception {
        when(payrollRecordService.findById(1L)).thenReturn(
                PayrollRecord.builder().id(1L).employeeId(1L)
                        .baseSalary(new BigDecimal("50000")).bonus(new BigDecimal("5000"))
                        .deductions(new BigDecimal("1000")).netPay(new BigDecimal("54000"))
                        .paymentDate(Instant.now()).status("PAID").build()
        );

        mockMvc.perform(get("/api/v1/admin/payroll/records/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.netPay").value(54000));
    }

    @Test
    @WithMockUser(roles = "OWNER")
    void testCreateRecord() throws Exception {
        when(payrollRecordService.create(eq(1L), any())).thenReturn(
                PayrollRecord.builder().id(20L).employeeId(1L)
                        .baseSalary(new BigDecimal("50000")).bonus(BigDecimal.ZERO)
                        .deductions(BigDecimal.ZERO).netPay(new BigDecimal("50000"))
                        .paymentDate(Instant.parse("2026-06-01T00:00:00Z")).status("PAID").build()
        );

        mockMvc.perform(post("/api/v1/admin/payroll/records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"employeeId\":1,\"bonus\":0,\"deductions\":0,\"paymentDate\":\"2026-06-01T00:00:00Z\",\"status\":\"PAID\"}")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeId").value(1));
    }

    @Test
    @WithMockUser(roles = "OWNER")
    void testUpdateRecord() throws Exception {
        when(payrollRecordService.update(eq(1L), any())).thenReturn(
                PayrollRecord.builder().id(1L).employeeId(1L)
                        .baseSalary(new BigDecimal("50000")).bonus(new BigDecimal("3000"))
                        .deductions(new BigDecimal("500")).netPay(new BigDecimal("52500"))
                        .paymentDate(Instant.now()).status("PAID").build()
        );

        mockMvc.perform(put("/api/v1/admin/payroll/records/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bonus\":3000,\"deductions\":500,\"status\":\"PAID\"}")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bonus").value(3000));
    }

    @Test
    @WithMockUser(roles = "OWNER")
    void testDeleteRecord() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/payroll/records/1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }
}
