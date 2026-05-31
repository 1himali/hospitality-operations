package com.hospitality.operations.domain.activity;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.hospitality.operations.auth.User;
import com.hospitality.operations.auth.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AuditHelper {

    private final ActivityLogService activityLogService;
    private final UserRepository userRepository;

    public void record(String actionType, String module, Long entityId, String notes) {
        record(actionType, module, entityId, null, null, notes);
    }

    public void record(String actionType, String module, Long entityId,
                       String beforeState, String afterState, String notes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String actor = "anonymous";
        String role = "NONE";
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            actor = auth.getName();
            role = auth.getAuthorities().stream()
                    .findFirst()
                    .map(g -> g.getAuthority().replace("ROLE_", ""))
                    .orElse("NONE");
        }
        activityLogService.record(actor, role, actionType, module, entityId, beforeState, afterState, notes);
    }
}
