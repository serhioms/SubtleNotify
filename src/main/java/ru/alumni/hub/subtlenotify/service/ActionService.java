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

            Action saved = actionsRepository.save(action);
            actionsMetrics.incrementActionsCreated();
            actionsMetrics.recordCreationTime(timer);
            return saved;
        } catch (Exception e) {
            actionsMetrics.incrementActionsFailed();
            throw e;
        }
    }

    @Transactional
    public List<Action> storeActions(List<ActionRequest> requests) {
        List<Action> actions = requests.stream()
                .map(this::convertToEntity)
                .toList();

        return actionsRepository.saveAll(actions);
    }

    public List<Action> getUserActions(String userId) {
        return actionsRepository.findByUserId(userId);
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