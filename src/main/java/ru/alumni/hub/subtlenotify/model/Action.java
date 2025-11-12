package ru.alumni.hub.subtlenotify.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.UUID;


@Entity
@Table(name = "actions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"user", "actionType"})
public class Action {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", nullable = false)
    @NotNull(message = "user is required")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "action_type", referencedColumnName = "action_type", nullable = false)
    @NotNull(message = "actionType is required")
    private ActionType actionType;

    @NotNull(message = "timestamp is required")
    @Column(nullable = false)
    private LocalDateTime timestamp;

    @NotNull(message = "dayOfYear is required")
    @Column(name = "day_of_year", nullable = false)
    private Integer dayOfYear;

    @NotNull(message = "weekOfYear is required")
    @Column(name = "week_of_year", nullable = false)
    private Integer weekOfYear;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}