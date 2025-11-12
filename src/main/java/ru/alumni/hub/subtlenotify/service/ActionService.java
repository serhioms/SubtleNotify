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

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ActionService {

    Logger LOGGER = LoggerFactory.getLogger(ActionService.class);

    private final ActionRepository actionsRepository;
    private final ActionsMetrics actionsMetrics;
    private final UserService userService;
    private final ActionTypeService actionTypeService;

    @Transactional
    public Optional<Action> storeAction(ActionRequest request) {
        var timer = actionsMetrics.startTimer();
        try {
            Optional<User> user = userService.storeUser(request.getUserId());
            Optional<ActionType> actionType = actionTypeService.storeActionType(request.getActionType());

            Action action = new Action();
            user.ifPresentOrElse(action::setUser, ()->{});
            actionType.ifPresentOrElse(action::setActionType, ()->{});
            action.setTimestamp(request.getTimestamp());

            Optional<Action> save = Optional.of(actionsRepository.save(action));
            save.ifPresentOrElse(a->{}, actionsMetrics::incrementActionsFailed);
            return save;
        } finally {
            actionsMetrics.incrementActionsCreated();
            actionsMetrics.recordCreationTime(timer);
        }
    }

    public List<Action> getActionsForTimeRange(User user, ActionType actionType, List<Integer> dayList, List<Integer> weekList) throws SubtleNotifyException {
        if(! dayList.isEmpty() ) {
            return actionsRepository.findByUserIdAndActionTypeByDays(user, actionType, dayList);
        } else if(! weekList.isEmpty() ) {
            return actionsRepository.findByUserIdAndActionTypeByWeeks(user, actionType, weekList);
        } else {
            throw new SubtleNotifyException("Both filter of days and weeks should not be empty");
        }
    }

    public List<Action> getAllActions(String userId, String actionType) {
        boolean hasUserId = !StringUtils.isBlank(userId);
        boolean hasActionType = !StringUtils.isBlank(actionType);

        if (hasUserId && hasActionType) {
            return actionsRepository.findByUserIdAndActionType(userId, actionType);
        } else if (hasActionType) {
            return actionsRepository.findByActionType(actionType);
        } else if (hasUserId) {
            return actionsRepository.findByUserId(userId);
        } else {
            return actionsRepository.findAll();
        }
    }

    @Transactional
    public void deleteAllActions() {
        actionsRepository.deleteAll();
    }

}