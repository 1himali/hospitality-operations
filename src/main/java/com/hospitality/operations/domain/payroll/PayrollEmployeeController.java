package com.hospitality.operations.domain.payroll;

import java.nio.charset.StandardCharsets;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hospitality.operations.domain.activity.AuditHelper;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/payroll/employees")
@RequiredArgsConstructor
public class PayrollEmployeeController {

    private final PayrollEmployeeService payrollEmployeeService;
    private final AuditHelper auditHelper;

    @GetMapping
    public ResponseEntity<List<PayrollEmployee>> getAll(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String department) {
        if (name != null || status != null || department != null) {
            return ResponseEntity.ok(payrollEmployeeService.search(name, status, department));
        }
        return ResponseEntity.ok(payrollEmployeeService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PayrollEmployee> getById(@PathVariable Long id) {
        return ResponseEntity.ok(payrollEmployeeService.findById(id));
    }

    @PostMapping
    public ResponseEntity<PayrollEmployee> create(@RequestBody PayrollEmployee employee) {
        PayrollEmployee created = payrollEmployeeService.create(employee);
        auditHelper.record("CREATE", "PAYROLL_EMPLOYEE", created.getId(), "Created employee: " + created.getName());
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PayrollEmployee> update(@PathVariable Long id, @RequestBody PayrollEmployee employee) {
        PayrollEmployee updated = payrollEmployeeService.update(id, employee);
        auditHelper.record("UPDATE", "PAYROLL_EMPLOYEE", id, "Updated employee: " + updated.getName());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        PayrollEmployee prev = payrollEmployeeService.findById(id);
        payrollEmployeeService.delete(id);
        auditHelper.record("DELETE", "PAYROLL_EMPLOYEE", id, "Deleted employee: " + prev.getName());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/export/csv")
    public ResponseEntity<byte[]> exportCsv() {
        String csv = payrollEmployeeService.exportCsv();
        byte[] bytes = csv.getBytes(StandardCharsets.UTF_8);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "payroll_employees.csv");
        return ResponseEntity.ok().headers(headers).body(bytes);
    }
}
