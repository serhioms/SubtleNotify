package ru.alumni.hub.subtlenotify.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import ru.alumni.hub.subtlenotify.types.NotifyMoment;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "triggers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"notifyMessage", "actionType"})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Trigger {

    @Id
    @GeneratedValue
    @JsonIgnore
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_ident", referencedColumnName = "ident", nullable = false)
    @NotNull(message = "notifyMessage is required")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private NotifyMessage notifyMessage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "action_type", referencedColumnName = "action_type", nullable = false)
    @NotNull(message = "actionType is required")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private ActionType actionType;

    @Column(name = "descr")
    private String descr;

    @Column(name = "expect_week_days")
    private String expectWeekDays;

    @Column(name = "expect_every_days")
    private Integer expectEveryDays;

    @Column(name = "expect_how_often")
    private Integer expectHowOften;

    @Column(name = "expect_from_hr")
    private Integer expectFromHr;

    @Column(name = "expect_to_hr")
    private Integer expectToHr;

    @Column(name = "actual_hours")
    private String actualHours;

    @Column(name = "actual_week_days")
    private String actualWeekDays;

    @Column(name = "miss_previous_time", nullable = false)
    private Boolean missPreviousTime = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "notify_moment", nullable = false)
    @NotNull(message = "notifyMoment is required")
    private NotifyMoment notifyMoment;

    @PrePersist
    protected void onCreate() {
        if (missPreviousTime == null) {
            missPreviousTime = false;
        }
    }

    // Custom getters for JSON serialization

    @JsonProperty("notifyMessage")
    public String getMessage() {
        return notifyMessage != null ? notifyMessage.getMessage() : null;
    }

    @JsonProperty("actionType")
    public String getActionTypeName() {
        return actionType != null ? actionType.getActionType() : null;
    }
}