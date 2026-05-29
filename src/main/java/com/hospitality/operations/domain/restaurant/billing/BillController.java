package com.hospitality.operations.domain.restaurant.billing;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hospitality.operations.domain.restaurant.billing.dto.BillRequestDto;
import com.hospitality.operations.domain.restaurant.billing.dto.BillResponseDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/bills")
@RequiredArgsConstructor
public class BillController {

    private final BillService billService;

    @PostMapping
    public ResponseEntity<BillResponseDto> generateBill(@Valid @RequestBody BillRequestDto requestDto) {
        BillResponseDto response = billService.generateBill(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BillResponseDto> getBillById(@PathVariable Long id) {
        BillResponseDto response = billService.getBillById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<BillResponseDto>> getBills(
            @RequestParam(required = false) Long orderId) {

        if (orderId != null) {
            BillResponseDto response = billService.getBillByOrderId(orderId);
            return ResponseEntity.ok(List.of(response));
        }

        List<BillResponseDto> bills = billService.getAllBills();
        return ResponseEntity.ok(bills);
    }
}
