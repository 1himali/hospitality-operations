package com.hospitality.operations.domain.payroll;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
@RequestMapping("/api/v1/admin/payroll")
@RequiredArgsConstructor
public class PayrollEmployeeController {

    private final PayrollEmployeeService payrollEmployeeService;

    @GetMapping
    public ResponseEntity<List<PayrollEmployee>> getAll() {
        return ResponseEntity.ok(payrollEmployeeService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PayrollEmployee> getById(@PathVariable Long id) {
        return ResponseEntity.ok(payrollEmployeeService.findById(id));
    }

    @PostMapping
    public ResponseEntity<PayrollEmployee> create(@RequestBody PayrollEmployee employee) {
        return ResponseEntity.ok(payrollEmployeeService.create(employee));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PayrollEmployee> update(@PathVariable Long id, @RequestBody PayrollEmployee employee) {
        return ResponseEntity.ok(payrollEmployeeService.update(id, employee));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        payrollEmployeeService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/export/csv")
    public ResponseEntity<byte[]> exportCsv() {
        String csv = payrollEmployeeService.exportCsv();
        byte[] bytes = csv.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "payroll_employees.csv");
        return ResponseEntity.ok().headers(headers).body(bytes);
    }
}
