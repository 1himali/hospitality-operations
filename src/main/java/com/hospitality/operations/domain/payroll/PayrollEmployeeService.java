package com.hospitality.operations.domain.payroll;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hospitality.operations.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PayrollEmployeeService {

    private final PayrollEmployeeRepository repository;

    @Transactional(readOnly = true)
    public List<PayrollEmployee> findAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public PayrollEmployee findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PayrollEmployee", "id", id));
    }

    @Transactional
    public PayrollEmployee create(PayrollEmployee employee) {
        return repository.save(employee);
    }

    @Transactional
    public PayrollEmployee update(Long id, PayrollEmployee updated) {
        PayrollEmployee existing = findById(id);
        existing.setName(updated.getName());
        existing.setPosition(updated.getPosition());
        existing.setDepartment(updated.getDepartment());
        existing.setSalary(updated.getSalary());
        existing.setStatus(updated.getStatus());
        existing.setPhone(updated.getPhone());
        existing.setEmail(updated.getEmail());
        return repository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        PayrollEmployee existing = findById(id);
        repository.delete(existing);
    }

    @Transactional(readOnly = true)
    public String exportCsv() {
        List<PayrollEmployee> all = repository.findAll();
        StringBuilder sb = new StringBuilder();
        sb.append("ID,Name,Position,Department,Salary,Status,Phone,Email,HireDate\n");
        for (PayrollEmployee e : all) {
            sb.append(e.getId()).append(",");
            sb.append(escapeCsv(e.getName())).append(",");
            sb.append(escapeCsv(e.getPosition())).append(",");
            sb.append(escapeCsv(e.getDepartment())).append(",");
            sb.append(e.getSalary()).append(",");
            sb.append(e.getStatus()).append(",");
            sb.append(escapeCsv(e.getPhone() != null ? e.getPhone() : "")).append(",");
            sb.append(escapeCsv(e.getEmail() != null ? e.getEmail() : "")).append(",");
            sb.append(e.getHireDate() != null ? e.getHireDate().toString() : "").append("\n");
        }
        return sb.toString();
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
