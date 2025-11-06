package ru.alumni.hub.subtlenotify.types;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    private Integer expectWeeks;
    private String expectWeekDays;
    private Integer expectStraightDays;
    private Integer expectEveryDays;

    private Integer expectFromHr;
    private Integer expectToHr;

    private String actualWeekDays;
    private String actualHr;

    private Boolean actualMiss;

    public Integer getExpectWeeks() {
        return expectWeeks == null? 2: expectWeeks;
    }

    public Integer getExpectEveryDays() {
        return expectEveryDays == null? 0: expectEveryDays;
    }

    public Integer getExpectFromHr() {
        return expectFromHr == null? 0: expectFromHr;
    }

    public Integer getExpectToHr() {
        return expectToHr == null? 23: expectToHr;
    }

    public Boolean getActualMiss() {
        return actualMiss == null? false: actualMiss;
    }
}