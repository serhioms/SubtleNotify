package ru.alumni.hub.subtlenotify.types;

import io.micrometer.common.util.StringUtils;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TriggerRequest {

    @NotNull(message = "triggerIdent is required")
    private String triggerIdent;
    private String triggerDescr;

    @NotNull(message = "notifIdent is required")
    private String notifIdent;
    private String notifDescr;

    @NotNull(message = "notifMoment is required")
    private NotificationMoment notifMoment;

    private Integer expectEveryDays;
    private String expectWeekDays;
    private Integer expectHowOften;

    private Integer expectFromHr;
    private Integer expectToHr;

    private String actualWeekDays;
    private String actualHours;

    private Boolean missYesterday;

    // Frequently used functions and defaults

    public int getExpectHowOften() {
        return expectHowOften == null? 0: expectHowOften;
    }

    public int getExpectFromHr() {
        return expectFromHr == null? 0: expectFromHr;
    }

    public int getExpectToHr() {
        return expectToHr == null? 24: expectToHr;
    }

    public Boolean getMissYesterday() {
        return missYesterday != null && missYesterday;
    }

    public List<String> getExpectWeekDaysList() {
        return expectWeekDays == null || expectWeekDays.isBlank()? List.of(): List.of(expectWeekDays.split(","));
    }

    public boolean isExpectWeekDays() {
        return !StringUtils.isBlank(expectWeekDays);
    }

    public String getExpectWeekDay() {
        return StringUtils.isBlank(expectWeekDays)? null: expectWeekDays.split(",")[0];
    }

    public List<String> getActualWeekDaysList() {
        return StringUtils.isBlank(actualWeekDays)? List.of(): List.of(actualWeekDays.split(","));
    }

    public boolean isActualWeekDays() {
        return !StringUtils.isBlank(actualWeekDays);
    }

    public String getActualWeekDay() {
        return StringUtils.isBlank(actualWeekDays)? null: actualWeekDays.split(",")[0];
    }

    public List<String> getActualHoursList() {
        return actualHours == null || actualHours.isBlank()? List.of(): List.of(actualHours.split(","));
    }

    public boolean isActualHours() {
        return !StringUtils.isBlank(actualHours);
    }

    public int getActualHour() {
        return StringUtils.isBlank(actualHours)? null: Integer.parseInt(actualHours.split(",")[0]);
    }

}