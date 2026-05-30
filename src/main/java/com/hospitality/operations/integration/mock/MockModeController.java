package com.hospitality.operations.integration.mock;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/mock-mode")
@RequiredArgsConstructor
public class MockModeController {

    private final MockModeService mockModeService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getStatus() {
        return ResponseEntity.ok(Map.of("enabled", mockModeService.isMockMode()));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> setStatus(@RequestBody Map<String, Boolean> body) {
        boolean enabled = body.getOrDefault("enabled", false);
        if (enabled) {
            mockModeService.enableMockMode();
        } else {
            mockModeService.disableMockMode();
        }
        return ResponseEntity.ok(Map.of("enabled", mockModeService.isMockMode()));
    }
}
