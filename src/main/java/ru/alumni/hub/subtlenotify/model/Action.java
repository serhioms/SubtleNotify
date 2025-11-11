package ru.alumni.hub.subtlenotify.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.Locale;
import java.util.UUID;


@Entity
@Table(name = "action")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Action {
    @Id
    @GeneratedValue
    private UUID id;

    @NotNull(message = "userId is required")
    @Column(name = "user_id", nullable = false)
    private String userId;

    @NotNull(message = "actionType is required")
    @Column(name = "action_type", nullable = false)
    private String actionType;

    @NotNull(message = "timestamp is required")
    @Column(nullable = false)
    private LocalDateTime timestamp;

    @NotNull(message = "dayOfYear is required")
    @Column(nullable = false)
    private Integer dayOfYear;

    @NotNull(message = "weekOfYear is required")
    @Column(nullable = false)
    private Integer weekOfYear;

    // Frequently used functions

    public String getDayOfWeek(){
        return timestamp.getDayOfWeek().name().substring(0,3);
    }

    public int weekOfYear(){
        return timestamp.get(WeekFields.of(Locale.getDefault()).weekOfYear());
    }

    public int getDayOfYear(){
        return timestamp.getDayOfYear();
    }

    public int getHour(){
        return timestamp.getHour();
    }
}