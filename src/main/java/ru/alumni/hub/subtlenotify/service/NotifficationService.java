package ru.alumni.hub.subtlenotify.service;

import io.micrometer.common.util.StringUtils;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.alumni.hub.subtlenotify.health.ActionsMetrics;
import ru.alumni.hub.subtlenotify.model.Action;
import ru.alumni.hub.subtlenotify.types.NotificationResponse;
import ru.alumni.hub.subtlenotify.types.TriggerRequest;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotifficationService {

    private static final List<NotificationResponse> notifications = new ArrayList<NotificationResponse>(64);

    public static final int NOTIFICATION_MINUTES_OFFSET = 10;

    private final ActionsMetrics actionsMetrics;
    private final ActionService actionService;
    private final TriggerService triggerService;

    @Transactional
    public void generateNotification(Action action) {
        var timer = actionsMetrics.startTimer();
        try {
            var triggers = triggerService.getTriggersByIdent(action.getActionType());
                if( !triggers.isEmpty() ) {
                    for (TriggerRequest trigger : triggers) {
                        int curWeekOfYear = action.weekOfYear();
                        int curDayOfYear = action.getDayOfYear();
                        int curHourOfDay = action.getHour();

                        List<Integer> expectedDayList = getDaysInTriggerScope(curDayOfYear, trigger);
                        List<Integer> expectedWeekList = getWeeksInTriggerScope(curWeekOfYear, trigger);

                        // select actions from DB for the last N days or weeks according to the trigger scope ordered by timestamp
                        List<Action> actions = actionService.getUserActions(action.getUserId(), action.getActionType(), expectedDayList, expectedWeekList);

                        // filter actions by expected date and time and once for the day
                        List<Integer> onceForDay = new ArrayList<>(64);
                        List<Action> selectedActions = actions.stream()
                                .filter(a -> checkRightTime(a.getHour(), trigger.getExpectFromHr(), trigger.getExpectToHr()) ||  checkRightTime(a.getHour(), trigger.getActualHoursList()))
                                .filter(a -> checkRightDays(a.getDayOfWeek(), trigger.getExpectWeekDaysList()) || checkRightDays(a.getDayOfWeek(), trigger.getActualWeekDaysList()))
                                .filter(a -> isOnceForDay(onceForDay, a.getTimestamp().getDayOfYear()))
                                .toList();

                        // filter actions by pattern
                        if (selectedActions.isEmpty()) {
                            return;
                        } else if (trigger.getExpectWeekDays() != null) { // check actions for a week days pattern aka sun, mon ...
                            if (!(trigger.getExpectWeekDays().equals(selectedActions.stream().map(Action::getDayOfWeek).distinct().collect(Collectors.joining(",")))
                                    && selectedActions.size() == trigger.getExpectHowOften())) {
                                return;
                            }
                        } else if (trigger.getExpectEveryDays() != null) {  // check actions for every N days pattern aka every 1 day, every 2 days etc
                            int expectDays = trigger.getExpectEveryDays() * trigger.getExpectHowOften();
                            Set<Integer> selectDays = selectedActions.stream().map(Action::getDayOfYear).collect(Collectors.toSet());
                            for (int day = curDayOfYear, step = trigger.getExpectEveryDays(), min = curDayOfYear - expectDays; day > min; day -= step) {
                                if (!selectDays.contains(day)) {
                                    return;
                                }
                            }
                            // check if you missed action yesterday
                            if( trigger.getMissYesterday() ){

                            }
                        }

                        // filter by actual hours
                        if (!StringUtils.isBlank(trigger.getActualHours()) && trigger.getActualWeekDaysList().contains(action.getDayOfWeek())) {
                            if (trigger.getActualHoursList().stream().map(Integer::valueOf).noneMatch(h -> h == curHourOfDay)) {
                                return;
                            }
                        }

                        // store notification
                        storeNotification(new NotificationResponse(notificationDateTime(action, trigger), trigger.getNotifDescr(), action.getActionType()));
                    }
                } else {
                    System.out.println("No triggers found for action: " + action);
                }
        } catch (Exception e) {
            throw e;
        } finally {
            actionsMetrics.incrementActionsCreated();
            actionsMetrics.recordCreationTime(timer);
        }
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
                    .withHour(trigger.isActualHours() ? trigger.getActualHour() : trigger.getExpectFromHr())
                    .withMinute(0)
                    .withSecond(0)
                    .minusMinutes(NOTIFICATION_MINUTES_OFFSET);
        } else if(trigger.getExpectWeekDays() != null ) {
            return (switch( trigger.isActualWeekDays()? trigger.getActualWeekDay(): trigger.getExpectWeekDay()){
                case "MON" ->  action.getTimestamp().plusWeeks(1).with(DayOfWeek.MONDAY);
                case "TUE" ->  action.getTimestamp().plusWeeks(1).with(DayOfWeek.TUESDAY);
                case "WED" ->  action.getTimestamp().plusWeeks(1).with(DayOfWeek.WEDNESDAY);
                case "THU" ->  action.getTimestamp().plusWeeks(1).with(DayOfWeek.THURSDAY);
                case "FRI" ->  action.getTimestamp().plusWeeks(1).with(DayOfWeek.FRIDAY);
                case "SAT" ->  action.getTimestamp().plusWeeks(1).with(DayOfWeek.SATURDAY);
                case "SUN" ->  action.getTimestamp().plusWeeks(1).with(DayOfWeek.SUNDAY);
                default -> throw new IllegalStateException("Unexpected day of week: '" + (trigger.getActualWeekDays() != null?  action.getDayOfWeek(): trigger.getExpectWeekDay()));
            })
                    .withHour( trigger.isActualHours()? trigger.getActualHour() : action.getHour())
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


    public List<NotificationResponse> getNotifications() {
        return notifications;
    }

    public void deleteAllNotifications() {
        notifications.clear();
    }

    private void storeNotification(@NotNull NotificationResponse notificationResponse) {
        System.out.println("Notification stored: " + notificationResponse);
        notifications.stream()
                .filter(n -> n.getTimestamp().getDayOfYear() == notificationResponse.getTimestamp().getDayOfYear())
                .filter(n -> n.getActionType().equals(notificationResponse.getActionType()))
                .findFirst()
                .ifPresentOrElse(n -> {},
                        () -> notifications.add(notificationResponse) // do not duplicate notifications!
        );
    }

}