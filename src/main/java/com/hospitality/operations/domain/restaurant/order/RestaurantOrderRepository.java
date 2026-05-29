package com.hospitality.operations.domain.restaurant.order;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RestaurantOrderRepository extends JpaRepository<RestaurantOrder, Long> {

    Optional<RestaurantOrder> findByOrderReference(String orderReference);
}
