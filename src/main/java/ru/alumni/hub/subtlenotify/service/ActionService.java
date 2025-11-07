package ru.alumni.hub.subtlenotify.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.alumni.hub.subtlenotify.health.ActionsMetrics;
import ru.alumni.hub.subtlenotify.model.Action;
import ru.alumni.hub.subtlenotify.repository.ActionRepository;
import ru.alumni.hub.subtlenotify.types.ActionRequest;
import ru.alumni.hub.subtlenotify.types.ActionResponse;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ActionService {

    private final ActionRepository actionsRepository;
    private final ActionsMetrics actionsMetrics;

    @Transactional
    public Action storeAction(ActionRequest request) {
        var timer = actionsMetrics.startTimer();
        try {
            Action action = new Action();
            action.setUserId(request.getUserId());
            action.setActionType(request.getActionType());
            action.setTimestamp(request.getTimestamp());

            return actionsRepository.save(action);
        } catch (Exception e) {
            actionsMetrics.incrementActionsFailed();
            throw e;
        } finally {
            actionsMetrics.incrementActionsCreated();
            actionsMetrics.recordCreationTime(timer);
        }
    }

    @Transactional
    public List<Action> storeActions(List<ActionRequest> requests) {
        List<Action> actions = requests.stream()
                .map(this::convertToEntity)
                .toList();

        return actionsRepository.saveAll(actions);
    }

    public List<Action> getUserActions(String userId, String actionType) {
        return actionsRepository.findByUserIdAndActionType(userId, actionType);
    }

    public List<ActionResponse> getAllActions() {
        return ActionResponse.fromEntityList(actionsRepository.findAll());
    }

    private Action convertToEntity(ActionRequest request) {
        Action action = new Action();
        action.setUserId(request.getUserId());
        action.setActionType(request.getActionType());
        action.setTimestamp(request.getTimestamp());
        return action;
    }
}