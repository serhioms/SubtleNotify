package ru.alumni.hub.subtlenotify.service;

import io.micrometer.common.util.StringUtils;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.alumni.hub.subtlenotify.exception.SubtleNotifyException;
import ru.alumni.hub.subtlenotify.health.ActionsMetrics;
import ru.alumni.hub.subtlenotify.model.Action;
import ru.alumni.hub.subtlenotify.types.NotificationResponse;
import ru.alumni.hub.subtlenotify.types.TriggerRequest;
import ru.alumni.hub.subtlenotify.util.UtilAction;
import ru.alumni.hub.subtlenotify.util.UtilTriggerRequest;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubtleNotifyService {

    Logger LOGGER = LoggerFactory.getLogger(SubtleNotifyService.class);

    private static final List<NotificationResponse> notifications = new ArrayList<NotificationResponse>(64);

    public static final int NOTIFICATION_MINUTES_OFFSET = 10;

    private final ActionsMetrics actionsMetrics;
    private final ActionService actionService;
    private final TriggerServiceOld triggerServiceOld;
    private final NotificationService notificationService;

    public void generateNotification(Action action) {
        var timer = actionsMetrics.startTimer();
        try {
            var triggers = triggerServiceOld.getTriggersByIdent(action.getActionType().getActionType());
            if( !triggers.isEmpty() ) {
                for (TriggerRequest trigger : triggers) {
                    if(UtilTriggerRequest.isMissPreviousTime(trigger) ) {
                        generateMissedNotification(action, trigger).ifPresentOrElse( this::storeNotification, ()->{});
                    } else {
                        generateNormalNotification(action, trigger).ifPresentOrElse( this::storeNotification, ()->{});
                    }
                }
            } else {
                throw new RuntimeException("No triggers found for action: " + action);
            }
        } catch (Exception e) {
            LOGGER.error("Error while generating notification for action: " + action, e);
        } finally {
            actionsMetrics.incrementActionsCreated();
            actionsMetrics.recordCreationTime(timer);
        }
    }

    public Optional<NotificationResponse> generateMissedNotification(Action action, TriggerRequest trigger) throws SubtleNotifyException {
        if( trigger.getExpectEveryDays() != null) {
            // check if you missed action yesterday
            action.setTimestamp(UtilAction.minusDays(action, trigger.getExpectEveryDays()));
            if (generateNormalNotification(action, trigger).isEmpty()) {
                // check if you had notification the day before yesterday
                action.setTimestamp(UtilAction.minusDays(action, trigger.getExpectEveryDays()));
                Optional<NotificationResponse> notification = generateNormalNotification(action, trigger);
                if (notification.isPresent()) {
                    // yeah, you missed it yesterday, and today you can't have notification then will generate it for tomorrow!
                    notification.get().setTimestamp(notification.get().getTimestamp().plusDays(2L*trigger.getExpectEveryDays()));
                    return notification;
                }
            }
        } else if( trigger.getExpectWeekDays() != null) {
            // check if you missed action the previous week
            action.setTimestamp(UtilAction.minusWeek(action));
            if (generateNormalNotification(action, trigger).isEmpty()) {
                // check if you had notification the week ago
                action.setTimestamp(UtilAction.minusWeek(action));
                Optional<NotificationResponse> notification = generateNormalNotification(action, trigger);
                if (notification.isPresent()) {
                    // yeah, you missed it two weeks ago, and today you can't have notification then will generate it for next week!
                    notification.get().setTimestamp(notification.get().getTimestamp().plusWeeks(2L));
                    return notification;
                }
            }
        }
        return Optional.empty();
      }

    private Optional<NotificationResponse> generateNormalNotification(Action action, TriggerRequest trigger) throws SubtleNotifyException {
        int curWeekOfYear = UtilAction.weekOfYear(action);
        int curDayOfYear = action.getDayOfYear();
        int curHourOfDay = UtilAction.getHour(action);

        List<Integer> expectedDayList = getDaysInTriggerScope(curDayOfYear, trigger);
        List<Integer> expectedWeekList = getWeeksInTriggerScope(curWeekOfYear, trigger);

        if( expectedDayList.isEmpty() && expectedWeekList.isEmpty()){
            return Optional.empty(); // no statistics for today
        }

        // select actions from DB for the last N days or weeks according to the trigger scope ordered by timestamp
        List<Action> actions = actionService.getUserActions(action.getUser(), action.getActionType(), expectedDayList, expectedWeekList);

        // filter actions by expected date and time and once for the day
        List<Integer> onceForDay = new ArrayList<>(64);
        List<Action> selectedActions = actions.stream()
                .filter(a -> checkRightTime(UtilAction.getHour(a), trigger.getExpectFromHr(), trigger.getExpectToHr()) ||  checkRightTime(UtilAction.getHour(a), UtilTriggerRequest.getActualHoursList(trigger)))
                .filter(a -> checkRightDays(UtilAction.getDayOfWeek(a), UtilTriggerRequest.getExpectWeekDaysList(trigger)) || checkRightDays(UtilAction.getDayOfWeek(a), UtilTriggerRequest.getActualWeekDaysList(trigger)))
                .filter(a -> isOnceForDay(onceForDay, a.getTimestamp().getDayOfYear()))
                .toList();

        // filter actions by pattern
        if (selectedActions.isEmpty()) {
            return Optional.empty();
        } else if (trigger.getExpectWeekDays() != null) { // check actions for a week days pattern aka sun, mon ...
            if ( !( trigger.getExpectWeekDays().equals(selectedActions.stream().map(UtilAction::getDayOfWeek).distinct().collect(Collectors.joining(",")))
                && trigger.getExpectHowOften() == selectedActions.stream().map(Action::getWeekOfYear).distinct().collect(Collectors.toSet()).size()) ) {
                return Optional.empty();
            }
        } else if (trigger.getExpectEveryDays() != null) {  // check actions for every N days pattern aka every 1 day, every 2 days etc
            int expectDays = trigger.getExpectEveryDays() * trigger.getExpectHowOften();
            Set<Integer> selectDays = selectedActions.stream().map(Action::getDayOfYear).collect(Collectors.toSet());
            for (int day = curDayOfYear, step = trigger.getExpectEveryDays(), min = curDayOfYear - expectDays; day > min; day -= step) {
                if (!selectDays.contains(day)) {
                    return Optional.empty();
                }
            }
        }

        // filter by actual hours
        if (!StringUtils.isBlank(trigger.getActualHours()) && UtilTriggerRequest.getActualWeekDaysList(trigger).contains(UtilAction.getDayOfWeek(action))) {
            if (UtilTriggerRequest.getActualHoursList(trigger).stream().map(Integer::valueOf).noneMatch(h -> h == curHourOfDay)) {
                return Optional.empty();
            }
        }

        // return notification
        return Optional.of(new NotificationResponse(notificationDateTime(action, trigger), trigger.getNotifDescr(), action.getActionType()));
    }

    private boolean isOnceForDay(List<Integer> onceForDay, Integer dayOfYear) {
        if( onceForDay.contains(dayOfYear) ) {
            return false;
        }
        onceForDay.add(dayOfYear);
        return true;
    }

    private List<Integer> getWeeksInTriggerScope(int curWeekOfYear, TriggerRequest trigger) {
        List<Integer> list = new ArrayList<>(16);
        if( trigger.getExpectWeekDays() != null ) {
            int expectWeeks = trigger.getExpectHowOften();
            for (int week = curWeekOfYear, min = curWeekOfYear - expectWeeks; week > min; week--) {
                list.add(week);
            }
        }
        return list;
    }

    private List<Integer> getDaysInTriggerScope(int curDayOfYear, TriggerRequest trigger) {
        List<Integer> list = new ArrayList<>(64);
        if( trigger.getExpectEveryDays() != null ) {
            int expectDays = trigger.getExpectEveryDays()*trigger.getExpectHowOften();
            for (int day = curDayOfYear, step = trigger.getExpectEveryDays(), min = curDayOfYear - expectDays; day > min; day-=step) {
                list.add(day);
            }
        }
        return list;
    }

    private @NotNull(message = "timestamp is required") LocalDateTime notificationDateTime(Action action, TriggerRequest trigger) {
        return switch(trigger.getNotifMoment()) {
            case immediately -> action.getTimestamp()
                    .withSecond(0)
                    .plusMinutes(NOTIFICATION_MINUTES_OFFSET);
            case next_time -> notificationNextDayTime(action, trigger);
        };
    }

    private @NotNull(message = "timestamp is required") LocalDateTime notificationNextDayTime(Action action, TriggerRequest trigger) {
        if( trigger.getExpectEveryDays() != null ) {
            return action.getTimestamp()
                    .plusDays(trigger.getExpectEveryDays())
                    .withHour(UtilTriggerRequest.isActualHours(trigger) ? UtilTriggerRequest.getActualHour(trigger) : trigger.getExpectFromHr())
                    .withMinute(0)
                    .withSecond(0)
                    .minusMinutes(NOTIFICATION_MINUTES_OFFSET);
        } else if(trigger.getExpectWeekDays() != null ) {
            return (switch(Objects.requireNonNull(UtilTriggerRequest.isActualWeekDays(trigger) ? UtilTriggerRequest.getActualWeekDay(trigger) : UtilTriggerRequest.getExpectWeekDay(trigger))){
                case "MON" ->  action.getTimestamp().plusWeeks(1).with(DayOfWeek.MONDAY);
                case "TUE" ->  action.getTimestamp().plusWeeks(1).with(DayOfWeek.TUESDAY);
                case "WED" ->  action.getTimestamp().plusWeeks(1).with(DayOfWeek.WEDNESDAY);
                case "THU" ->  action.getTimestamp().plusWeeks(1).with(DayOfWeek.THURSDAY);
                case "FRI" ->  action.getTimestamp().plusWeeks(1).with(DayOfWeek.FRIDAY);
                case "SAT" ->  action.getTimestamp().plusWeeks(1).with(DayOfWeek.SATURDAY);
                case "SUN" ->  action.getTimestamp().plusWeeks(1).with(DayOfWeek.SUNDAY);
                default -> throw new IllegalStateException("Unexpected day of week: '" + (trigger.getActualWeekDays() != null?  UtilAction.getDayOfWeek(action): UtilTriggerRequest.getExpectWeekDay(trigger)));
            })
                    .withHour( UtilTriggerRequest.isActualHours(trigger)? UtilTriggerRequest.getActualHour(trigger) : UtilAction.getHour(action))
                    .withMinute(0)
                    .withSecond(0);
        } else {
            throw new RuntimeException("Trigger should have either expectEveryDays or expectWeekDays");
        }
    }

    private boolean checkRightDays(String dayOfWeek, List<String> expectWeekDaysList) {
        return expectWeekDaysList.isEmpty() || expectWeekDaysList.contains(dayOfWeek);
    }

    private boolean checkRightTime(int hourOfDay, Integer expectFromHr, Integer expectToHr) {
        return  ((expectFromHr < expectToHr) && (hourOfDay >= expectFromHr && hourOfDay <= expectToHr)) ||
                ((expectFromHr > expectToHr) && (hourOfDay >= expectFromHr || hourOfDay <= expectToHr));
    }

    private boolean checkRightTime(int hour, List<String> actualHoursList) {
        return actualHoursList.isEmpty() || actualHoursList.contains(String.valueOf(hour));
    }

    private boolean checkRightWeek(int weekOfYear, int oldestWeek) {
        return weekOfYear >= oldestWeek;
    }


    public List<NotificationResponse> retrieveNotifications() {
        return notifications;
    }

    public void deleteAllNotifications() {
        notifications.clear();
    }

    private void storeNotification(@NotNull NotificationResponse notificationResponse) {
        notifications.stream()
                .filter(n -> n.getTimestamp().getDayOfYear() == notificationResponse.getTimestamp().getDayOfYear())
                .filter(n -> n.getActionType().getActionType().equals(notificationResponse.getActionType().getActionType()))
                .findFirst()
                .ifPresentOrElse(n -> {},
                        () -> notifications.add(notificationResponse) // do not duplicate notifications!
        );
    }



}