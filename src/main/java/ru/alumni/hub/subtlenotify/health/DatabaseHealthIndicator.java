package ru.alumni.hub.subtlenotify.health;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import ru.alumni.hub.subtlenotify.repository.ActionRepository;

@Component
@RequiredArgsConstructor
public class DatabaseHealthIndicator implements HealthIndicator {

    private final ActionRepository actionsRepository;

    @Override
    public Health health() {
        try {
            long count = actionsRepository.count();
            return Health.up()
                    .withDetail("database", "Available")
                    .withDetail("actionsCount", count)
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("database", "Unavailable")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}