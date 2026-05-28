package com.hospitality.operations.domain.restaurant.menu;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

    List<MenuItem> findByTenantSchema(String tenantSchema);

    List<MenuItem> findByCategory(MenuCategory category);

    List<MenuItem> findByTenantSchemaAndCategory(String tenantSchema, MenuCategory category);

    List<MenuItem> findByAvailable(Boolean available);

    List<MenuItem> findByTenantSchemaAndAvailable(String tenantSchema, Boolean available);

    List<MenuItem> findByTenantSchemaAndCategoryAndAvailable(String tenantSchema, MenuCategory category, Boolean available);
}
