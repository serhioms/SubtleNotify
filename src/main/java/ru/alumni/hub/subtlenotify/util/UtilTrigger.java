package ru.alumni.hub.subtlenotify.util;

import io.micrometer.common.util.StringUtils;
import ru.alumni.hub.subtlenotify.model.Trigger;

import java.util.List;

public class UtilTrigger {

    public static Integer getExpectHowOften(Trigger trigger) {
        return trigger.getExpectHowOften() == null? 0: trigger.getExpectHowOften();
    }

    public static Integer getExpectFromHr(Trigger trigger) {
        return trigger.getExpectFromHr() == null? 0: trigger.getExpectFromHr();
    }

    public static Integer getExpectToHr(Trigger trigger) {
        return trigger.getExpectFromHr() == null? 24: trigger.getExpectFromHr();
    }

    public static Boolean isMissPreviousTime(Trigger trigger) {
        return trigger.getMissPreviousTime() != null && trigger.getMissPreviousTime();
    }

    public static List<String> getExpectWeekDaysList(Trigger trigger) {
        return StringUtils.isBlank(trigger.getExpectWeekDays())? List.of(): List.of(trigger.getExpectWeekDays().split(","));
    }

    public static boolean isExpectWeekDays(Trigger trigger) {
        return !StringUtils.isBlank(trigger.getExpectWeekDays());
    }

    public static String getExpectWeekDay(Trigger trigger) {
        return StringUtils.isBlank(trigger.getExpectWeekDays())? null: trigger.getExpectWeekDays().split(",")[0];
    }

    public static List<String> getActualWeekDaysList(Trigger trigger) {
        return StringUtils.isBlank(trigger.getActualHours())? List.of(): List.of(trigger.getActualHours().split(","));
    }

    public static boolean isActualWeekDays(Trigger trigger) {
        return !StringUtils.isBlank(trigger.getActualWeekDays());
    }

    public static String getActualWeekDay(Trigger trigger) {
        return StringUtils.isBlank(trigger.getActualHours())? null: trigger.getActualHours().split(",")[0];
    }

    public static List<String> getActualHoursList(Trigger trigger) {
        return StringUtils.isBlank(trigger.getActualHours())? List.of(): List.of(trigger.getActualHours().split(","));
    }

    public static boolean isActualHours(Trigger trigger) {
        return !StringUtils.isBlank(trigger.getActualHours());
    }

    public static Integer getActualHour(Trigger trigger) {
        return StringUtils.isBlank(trigger.getActualHours())? null: Integer.parseInt(trigger.getActualHours().split(",")[0]);
    }
}
