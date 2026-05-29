package com.hospitality.operations.domain.restaurant.order;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RestaurantOrderRepository extends JpaRepository<RestaurantOrder, Long> {

    Optional<RestaurantOrder> findByOrderReference(String orderReference);

    List<RestaurantOrder> findByCreatedAtBetween(Instant start, Instant end);

    List<RestaurantOrder> findByStatusAndCreatedAtBetween(OrderStatus status, Instant start, Instant end);

    List<RestaurantOrder> findByStatus(OrderStatus status);

    List<RestaurantOrder> findByStatusOrderByCreatedAtDesc(OrderStatus status);

    List<RestaurantOrder> findAllByOrderByCreatedAtDesc();
}
