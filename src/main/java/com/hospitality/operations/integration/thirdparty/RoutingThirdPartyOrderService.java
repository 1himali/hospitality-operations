package com.hospitality.operations.integration.thirdparty;

import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.hospitality.operations.integration.mock.MockModeService;
import com.hospitality.operations.integration.thirdparty.dto.ThirdPartyListingDto;
import com.hospitality.operations.integration.thirdparty.dto.ThirdPartyOrderRequestDto;
import com.hospitality.operations.integration.thirdparty.dto.ThirdPartyOrderResponseDto;

@Service
@Primary
public class RoutingThirdPartyOrderService implements ThirdPartyOrderService {

    private final MockModeService mockModeService;
    private final ThirdPartyOrderService prodService;
    private final ThirdPartyOrderService mockService;

    public RoutingThirdPartyOrderService(
            MockModeService mockModeService,
            @Qualifier("prodThirdPartyOrderService") ThirdPartyOrderService prodService,
            @Qualifier("mockThirdPartyOrderService") ThirdPartyOrderService mockService) {
        this.mockModeService = mockModeService;
        this.prodService = prodService;
        this.mockService = mockService;
    }

    private ThirdPartyOrderService delegate() {
        return mockModeService.isMockMode() ? mockService : prodService;
    }

    @Override
    public List<ThirdPartyListingDto> listListings() {
        return delegate().listListings();
    }

    @Override
    public ThirdPartyOrderResponseDto createOrder(ThirdPartyOrderRequestDto request) {
        return delegate().createOrder(request);
    }

    @Override
    public ThirdPartyOrderResponseDto getOrderStatus(String orderId) {
        return delegate().getOrderStatus(orderId);
    }
}
