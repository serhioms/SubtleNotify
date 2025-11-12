package ru.alumni.hub.subtlenotify.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.alumni.hub.subtlenotify.health.ActionsMetrics;
import ru.alumni.hub.subtlenotify.model.ActionType;
import ru.alumni.hub.subtlenotify.repository.ActionTypeRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ActionTypeService {

    Logger LOGGER = LoggerFactory.getLogger(ActionTypeService.class);

    private final ActionTypeRepository actionTypeRepository;
    private final ActionsMetrics actionsMetrics;

    /**
     * Get action type by actionType string
     * @param actionType the action type identifier
     * @return Optional containing the ActionType if found
     */
    public Optional<ActionType> getActionType(String actionType) {
        try {
            return actionTypeRepository.findByActionType(actionType);
        } catch (Exception e) {
            LOGGER.error("Error while retrieving action type: " + actionType, e);
        }
        return Optional.empty();
    }

    /**
     * Store action type if not exists, or return existing action type
     * @param actionTypeStr the action type identifier to store
     * @return Optional containing the ActionType object (existing or newly created)
     */
    @Transactional
    public Optional<ActionType> storeActionType(String actionTypeStr) {
        var timer = actionsMetrics.startTimer();
        try {
            ActionType actionType = actionTypeRepository.findByActionType(actionTypeStr)
                    .orElseGet(() -> {
                        ActionType newActionType = new ActionType();
                        newActionType.setActionType(actionTypeStr);
                        ActionType savedActionType = actionTypeRepository.save(newActionType);
                        LOGGER.info("Created new action type: {}", actionTypeStr);
                        return savedActionType;
                    });

            return Optional.of(actionType);
        } catch (Exception e) {
            actionsMetrics.incrementActionsFailed();
            LOGGER.error("Error while storing action type: " + actionTypeStr, e);
        } finally {
            actionsMetrics.incrementActionsCreated();
            actionsMetrics.recordCreationTime(timer);
        }
        return Optional.empty();
    }

}