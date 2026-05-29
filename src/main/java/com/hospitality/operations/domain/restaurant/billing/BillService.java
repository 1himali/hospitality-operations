package com.hospitality.operations.domain.restaurant.billing;

import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.hospitality.operations.domain.restaurant.billing.dto.BillRequestDto;
import com.hospitality.operations.domain.restaurant.billing.dto.BillResponseDto;

public interface BillService {

    BillResponseDto generateBill(BillRequestDto requestDto);

    BillResponseDto getBillById(Long id);

    BillResponseDto getBillByOrderId(Long orderId);

    List<BillResponseDto> getAllBills();

    Page<BillResponseDto> getBills(Instant dateFrom, Instant dateTo, Pageable pageable);
}
