package com.hospitality.operations.integration.thirdparty;

import java.util.List;

import org.springframework.stereotype.Service;

import com.hospitality.operations.integration.thirdparty.dto.ThirdPartyListingDto;
import com.hospitality.operations.integration.thirdparty.dto.ThirdPartyOrderRequestDto;
import com.hospitality.operations.integration.thirdparty.dto.ThirdPartyOrderResponseDto;

import lombok.extern.slf4j.Slf4j;

@Service("prodThirdPartyOrderService")
@Slf4j
public class ProdThirdPartyOrderService implements ThirdPartyOrderService {

    @Override
    public List<ThirdPartyListingDto> listListings() {
        log.debug("Production mode: no third-party integration configured");
        return List.of();
    }

    @Override
    public ThirdPartyOrderResponseDto createOrder(ThirdPartyOrderRequestDto request) {
        throw new UnsupportedOperationException("Third-party ordering is not available in production mode");
    }

    @Override
    public ThirdPartyOrderResponseDto getOrderStatus(String orderId) {
        throw new UnsupportedOperationException("Third-party ordering is not available in production mode");
    }
}
