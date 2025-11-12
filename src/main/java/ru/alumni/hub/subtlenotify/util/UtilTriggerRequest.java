package ru.alumni.hub.subtlenotify.util;

import io.micrometer.common.util.StringUtils;
import ru.alumni.hub.subtlenotify.types.TriggerRequest;

import java.util.List;

public class UtilTriggerRequest {

    public static Integer getExpectHowOften(TriggerRequest request) {
        return request.getExpectHowOften() == null? 0: request.getExpectHowOften();
    }

    public static Integer getExpectFromHr(TriggerRequest request) {
        return request.getExpectFromHr() == null? 0: request.getExpectFromHr();
    }

    public static Integer getExpectToHr(TriggerRequest request) {
        return request.getExpectFromHr() == null? 24: request.getExpectFromHr();
    }

    public static Boolean isMissPreviousTime(TriggerRequest request) {
        return request.getMissPreviousTime() != null && request.getMissPreviousTime();
    }

    public static List<String> getExpectWeekDaysList(TriggerRequest request) {
        return StringUtils.isBlank(request.getExpectWeekDays())? List.of(): List.of(request.getExpectWeekDays().split(","));
    }

    public static boolean isExpectWeekDays(TriggerRequest request) {
        return !StringUtils.isBlank(request.getExpectWeekDays());
    }

    public static String getExpectWeekDay(TriggerRequest request) {
        return StringUtils.isBlank(request.getExpectWeekDays())? null: request.getExpectWeekDays().split(",")[0];
    }

    public static List<String> getActualWeekDaysList(TriggerRequest request) {
        return StringUtils.isBlank(request.getActualHours())? List.of(): List.of(request.getActualHours().split(","));
    }

    public static boolean isActualWeekDays(TriggerRequest request) {
        return !StringUtils.isBlank(request.getActualWeekDays());
    }

    public static String getActualWeekDay(TriggerRequest request) {
        return StringUtils.isBlank(request.getActualHours())? null: request.getActualHours().split(",")[0];
    }

    public static List<String> getActualHoursList(TriggerRequest request) {
        return StringUtils.isBlank(request.getActualHours())? List.of(): List.of(request.getActualHours().split(","));
    }

    public static boolean isActualHours(TriggerRequest request) {
        return !StringUtils.isBlank(request.getActualHours());
    }

    public static Integer getActualHour(TriggerRequest request) {
        return StringUtils.isBlank(request.getActualHours())? null: Integer.parseInt(request.getActualHours().split(",")[0]);
    }
}
