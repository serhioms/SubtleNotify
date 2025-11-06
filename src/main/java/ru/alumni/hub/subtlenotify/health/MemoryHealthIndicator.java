package ru.alumni.hub.subtlenotify.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component("memory")
public class MemoryHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long freeMemory = runtime.freeMemory();
        long totalMemory = runtime.totalMemory();
        long usedMemory = totalMemory - freeMemory;

        double usagePercent = (usedMemory * 100.0) / maxMemory;

        Health.Builder builder = usagePercent > 90 ? Health.down() : Health.up();

        return builder
                .withDetail("max", maxMemory)
                .withDetail("total", totalMemory)
                .withDetail("free", freeMemory)
                .withDetail("used", usedMemory)
                .withDetail("usagePercent", String.format("%.2f%%", usagePercent))
                .build();
    }
}