package ru.alumni.hub.subtlenotify.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

@Component
public class ActionsMetrics {

    private final Counter actionsCreatedCounter;
    private final Counter actionsFailedCounter;
    private final Timer actionCreationTimer;
    private final MeterRegistry meterRegistry;

    public ActionsMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        this.actionsCreatedCounter = Counter.builder("actions.created.total")
                .description("Total number of actions created")
                .register(meterRegistry);

        this.actionsFailedCounter = Counter.builder("actions.failed.total")
                .description("Total number of failed action creations")
                .register(meterRegistry);

        this.actionCreationTimer = Timer.builder("actions.creation.time")
                .description("Time taken to create an action")
                .register(meterRegistry);
    }

    public void incrementActionsCreated() {
        actionsCreatedCounter.increment();
    }

    public void incrementActionsFailed() {
        actionsFailedCounter.increment();
    }

    public Timer.Sample startTimer() {
        return Timer.start(meterRegistry);
    }

    public void recordCreationTime(Timer.Sample sample) {
        sample.stop(actionCreationTimer);
    }
}