package ru.alumni.hub.subtlenotify.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
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
}