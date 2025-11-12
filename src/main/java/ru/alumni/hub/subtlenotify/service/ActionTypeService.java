package ru.alumni.hub.subtlenotify.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.alumni.hub.subtlenotify.health.ActionsMetrics;
import ru.alumni.hub.subtlenotify.model.ActionType;
import ru.alumni.hub.subtlenotify.repository.ActionTypeRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ActionTypeService {

    private final ActionTypeRepository actionTypeRepository;
    private final ActionsMetrics actionsMetrics;

    /**
     * Store action type if not exists, or return existing action type
     * @param actionTypeStr the action type identifier to store
     * @return Optional containing the ActionType object (existing or newly created)
     */
    @Transactional
    public Optional<ActionType> storeActionType(String actionTypeStr) {
        var timer = actionsMetrics.startTimer();
        try {
            Optional<ActionType> actionType = Optional.of(actionTypeRepository.findByActionType(actionTypeStr)
                    .orElseGet(() -> {
                        ActionType newActionType = new ActionType();
                        newActionType.setActionType(actionTypeStr);
                        ActionType savedActionType = actionTypeRepository.save(newActionType);
                        return savedActionType;
                    }));
            actionType.ifPresentOrElse(a->{}, actionsMetrics::incrementActionsFailed);
            return actionType;
        } finally {
            actionsMetrics.incrementActionsCreated();
            actionsMetrics.recordCreationTime(timer);
        }
    }

    /**
     * Get action type by actionType string
     * @param actionType the action type identifier
     * @return Optional containing the ActionType if found
     */
    public Optional<ActionType> getActionType(String actionType) {
        return actionTypeRepository.findByActionType(actionType);
    }
}