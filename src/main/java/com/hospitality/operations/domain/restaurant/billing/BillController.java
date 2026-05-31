package com.hospitality.operations.domain.restaurant.billing;

import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hospitality.operations.domain.activity.AuditHelper;
import com.hospitality.operations.domain.restaurant.billing.dto.BillRequestDto;
import com.hospitality.operations.domain.restaurant.billing.dto.BillResponseDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/bills")
@RequiredArgsConstructor
public class BillController {

    private final BillService billService;
    private final AuditHelper auditHelper;

    @PostMapping
    public ResponseEntity<BillResponseDto> generateBill(@Valid @RequestBody BillRequestDto requestDto) {
        BillResponseDto response = billService.generateBill(requestDto);
        auditHelper.record("CREATE", "BILL", response.getId(), "Generated bill: " + response.getInvoiceNumber());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BillResponseDto> getBillById(@PathVariable Long id) {
        BillResponseDto response = billService.getBillById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<BillResponseDto>> getBills(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) Long orderId,
            @RequestParam(required = false) Instant dateFrom,
            @RequestParam(required = false) Instant dateTo) {

        if (orderId != null) {
            BillResponseDto response = billService.getBillByOrderId(orderId);
            return ResponseEntity.ok(new PageImpl<>(List.of(response), pageable, 1));
        }

        Page<BillResponseDto> bills = billService.getBills(dateFrom, dateTo, pageable);
        return ResponseEntity.ok(bills);
    }
}
