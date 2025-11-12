package ru.alumni.hub.subtlenotify.util;

import ru.alumni.hub.subtlenotify.model.Action;

import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.Locale;

public class UtilAction {

    public static String getDayOfWeek(Action action){
        return action.getTimestamp().getDayOfWeek().name().substring(0,3);
    }

    public static int weekOfYear(Action action){
        return action.getTimestamp().get(WeekFields.of(Locale.getDefault()).weekOfYear());
    }

    public static int getDayOfYear(Action action){
        return action.getTimestamp().getDayOfYear();
    }

    public static int getHour(Action action){
        return action.getTimestamp().getHour();
    }

    public static LocalDateTime minusWeek(Action action) {
        return action.getTimestamp().minusWeeks(1);
    }

    public static LocalDateTime minusDays(Action action, int days) {
        return action.getTimestamp().minusDays(days);
    }

}
