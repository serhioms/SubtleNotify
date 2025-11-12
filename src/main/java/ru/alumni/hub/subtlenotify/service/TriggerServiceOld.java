package ru.alumni.hub.subtlenotify.service;

import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.alumni.hub.subtlenotify.health.ActionsMetrics;
import ru.alumni.hub.subtlenotify.types.TriggerRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TriggerServiceOld {

    Logger LOGGER = LoggerFactory.getLogger(TriggerServiceOld.class);

    private final ActionsMetrics actionsMetrics;
    private static final List<TriggerRequest> triggers = new ArrayList<TriggerRequest>(64);

    @Transactional
    public Optional<TriggerRequest> storeTrigger(TriggerRequest request) {
        var timer = actionsMetrics.startTimer();
        try {
            if( !StringUtils.isBlank(request.getExpectWeekDays()) ){
                request.setExpectWeekDays(request.getExpectWeekDays().toUpperCase()); // LocalDateTime.getDayOfWeek returns uppercase
            }
            if( !StringUtils.isBlank(request.getActualWeekDays()) ){
                request.setActualWeekDays(request.getActualWeekDays().toUpperCase()); // LocalDateTime.getDayOfWeek returns uppercase
            }
            triggers.add(request);
            return Optional.of(request);
        } catch (Exception e) {
            actionsMetrics.incrementActionsFailed();
            LOGGER.error("Error while storing trigger", e);
        } finally {
            actionsMetrics.incrementActionsCreated();
            actionsMetrics.recordCreationTime(timer);
        }
        return Optional.empty();
    }

    public List<TriggerRequest> getAllTriggers() {
        return triggers;
    }

    public List<TriggerRequest> getTriggersByIdent(String triggerIdent) {
        return triggers.stream().filter(t -> triggerIdent.equals(t.getTriggerIdent())).collect(Collectors.toList());
    }

    @Transactional
    public void deleteAllTriggers() {
        triggers.clear();
    }
}