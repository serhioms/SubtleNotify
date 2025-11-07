package ru.alumni.hub.subtlenotify.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.alumni.hub.subtlenotify.health.ActionsMetrics;
import ru.alumni.hub.subtlenotify.types.TriggerRequest;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TriggerService {

    private final ActionsMetrics actionsMetrics;
    private static final List<TriggerRequest> triggers = new ArrayList<TriggerRequest>(64);

    @Transactional
    public TriggerRequest storeTrigger(TriggerRequest request) {
        var timer = actionsMetrics.startTimer();
        try {
            triggers.add(request);
            return request;
        } catch (Exception e) {
            actionsMetrics.incrementActionsFailed();
            throw e;
        } finally {
            actionsMetrics.incrementActionsCreated();
            actionsMetrics.recordCreationTime(timer);
        }
    }

    public List<TriggerRequest> getAllTriggers() {
        return triggers;
    }

    public TriggerRequest getTriggerByIdent(String triggerIdent) {
        return triggers.stream().filter(t -> triggerIdent.equals(t.getTriggerIdent())).findFirst().orElse(null);
    }

}