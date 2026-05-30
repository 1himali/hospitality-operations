package com.hospitality.operations.domain.payroll;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/payroll/records")
@RequiredArgsConstructor
public class PayrollRecordController {

    private final PayrollRecordService payrollRecordService;

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<PayrollRecord>> getByEmployee(@PathVariable Long employeeId) {
        return ResponseEntity.ok(payrollRecordService.getRecordsByEmployee(employeeId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PayrollRecord> getById(@PathVariable Long id) {
        return ResponseEntity.ok(payrollRecordService.findById(id));
    }

    @PostMapping
    public ResponseEntity<PayrollRecord> create(@RequestBody PayrollRecord record) {
        if (record.getEmployeeId() == null) {
            throw new IllegalArgumentException("employeeId is required");
        }
        return ResponseEntity.ok(payrollRecordService.create(record.getEmployeeId(), record));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PayrollRecord> update(@PathVariable Long id, @RequestBody PayrollRecord record) {
        return ResponseEntity.ok(payrollRecordService.update(id, record));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        payrollRecordService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
