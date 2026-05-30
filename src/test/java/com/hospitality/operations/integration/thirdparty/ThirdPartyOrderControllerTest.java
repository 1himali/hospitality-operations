package com.hospitality.operations.integration.thirdparty;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
import com.hospitality.operations.integration.thirdparty.dto.ThirdPartyListingDto;
import com.hospitality.operations.integration.thirdparty.dto.ThirdPartyOrderRequestDto;
import com.hospitality.operations.integration.thirdparty.dto.ThirdPartyOrderResponseDto;

@WebMvcTest(ThirdPartyOrderController.class)
@Import(SecurityConfig.class)
class ThirdPartyOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ThirdPartyOrderService thirdPartyOrderService;

    @MockBean
    private ApiUsageLogService apiUsageLogService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    @WithMockUser
    void testListListings() throws Exception {
        when(thirdPartyOrderService.listListings()).thenReturn(List.of(
                ThirdPartyListingDto.builder().id("f1").name("Pizza")
                        .description("Cheese pizza").price(new BigDecimal("12.99"))
                        .currency("USD").platform("DoorDash").build()
        ));

        mockMvc.perform(get("/api/v1/third-party/listings")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("f1"))
                .andExpect(jsonPath("$[0].name").value("Pizza"))
                .andExpect(jsonPath("$[0].price").value(12.99));
    }

    @Test
    @WithMockUser
    void testCreateOrder() throws Exception {
        when(thirdPartyOrderService.createOrder(any(ThirdPartyOrderRequestDto.class)))
                .thenReturn(ThirdPartyOrderResponseDto.builder()
                        .orderId("MOCK-1001")
                        .listingId("f1")
                        .listingName("Pizza")
                        .quantity(2)
                        .status("CONFIRMED")
                        .totalAmount(new BigDecimal("25.98"))
                        .currency("USD")
                        .platform("DoorDash")
                        .createdAt(LocalDateTime.of(2026, 5, 30, 12, 0))
                        .build());

        mockMvc.perform(post("/api/v1/third-party/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"listingId\":\"f1\",\"quantity\":2,\"customerName\":\"Test\"}")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value("MOCK-1001"))
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.totalAmount").value(25.98));
    }

    @Test
    @WithMockUser
    void testGetOrderStatusFound() throws Exception {
        when(thirdPartyOrderService.getOrderStatus("MOCK-1001"))
                .thenReturn(ThirdPartyOrderResponseDto.builder()
                        .orderId("MOCK-1001")
                        .listingId("f1")
                        .status("CONFIRMED")
                        .build());

        mockMvc.perform(get("/api/v1/third-party/orders/MOCK-1001")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value("MOCK-1001"));
    }

    @Test
    @WithMockUser
    void testGetOrderStatusNotFound() throws Exception {
        when(thirdPartyOrderService.getOrderStatus("MOCK-9999"))
                .thenReturn(null);

        mockMvc.perform(get("/api/v1/third-party/orders/MOCK-9999")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
