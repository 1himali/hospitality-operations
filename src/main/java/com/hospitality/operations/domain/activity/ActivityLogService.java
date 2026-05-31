package com.hospitality.operations.domain.activity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ActivityLogService {

    private final ActivityLogRepository repository;

    public ActivityLog record(String actor, String role, String actionType, String module, Long entityId,
                              String beforeState, String afterState, String notes) {
        ActivityLog log = ActivityLog.builder()
                .actor(actor)
                .role(role)
                .actionType(actionType)
                .module(module)
                .entityId(entityId)
                .beforeState(beforeState)
                .afterState(afterState)
                .occurredAt(Instant.now())
                .success(true)
                .notes(notes)
                .build();
        return repository.save(log);
    }

    public Page<ActivityLog> search(String actor, String module, String actionType,
                                    Instant dateFrom, Instant dateTo, Pageable pageable) {
        Specification<ActivityLog> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (actor != null && !actor.isBlank()) {
                predicates.add(cb.equal(root.get("actor"), actor));
            }
            if (module != null && !module.isBlank()) {
                predicates.add(cb.equal(root.get("module"), module));
            }
            if (actionType != null && !actionType.isBlank()) {
                predicates.add(cb.equal(root.get("actionType"), actionType));
            }
            if (dateFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("occurredAt"), dateFrom));
            }
            if (dateTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("occurredAt"), dateTo));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return repository.findAll(spec, pageable);
    }
}
