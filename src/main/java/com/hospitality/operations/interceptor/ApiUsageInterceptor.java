package com.hospitality.operations.interceptor;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.hospitality.operations.dashboard.metrics.ApiUsageLogService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ApiUsageInterceptor implements HandlerInterceptor {

    private static final String START_TIME_ATTR = "apiStartTime";

    private final ApiUsageLogService apiUsageLogService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute(START_TIME_ATTR, System.currentTimeMillis());

        String path = request.getRequestURI();
        String method = request.getMethod();

        if (isMetricsEndpoint(path)) {
            return true;
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        String path = request.getRequestURI();

        if (isMetricsEndpoint(path) || isAuthEndpoint(path)) {
            return;
        }

        Long startTime = (Long) request.getAttribute(START_TIME_ATTR);
        if (startTime == null) return;

        long duration = System.currentTimeMillis() - startTime;
        int status = response.getStatus();

        String username = null;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            username = auth.getName();
        }

        apiUsageLogService.log(path, request.getMethod(), status, duration, "default", username);
    }

    private boolean isMetricsEndpoint(String path) {
        return path.startsWith("/api/v1/metrics");
    }

    private boolean isAuthEndpoint(String path) {
        return path.startsWith("/api/v1/auth");
    }
}
