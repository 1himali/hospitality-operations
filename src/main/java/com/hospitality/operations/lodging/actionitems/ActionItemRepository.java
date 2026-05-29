package com.hospitality.operations.lodging.actionitems;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActionItemRepository extends JpaRepository<ActionItem, Long> {

    List<ActionItem> findByStatus(ActionItemStatus status);

    List<ActionItem> findByCategory(ActionItemCategory category);

    List<ActionItem> findByStatusAndCategory(ActionItemStatus status, ActionItemCategory category);
}
