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
    private static final List<TriggerRequest> response = new ArrayList<TriggerRequest>(64);

    @Transactional
    public TriggerRequest storeTrigger(TriggerRequest request) {
        var timer = actionsMetrics.startTimer();
        try {
            response.add(request);
            actionsMetrics.incrementActionsCreated();
            actionsMetrics.recordCreationTime(timer);
            return request;
        } catch (Exception e) {
            actionsMetrics.incrementActionsFailed();
            throw e;
        }
    }

    public List<TriggerRequest> getAllTriggers() {
        return response;
    }

}