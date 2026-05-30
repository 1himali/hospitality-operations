package com.hospitality.operations.domain.payroll;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hospitality.operations.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PayrollRecordService {

    private final PayrollRecordRepository repository;
    private final PayrollEmployeeService employeeService;

    @Transactional(readOnly = true)
    public List<PayrollRecord> getRecordsByEmployee(Long employeeId) {
        employeeService.findById(employeeId);
        return repository.findByEmployeeIdOrderByPaymentDateDesc(employeeId);
    }

    @Transactional(readOnly = true)
    public PayrollRecord findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PayrollRecord", "id", id));
    }

    @Transactional
    public PayrollRecord create(Long employeeId, PayrollRecord record) {
        PayrollEmployee employee = employeeService.findById(employeeId);
        record.setEmployeeId(employeeId);
        record.setBaseSalary(employee.getSalary());
        if (record.getBonus() == null) record.setBonus(java.math.BigDecimal.ZERO);
        if (record.getDeductions() == null) record.setDeductions(java.math.BigDecimal.ZERO);
        record.setNetPay(record.getBaseSalary().add(record.getBonus()).subtract(record.getDeductions()));
        return repository.save(record);
    }

    @Transactional
    public PayrollRecord update(Long id, PayrollRecord updated) {
        PayrollRecord existing = findById(id);
        existing.setBonus(updated.getBonus() != null ? updated.getBonus() : java.math.BigDecimal.ZERO);
        existing.setDeductions(updated.getDeductions() != null ? updated.getDeductions() : java.math.BigDecimal.ZERO);
        existing.setBaseSalary(updated.getBaseSalary() != null ? updated.getBaseSalary() : existing.getBaseSalary());
        existing.setNetPay(existing.getBaseSalary().add(existing.getBonus()).subtract(existing.getDeductions()));
        existing.setPaymentDate(updated.getPaymentDate());
        existing.setNotes(updated.getNotes());
        existing.setStatus(updated.getStatus() != null ? updated.getStatus() : "PAID");
        return repository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        PayrollRecord existing = findById(id);
        repository.delete(existing);
    }
}
