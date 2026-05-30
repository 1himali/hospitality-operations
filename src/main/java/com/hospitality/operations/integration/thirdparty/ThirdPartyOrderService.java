package com.hospitality.operations.integration.thirdparty;

import java.util.List;

import com.hospitality.operations.integration.thirdparty.dto.ThirdPartyListingDto;
import com.hospitality.operations.integration.thirdparty.dto.ThirdPartyOrderRequestDto;
import com.hospitality.operations.integration.thirdparty.dto.ThirdPartyOrderResponseDto;

public interface ThirdPartyOrderService {

    List<ThirdPartyListingDto> listListings();

    ThirdPartyOrderResponseDto createOrder(ThirdPartyOrderRequestDto request);

    ThirdPartyOrderResponseDto getOrderStatus(String orderId);
}
