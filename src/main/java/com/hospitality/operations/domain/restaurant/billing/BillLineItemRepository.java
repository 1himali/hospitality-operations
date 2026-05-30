package com.hospitality.operations.domain.restaurant.billing;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BillLineItemRepository extends JpaRepository<BillLineItem, Long> {

    List<BillLineItem> findByBillId(Long billId);

    void deleteByBillId(Long billId);
}
