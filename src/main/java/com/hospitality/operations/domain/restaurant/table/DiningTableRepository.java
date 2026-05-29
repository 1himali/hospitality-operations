package com.hospitality.operations.domain.restaurant.table;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DiningTableRepository extends JpaRepository<DiningTable, Long> {

    List<DiningTable> findByStatus(TableStatus status);

    List<DiningTable> findByTenantSchema(String tenantSchema);

    List<DiningTable> findByTenantSchemaAndStatus(String tenantSchema, TableStatus status);

    @Query("SELECT t FROM DiningTable t WHERE LOWER(t.tableNumber) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(t.location) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(t.reservationName) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<DiningTable> search(@Param("query") String query);

    @Query("SELECT t FROM DiningTable t WHERE t.status = :status " +
           "AND (LOWER(t.tableNumber) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(t.location) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(t.reservationName) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<DiningTable> searchByStatus(@Param("query") String query, @Param("status") TableStatus status);
}
