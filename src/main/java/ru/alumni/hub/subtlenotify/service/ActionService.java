package ru.alumni.hub.subtlenotify.service;

import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.alumni.hub.subtlenotify.exception.SubtleNotifyException;
import ru.alumni.hub.subtlenotify.health.ActionsMetrics;
import ru.alumni.hub.subtlenotify.model.Action;
import ru.alumni.hub.subtlenotify.model.ActionType;
import ru.alumni.hub.subtlenotify.model.User;
import ru.alumni.hub.subtlenotify.repository.ActionRepository;
import ru.alumni.hub.subtlenotify.types.ActionRequest;

import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ActionService {

    Logger LOGGER = LoggerFactory.getLogger(ActionService.class);

    private final ActionRepository actionsRepository;
    private final ActionsMetrics actionsMetrics;
    private final UserService userService;
    private final ActionTypeService actionTypeService;

    /**
     * Store a new action with string parameters
     * @param triggerRequest the request
     * @return Optional containing the saved Trigger
     */
    @Transactional
    public Optional<Action> storeAction(ActionRequest request) {
        var timer = actionsMetrics.startTimer();
        try {
            Optional<User> userOpt = userService.storeUser(request.getUserId());
            Optional<ActionType> actionTypeOpt = actionTypeService.storeActionType(request.getActionType());

            if (userOpt.isPresent() && actionTypeOpt.isPresent()) {
                Action action = new Action();
                action.setUser(userOpt.get());
                action.setActionType(actionTypeOpt.get());
                action.setTimestamp(request.getTimestamp());

                return Optional.of(actionsRepository.save(action));
            } else {
                actionsMetrics.incrementActionsFailed();
                return Optional.empty();
            }
        } catch (Exception e) {
            actionsMetrics.incrementActionsFailed();
            LOGGER.error("Error while storing action: "+request, e);
        } finally {
            actionsMetrics.incrementActionsCreated();
            actionsMetrics.recordCreationTime(timer);
        }
        return Optional.empty();
    }

    public List<Action> getUserActions(User user, ActionType actionType, List<Integer> dayList, List<Integer> weekList) throws SubtleNotifyException {
        if(! dayList.isEmpty() ) {
            return actionsRepository.findByUserIdAndActionTypeByDays(user, actionType, dayList);
        } else if(! weekList.isEmpty() ) {
            return actionsRepository.findByUserIdAndActionTypeByWeeks(user, actionType, weekList);
        } else {
            throw new SubtleNotifyException("Both filter of days and weeks should not be empty");
        }
    }

    public List<Action> getUserActions(User user, ActionType actionType) {
        return actionsRepository.findByUserIdAndActionTypeByDays(user, actionType);
    }

    public List<Action> getActions(String userId, String actionType) {
        boolean hasUserId = !StringUtils.isBlank(userId);
        boolean hasActionType = !StringUtils.isBlank(actionType);

        if (hasUserId && hasActionType) {
            Optional<User> userOpt = userService.getUser(userId);
            Optional<ActionType> actionTypeOpt = actionTypeService.getActionType(actionType);

            if (userOpt.isPresent() && actionTypeOpt.isPresent()) {
                return getUserActions(userOpt.get(), actionTypeOpt.get());
            }
            return List.of();
        } else if (hasActionType) {
            return actionTypeService.getActionType(actionType)
                    .map(actionsRepository::findByActionType)
                    .orElseGet(List::of);
        } else if (hasUserId) {
            return userService.getUser(userId)
                    .map(actionsRepository::findByUserId)
                    .orElseGet(List::of);
        } else {
            return actionsRepository.findAll();
        }
    }

    @Transactional
    public void deleteAllActions() {
        actionsRepository.deleteAll();
    }

}