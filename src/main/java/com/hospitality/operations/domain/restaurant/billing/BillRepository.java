package com.hospitality.operations.domain.restaurant.billing;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {

    Optional<Bill> findByOrderId(Long orderId);

    List<Bill> findByTenantSchema(String tenantSchema);

    Page<Bill> findAllByCreatedAtBetween(Instant from, Instant to, Pageable pageable);

    Page<Bill> findAllByCreatedAtAfter(Instant from, Pageable pageable);

    Page<Bill> findAllByCreatedAtBefore(Instant to, Pageable pageable);
}
