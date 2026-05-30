package com.hospitality.operations.dashboard.metrics;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ApiUsageLogRepository extends JpaRepository<ApiUsageLog, Long> {

    long count();

    long countByCalledAtAfter(Instant since);

    long countByStatusCodeIsNotNull();

    long countByStatusCodeGreaterThanEqual(int statusCode);

    @Query("SELECT a.endpoint, COUNT(a) as cnt, AVG(a.durationMs) as avgDuration, " +
           "SUM(CASE WHEN a.statusCode >= 500 THEN 1 ELSE 0 END) as failures " +
           "FROM ApiUsageLog a GROUP BY a.endpoint ORDER BY cnt DESC")
    List<Object[]> findEndpointStats();
}
