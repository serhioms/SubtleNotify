package ru.alumni.hub.subtlenotify.service;

import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.alumni.hub.subtlenotify.health.ActionsMetrics;
import ru.alumni.hub.subtlenotify.model.Action;
import ru.alumni.hub.subtlenotify.repository.ActionRepository;
import ru.alumni.hub.subtlenotify.types.ActionRequest;

import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ActionService {

    private final ActionRepository actionsRepository;
    private final ActionsMetrics actionsMetrics;

    public Action storeAction(ActionRequest request) {
        var timer = actionsMetrics.startTimer();
        try {
            Action action = new Action();
            action.setUserId(request.getUserId());
            action.setActionType(request.getActionType());
            action.setTimestamp(request.getTimestamp());
            action.setDayOfYear(request.getTimestamp().getDayOfYear());
            action.setWeekOfYear(request.getTimestamp().get(WeekFields.of(Locale.getDefault()).weekOfYear()));

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

    public List<Action> getUserActions(String userId, String actionType, List<Integer> dayList, List<Integer> weekList) {
        if(! dayList.isEmpty() ) {
            return actionsRepository.findByUserIdAndActionTypeByDays(userId, actionType, dayList);
        } else if(! weekList.isEmpty() ) {
            return actionsRepository.findByUserIdAndActionTypeByWeeks(userId, actionType, weekList);
        } else {
            throw new RuntimeException("Both filter of days and weeks should not be empty");
        }
    }

    public List<Action> getUserActions(String userId, String actionType) {
        return actionsRepository.findByUserIdAndActionTypeByDays(userId, actionType);
    }

    public List<Action> getActions(String userId, String actionType) {
        if ( !StringUtils.isBlank(userId) && !StringUtils.isBlank(actionType) ) {
            return getUserActions(userId, actionType);
        } else if ( !StringUtils.isBlank(actionType) ) {
            return actionsRepository.findByActionType(actionType);
        } else if ( !StringUtils.isBlank(userId) ) {
            return actionsRepository.findByUserId(userId);
        } else {
            return actionsRepository.findAll();
        }
    }

    private Action convertToEntity(ActionRequest request) {
        Action action = new Action();
        action.setUserId(request.getUserId());
        action.setActionType(request.getActionType());
        action.setTimestamp(request.getTimestamp());
        return action;
    }

    @Transactional
    public void deleteAllActions() {
        actionsRepository.deleteAll();
    }

}