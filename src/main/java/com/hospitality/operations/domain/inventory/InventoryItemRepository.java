package com.hospitality.operations.domain.inventory;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {

    List<InventoryItem> findByType(InventoryType type);

    List<InventoryItem> findByCategory(String category);

    List<InventoryItem> findByStatus(InventoryStatus status);

    List<InventoryItem> findByTypeAndCategory(InventoryType type, String category);

    List<InventoryItem> findByTypeAndStatus(InventoryType type, InventoryStatus status);

    List<InventoryItem> findByTypeAndCategoryAndStatus(InventoryType type, String category, InventoryStatus status);
}
