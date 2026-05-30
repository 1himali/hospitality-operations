package com.hospitality.operations.domain.payroll;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PayrollEmployeeRepository extends JpaRepository<PayrollEmployee, Long> {

    List<PayrollEmployee> findByStatus(String status);

    List<PayrollEmployee> findByDepartment(String department);

    List<PayrollEmployee> findByNameContainingIgnoreCase(String name);
}
