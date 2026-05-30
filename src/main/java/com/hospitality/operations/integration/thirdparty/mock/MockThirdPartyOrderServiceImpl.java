package com.hospitality.operations.integration.thirdparty.mock;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;

import com.hospitality.operations.integration.thirdparty.ThirdPartyOrderService;
import com.hospitality.operations.integration.thirdparty.dto.ThirdPartyListingDto;
import com.hospitality.operations.integration.thirdparty.dto.ThirdPartyOrderRequestDto;
import com.hospitality.operations.integration.thirdparty.dto.ThirdPartyOrderResponseDto;

@Service("mockThirdPartyOrderService")
public class MockThirdPartyOrderServiceImpl implements ThirdPartyOrderService {

    private static final List<ThirdPartyListingDto> MOCK_LISTINGS = List.of(
            ThirdPartyListingDto.builder().id("f1").name("Margherita Pizza")
                    .description("Classic tomato, mozzarella, basil").price(new BigDecimal("12.99"))
                    .currency("USD").platform("DoorDash").build(),
            ThirdPartyListingDto.builder().id("f2").name("Chicken Burger")
                    .description("Grilled chicken with lettuce and mayo").price(new BigDecimal("9.99"))
                    .currency("USD").platform("UberEats").build(),
            ThirdPartyListingDto.builder().id("f3").name("Caesar Salad")
                    .description("Romaine, croutons, parmesan, caesar dressing").price(new BigDecimal("8.49"))
                    .currency("USD").platform("Grubhub").build(),
            ThirdPartyListingDto.builder().id("f4").name("Sushi Platter")
                    .description("Assorted nigiri and maki rolls").price(new BigDecimal("18.99"))
                    .currency("USD").platform("DoorDash").build(),
            ThirdPartyListingDto.builder().id("f5").name("Pasta Carbonara")
                    .description("Spaghetti with pancetta, egg, parmesan").price(new BigDecimal("14.49"))
                    .currency("USD").platform("UberEats").build()
    );

    private final Map<String, ThirdPartyOrderResponseDto> orders = new ConcurrentHashMap<>();
    private final AtomicLong idCounter = new AtomicLong(1000);

    @Override
    public List<ThirdPartyListingDto> listListings() {
        return MOCK_LISTINGS;
    }

    @Override
    public ThirdPartyOrderResponseDto createOrder(ThirdPartyOrderRequestDto request) {
        ThirdPartyListingDto listing = MOCK_LISTINGS.stream()
                .filter(l -> l.getId().equals(request.getListingId()))
                .findFirst()
                .orElse(null);

        String orderId = "MOCK-" + idCounter.incrementAndGet();
        ThirdPartyOrderResponseDto response = ThirdPartyOrderResponseDto.builder()
                .orderId(orderId)
                .listingId(request.getListingId())
                .listingName(listing != null ? listing.getName() : "Unknown")
                .quantity(request.getQuantity())
                .status("CONFIRMED")
                .totalAmount(listing != null
                        ? listing.getPrice().multiply(BigDecimal.valueOf(request.getQuantity()))
                        : BigDecimal.ZERO)
                .currency(listing != null ? listing.getCurrency() : "USD")
                .platform(listing != null ? listing.getPlatform() : "Unknown")
                .createdAt(LocalDateTime.now())
                .build();

        orders.put(orderId, response);
        return response;
    }

    @Override
    public ThirdPartyOrderResponseDto getOrderStatus(String orderId) {
        return orders.get(orderId);
    }
}
