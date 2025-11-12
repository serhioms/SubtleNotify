package ru.alumni.hub.subtlenotify.types;

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

    private Integer expectEveryDays;
    private String expectWeekDays;
    private Integer expectHowOften;

    private Integer expectFromHr;
    private Integer expectToHr;

    private String actualWeekDays;
    private String actualHours;

    private Boolean missPreviousTime;
}