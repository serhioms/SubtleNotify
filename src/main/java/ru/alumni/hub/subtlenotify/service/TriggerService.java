package ru.alumni.hub.subtlenotify.service;


import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.alumni.hub.subtlenotify.health.ActionsMetrics;
import ru.alumni.hub.subtlenotify.model.Action;
import ru.alumni.hub.subtlenotify.model.ActionType;
import ru.alumni.hub.subtlenotify.model.NotifyMessage;
import ru.alumni.hub.subtlenotify.model.Trigger;
import ru.alumni.hub.subtlenotify.repository.TriggerRepository;
import ru.alumni.hub.subtlenotify.types.NotifyMoment;
import ru.alumni.hub.subtlenotify.types.TriggerRequest;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TriggerService {

    private final ActionTypeService actionTypeService;
    private final NotifyMessageService notifyMessageService;
    private final TriggerRepository triggerRepository;
    private final ActionsMetrics actionsMetrics;

    /**
     * Store a new trigger with string parameters
     * @param triggerRequest the request
     * @return Optional containing the saved Trigger
     */
    @Transactional
    public void storeTrigger(TriggerRequest request) {
        var timer = actionsMetrics.startTimer();
        try {
            Optional<NotifyMessage> notificationMessage = notifyMessageService.getNotifyMessage(request.getNotifIdent());
            if (notificationMessage.isEmpty()) {
                notificationMessage = notifyMessageService.storeNotifyMessage(request.getNotifIdent(), request.getNotifDescr());
            }

            Optional<ActionType> actionType = actionTypeService.getActionType(request.getTriggerIdent());
            if (actionType.isEmpty()) {
                actionType = actionTypeService.storeActionType(request.getTriggerIdent());
            }

            Trigger trigger = new Trigger();
            notificationMessage.ifPresentOrElse(trigger::setNotifyMessage, ()->{});
            actionType.ifPresentOrElse(trigger::setActionType, ()->{});
            trigger.setDescr(request.getTriggerDescr());
            trigger.setExpectWeekDays(request.getExpectWeekDays());
            trigger.setExpectEveryDays(request.getExpectEveryDays());
            trigger.setExpectHowOften(request.getExpectHowOften());
            trigger.setExpectFromHr(request.getExpectFromHr());
            trigger.setExpectToHr(request.getExpectToHr());
            trigger.setMissPreviousTime(request.getMissPreviousTime() != null ? request.getMissPreviousTime() : false);
            trigger.setNotifyMoment(request.getNotifMoment());
            trigger.setActualHours(request.getActualHours());
            trigger.setActualWeekDays(request.getActualWeekDays());

            Optional.of(triggerRepository.save(trigger)).ifPresentOrElse(a->{}, actionsMetrics::incrementActionsFailed);
        } finally {
            actionsMetrics.incrementActionsCreated();
            actionsMetrics.recordCreationTime(timer);
        }
    }


    /**
     * Get all triggers
     * @return List of all Trigger objects
     */
    public List<Trigger> getAllTriggers() {
        return triggerRepository.findAll();
    }

    /**
     * Get triggers by action type
     * @param actionType the action type string
     * @return List of triggers for the action type
     */
    public List<Trigger> getTriggersByActionType(String actionType) {
        return triggerRepository.findByActionType(actionType);
    }

}