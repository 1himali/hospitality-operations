package com.hospitality.operations.integration.mock;

import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.stereotype.Service;

@Service
public class MockModeService {

    private final AtomicBoolean mockMode = new AtomicBoolean(false);

    public boolean isMockMode() {
        return mockMode.get();
    }

    public boolean enableMockMode() {
        return !mockMode.getAndSet(true);
    }

    public boolean disableMockMode() {
        return mockMode.getAndSet(false);
    }
}
