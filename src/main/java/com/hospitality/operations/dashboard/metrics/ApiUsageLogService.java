package com.hospitality.operations.dashboard.metrics;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApiUsageLogService {

    private final ApiUsageLogRepository repository;

    @Transactional
    public void log(String endpoint, String httpMethod, int statusCode, long durationMs, String tenantSchema, String username) {
        try {
            ApiUsageLog logEntry = ApiUsageLog.builder()
                    .endpoint(endpoint)
                    .httpMethod(httpMethod)
                    .statusCode(statusCode)
                    .durationMs(durationMs)
                    .tenantSchema(tenantSchema)
                    .username(username)
                    .calledAt(Instant.now())
                    .build();
            repository.save(logEntry);
        } catch (Exception e) {
            log.warn("Failed to persist API usage log: {}", e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getSummary() {
        Map<String, Object> summary = new LinkedHashMap<>();

        long totalRequests = repository.count();
        long failedRequests = repository.countByStatusCodeGreaterThanEqual(500);
        long successRequests = repository.countByStatusCodeIsNotNull();

        summary.put("totalRequests", totalRequests);
        summary.put("successRequests", successRequests);
        summary.put("failedRequests", failedRequests);
        summary.put("errorRate", totalRequests > 0 ? (double) failedRequests / totalRequests : 0.0);

        return summary;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getEndpointStats() {
        List<Object[]> raw = repository.findEndpointStats();
        List<Map<String, Object>> result = new ArrayList<>();

        for (Object[] row : raw) {
            Map<String, Object> stat = new LinkedHashMap<>();
            stat.put("endpoint", row[0]);
            stat.put("count", row[1]);
            stat.put("avgDurationMs", row[2] != null ? Math.round((Double) row[2]) : 0);
            stat.put("failures", row[3]);
            result.add(stat);
        }

        return result;
    }

    @Transactional(readOnly = true)
    public String exportCsv() {
        List<ApiUsageLog> allLogs = repository.findAll();
        StringBuilder sb = new StringBuilder();
        sb.append("ID,Endpoint,Method,Status,DurationMs,Username,CalledAt\n");
        for (ApiUsageLog log : allLogs) {
            sb.append(log.getId()).append(",");
            sb.append(escapeCsv(log.getEndpoint())).append(",");
            sb.append(log.getHttpMethod()).append(",");
            sb.append(log.getStatusCode() != null ? log.getStatusCode() : "").append(",");
            sb.append(log.getDurationMs() != null ? log.getDurationMs() : "").append(",");
            sb.append(escapeCsv(log.getUsername())).append(",");
            sb.append(log.getCalledAt() != null ? log.getCalledAt().toString() : "").append("\n");
        }
        return sb.toString();
    }

    @Transactional(readOnly = true)
    public String exportJson() {
        List<ApiUsageLog> allLogs = repository.findAll();
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(allLogs);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize API usage logs to JSON: {}", e.getMessage());
            return "[]";
        }
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
