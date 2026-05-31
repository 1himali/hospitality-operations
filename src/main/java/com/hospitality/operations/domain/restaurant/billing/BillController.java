package com.hospitality.operations.domain.restaurant.billing;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hospitality.operations.domain.activity.AuditHelper;
import com.hospitality.operations.domain.restaurant.billing.dto.BillRequestDto;
import com.hospitality.operations.domain.restaurant.billing.dto.BillResponseDto;

import java.util.Map;

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

    @PutMapping("/{id}")
    public ResponseEntity<BillResponseDto> updateBill(
            @PathVariable Long id,
            @Valid @RequestBody BillRequestDto requestDto) {
        BillResponseDto response = billService.updateBill(id, requestDto);
        auditHelper.record("UPDATE", "BILL", id, "Updated bill: " + response.getInvoiceNumber());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/flag")
    public ResponseEntity<BillResponseDto> flagInvoice(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String note = body.get("note");
        BillResponseDto response = billService.flagInvoice(id, note);
        auditHelper.record("FLAG", "BILL", id, "Invoice flagged: " + response.getInvoiceNumber() + " — " + (Boolean.TRUE.equals(response.getFlagged()) ? "Flagged" : "Unflagged"));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/export/csv")
    public ResponseEntity<byte[]> exportCsv(
            @RequestParam(required = false) Instant dateFrom,
            @RequestParam(required = false) Instant dateTo) {
        String csv = billService.exportCsv(dateFrom, dateTo);
        byte[] bytes = csv.getBytes(StandardCharsets.UTF_8);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "invoice_history.csv");
        return ResponseEntity.ok().headers(headers).body(bytes);
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
