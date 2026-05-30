package com.hospitality.operations.integration.thirdparty;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hospitality.operations.integration.thirdparty.dto.ThirdPartyListingDto;
import com.hospitality.operations.integration.thirdparty.dto.ThirdPartyOrderRequestDto;
import com.hospitality.operations.integration.thirdparty.dto.ThirdPartyOrderResponseDto;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/third-party")
@RequiredArgsConstructor
public class ThirdPartyOrderController {

    private final ThirdPartyOrderService thirdPartyOrderService;

    @GetMapping("/listings")
    public ResponseEntity<List<ThirdPartyListingDto>> listListings() {
        return ResponseEntity.ok(thirdPartyOrderService.listListings());
    }

    @PostMapping("/orders")
    public ResponseEntity<ThirdPartyOrderResponseDto> createOrder(@RequestBody ThirdPartyOrderRequestDto request) {
        return ResponseEntity.ok(thirdPartyOrderService.createOrder(request));
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<ThirdPartyOrderResponseDto> getOrderStatus(@PathVariable String orderId) {
        ThirdPartyOrderResponseDto order = thirdPartyOrderService.getOrderStatus(orderId);
        if (order == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(order);
    }
}
