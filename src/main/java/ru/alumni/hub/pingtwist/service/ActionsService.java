package ru.alumni.hub.subtlenotify.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.alumni.hub.subtlenotify.model.Actions;
import ru.alumni.hub.subtlenotify.repository.ActionsRepository;
import ru.alumni.hub.subtlenotify.types.ActionRequest;
import ru.alumni.hub.subtlenotify.types.ActionResponse;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ActionsService {

    private final ActionsRepository actionsRepository;
    private final ru.alumni.hub.subtlenotify.metrics.ActionsMetrics actionsMetrics;

    @Transactional
    public Actions storeAction(ActionRequest request) {
        var timer = actionsMetrics.startTimer();
        try {
            Actions action = new Actions();
            action.setUserId(request.getUserId());
            action.setActionType(request.getActionType());
            action.setTimestamp(request.getTimestamp());

            Actions saved = actionsRepository.save(action);
            actionsMetrics.incrementActionsCreated();
            actionsMetrics.recordCreationTime(timer);
            return saved;
        } catch (Exception e) {
            actionsMetrics.incrementActionsFailed();
            throw e;
        }
    }

    @Transactional
    public List<Actions> storeActions(List<ActionRequest> requests) {
        List<Actions> actions = requests.stream()
                .map(this::convertToEntity)
                .toList();

        return actionsRepository.saveAll(actions);
    }

    public List<Actions> getUserActions(String userId) {
        return actionsRepository.findByUserId(userId);
    }

    public List<ActionResponse> getAllActions() {
        return ActionResponse.fromEntityList(actionsRepository.findAll());
    }

    private Actions convertToEntity(ActionRequest request) {
        Actions action = new Actions();
        action.setUserId(request.getUserId());
        action.setActionType(request.getActionType());
        action.setTimestamp(request.getTimestamp());
        return action;
    }
}