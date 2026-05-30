package com.hospitality.operations.dashboard.metrics;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/metrics")
@RequiredArgsConstructor
public class ApiMetricsController {

    private final ApiUsageLogService apiUsageLogService;

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSummary() {
        return ResponseEntity.ok(apiUsageLogService.getSummary());
    }

    @GetMapping("/endpoints")
    public ResponseEntity<List<Map<String, Object>>> getEndpointStats() {
        return ResponseEntity.ok(apiUsageLogService.getEndpointStats());
    }
}
