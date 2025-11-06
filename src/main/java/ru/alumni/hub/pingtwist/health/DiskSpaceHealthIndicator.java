package ru.alumni.hub.subtlenotify.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component("diskSpace")
public class DiskSpaceHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        long freeSpace = new java.io.File("/").getFreeSpace();
        long totalSpace = new java.io.File("/").getTotalSpace();
        long threshold = 1024 * 1024 * 1024; // 1 GB

        if (freeSpace < threshold) {
            return Health.down()
                    .withDetail("free", freeSpace)
                    .withDetail("total", totalSpace)
                    .withDetail("threshold", threshold)
                    .build();
        }

        return Health.up()
                .withDetail("free", freeSpace)
                .withDetail("total", totalSpace)
                .withDetail("usagePercent", (totalSpace - freeSpace) * 100.0 / totalSpace)
                .build();
    }
}