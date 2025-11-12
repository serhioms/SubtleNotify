package ru.alumni.hub.subtlenotify.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
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
public class Trigger {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_ident", referencedColumnName = "ident", nullable = false)
    @NotNull(message = "notifyMessage is required")
    @JsonIgnore
    private NotifyMessage notifyMessage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "action_type", referencedColumnName = "action_type", nullable = false)
    @NotNull(message = "actionType is required")
    @JsonIgnore
    private ActionType actionType;

    @Column(name = "descr")
    private String descr;

    @Column(name = "expect_week_days")
    private String expectWeekDays;

    @Column(name = "expect_every_days")
    private String expectEveryDays;

    @Column(name = "expect_how_often")
    private Integer expectHowOften;

    @Column(name = "expect_from_hr")
    private Integer expectFromHr;

    @Column(name = "expect_to_hr")
    private Integer expectToHr;

    @Column(name = "miss_previous_time", nullable = false)
    private Boolean missPreviousTime = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "notify_moment", nullable = false)
    @NotNull(message = "notifyMoment is required")
    private NotifyMoment notifyMoment;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (missPreviousTime == null) {
            missPreviousTime = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        createdAt = LocalDateTime.now();
    }

}