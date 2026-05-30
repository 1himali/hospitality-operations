package com.hospitality.operations.domain.restaurant.billing.mapping;

import java.util.List;

import com.hospitality.operations.domain.restaurant.billing.Bill;
import com.hospitality.operations.domain.restaurant.billing.dto.BillResponseDto;

public final class BillMapper {

    private BillMapper() {
    }

    public static BillResponseDto toDto(Bill bill, List<BillResponseDto.LineItemDto> lineItems) {
        return BillResponseDto.builder()
                .id(bill.getId())
                .orderId(bill.getOrderId())
                .orderReference(bill.getOrderReference())
                .tableId(bill.getTableId())
                .serverName(bill.getServerName())
                .customerName(bill.getCustomerName())
                .phoneNumber(bill.getPhoneNumber())
                .email(bill.getEmail())
                .invoiceNumber(bill.getInvoiceNumber())
                .status(bill.getStatus())
                .notes(bill.getNotes())
                .subtotal(bill.getSubtotal())
                .taxRate(bill.getTaxRate())
                .taxAmount(bill.getTaxAmount())
                .discount(bill.getDiscount())
                .totalDue(bill.getTotalDue())
                .aiDescription(bill.getAiDescription())
                .tenantSchema(bill.getTenantSchema())
                .createdAt(bill.getCreatedAt())
                .lineItems(lineItems)
                .build();
    }

    public static BillResponseDto toDto(Bill bill) {
        return toDto(bill, List.of());
    }
}
