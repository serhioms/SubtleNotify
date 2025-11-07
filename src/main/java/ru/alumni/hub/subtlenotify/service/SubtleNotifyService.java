package ru.alumni.hub.subtlenotify.service;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.alumni.hub.subtlenotify.health.ActionsMetrics;
import ru.alumni.hub.subtlenotify.model.Action;
import ru.alumni.hub.subtlenotify.types.ActionRequest;
import ru.alumni.hub.subtlenotify.types.NotificationResponse;
import ru.alumni.hub.subtlenotify.types.RefineAction;
import ru.alumni.hub.subtlenotify.types.TriggerRequest;

import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubtleNotifyService {

    public static final int NOTIFICATION_MINUTES_OFFSET = 5;

    private final ActionsMetrics actionsMetrics;
    private final ActionService actionService;
    private final TriggerService triggerService;

    @Transactional
    public Optional<NotificationResponse> processAction(Action action) {
        var timer = actionsMetrics.startTimer();
        try {
            TriggerRequest trigger = triggerService.getTriggerByIdent("write_comments_night");
            if (trigger != null) {
                List<Action> actions =  actionService.getUserActions(action.getUserId(), action.getActionType());

                int curDayOfYear = action.getTimestamp().getDayOfYear();
                int curWeekOfYear = action.getTimestamp().get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear());

                int oldestWeek = curWeekOfYear - trigger.getExpectWeeks();

                // filter actions by time and days
                List<RefineAction> selectedActions = actions.stream()
                        .map(RefineAction::new)
                        .filter(a -> checkRightWeek(a.getWeekOfYear(), oldestWeek ))
                        .filter(a -> checkRightTime(a.getHourOfDay(), trigger.getExpectFromHr(), trigger.getExpectToHr()))
                        .filter(a -> checkRightDays(a.getDayOfWeek(), trigger.getExpectWeekDaysList()))
                        .toList();
                ;

                Set<Integer> selectDays = selectedActions.stream().map(RefineAction::getDayOfYear).collect(Collectors.toSet());

                // filter actions by straight days
                if( trigger.getExpectStraightDays() != null ) {
                    for (int d = curDayOfYear, min = curDayOfYear - trigger.getExpectStraightDays(); d > min; d -= 1) {
                        if (!selectDays.contains(d)) {
                            return Optional.empty();
                        }
                    }
                    // filter actions by everydays
                } else if( trigger.getExpectEveryDays() != null ) {
                    for (int d = curDayOfYear, step = trigger.getExpectEveryDays()+1, min = curDayOfYear - trigger.getExpectWeeks()*7; d > min; d-=step) {
                        if (!selectDays.contains(d)){
                            return Optional.empty();
                        }
                    }
                } else {
                    return Optional.empty();
                }

                // generate notification
                return Optional.of(new NotificationResponse(rightDateTime(action, trigger), trigger.getNotifDescr()));
            }

        } catch (Exception e) {
            throw e;
        } finally {
            actionsMetrics.incrementActionsCreated();
            actionsMetrics.recordCreationTime(timer);
        }
        return Optional.empty();
    }

    private @NotNull(message = "timestamp is required") LocalDateTime rightDateTime(Action action, TriggerRequest trigger) {
        return switch(trigger.getNotifMoment()) {
            case immediately -> action.getTimestamp().plusMinutes(NOTIFICATION_MINUTES_OFFSET);
            case next_time -> action.getTimestamp().plusDays(1).minusMinutes(NOTIFICATION_MINUTES_OFFSET); // todo: must be right time!
        };
    }

    private boolean checkRightDays(String dayOfWeek, List<String> expectWeekDaysList) {
        return expectWeekDaysList.isEmpty() || expectWeekDaysList.contains(dayOfWeek);
    }

    private boolean checkRightTime(int hourOfDay, Integer expectFromHr, Integer expectToHr) {
        return  ((expectFromHr < expectToHr) && (hourOfDay >= expectFromHr && hourOfDay <= expectToHr)) ||
                ((expectFromHr > expectToHr) && (hourOfDay >= expectFromHr || hourOfDay <= expectToHr));
    }

    private boolean checkRightWeek(int weekOfYear, int oldestWeek) {
        return weekOfYear >= oldestWeek;
    }


}