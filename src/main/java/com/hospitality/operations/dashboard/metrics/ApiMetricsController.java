package com.hospitality.operations.dashboard.metrics;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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

    @GetMapping("/export/csv")
    public ResponseEntity<byte[]> exportCsv() {
        String csv = apiUsageLogService.exportCsv();
        byte[] bytes = csv.getBytes(StandardCharsets.UTF_8);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "api_metrics.csv");
        return ResponseEntity.ok().headers(headers).body(bytes);
    }

    @GetMapping("/export/json")
    public ResponseEntity<byte[]> exportJson() {
        String json = apiUsageLogService.exportJson();
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setContentDispositionFormData("attachment", "api_metrics.json");
        return ResponseEntity.ok().headers(headers).body(bytes);
    }
}
