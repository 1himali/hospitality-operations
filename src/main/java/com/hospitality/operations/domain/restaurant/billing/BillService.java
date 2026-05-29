package com.hospitality.operations.domain.restaurant.billing;

import java.util.List;

import com.hospitality.operations.domain.restaurant.billing.dto.BillRequestDto;
import com.hospitality.operations.domain.restaurant.billing.dto.BillResponseDto;

public interface BillService {

    BillResponseDto generateBill(BillRequestDto requestDto);

    BillResponseDto getBillById(Long id);

    BillResponseDto getBillByOrderId(Long orderId);

    List<BillResponseDto> getAllBills();
}
