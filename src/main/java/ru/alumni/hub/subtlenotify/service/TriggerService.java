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

    Logger LOGGER = LoggerFactory.getLogger(TriggerService.class);

    private final ActionsMetrics actionsMetrics;
    private final ActionTypeService actionTypeService;
    private final NotifyMessageService notifyMessageService;
    private final TriggerRepository triggerRepository;


    /**
     * Store a new trigger with string parameters
     * @param triggerRequest the request
     * @return Optional containing the saved Trigger
     */
    @Transactional
    public Optional<Trigger> storeTrigger(TriggerRequest request) {
        var timer = actionsMetrics.startTimer();
        try {
            Optional<NotifyMessage> messageOpt = notifyMessageService.getNotifyMessage(request.getNotifIdent());
            Optional<ActionType> actionTypeOpt = actionTypeService.getActionType(request.getTriggerIdent());

            if (messageOpt.isEmpty()) {
                messageOpt = notifyMessageService.storeNotifyMessage(request.getNotifIdent(), request.getNotifDescr());
            }

            if (actionTypeOpt.isEmpty()) {
                actionTypeOpt = actionTypeService.storeActionType(request.getTriggerIdent());
            }

            Trigger trigger = new Trigger();
            trigger.setNotifyMessage(messageOpt.get());
            trigger.setActionType(actionTypeOpt.get());
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

            return Optional.of(triggerRepository.save(trigger));
        } catch (Exception e) {
            actionsMetrics.incrementActionsFailed();
            LOGGER.error("Error while storing trigger: "+request, e);
        } finally {
            actionsMetrics.incrementActionsCreated();
            actionsMetrics.recordCreationTime(timer);
        }
        return Optional.empty();
    }


    /**
     * Get all triggers
     * @return List of all Trigger objects
     */
    public List<Trigger> getAllTriggers() {
        try {
            return triggerRepository.findAll();
        } catch (Exception e) {
            LOGGER.error("Error while retrieving all triggers", e);
        }
        return List.of();
    }

    /**
     * Get triggers by action type
     * @param actionType the action type string
     * @return List of triggers for the action type
     */
    public List<Trigger> getTriggersByActionType(String actionType) {
        try {
            return actionTypeService.getActionType(actionType)
                    .map(triggerRepository::findByActionType)
                    .orElseGet(List::of);
        } catch (Exception e) {
            LOGGER.error("Error while retrieving triggers for action type: " + actionType, e);
        }
        return List.of();
    }

    /**
     * Get triggers by notify message
     * @param messageIdent the message identifier
     * @return List of triggers for the notify message
     */
    public List<Trigger> getTriggersByNotifyMessage(String messageIdent) {
        try {
            return notifyMessageService.getNotifyMessage(messageIdent)
                    .map(triggerRepository::findByNotifyMessage)
                    .orElseGet(List::of);
        } catch (Exception e) {
            LOGGER.error("Error while retrieving triggers for notify message: " + messageIdent, e);
        }
        return List.of();
    }


}