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
@Table(name = "action")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "user")
public class Action {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", nullable = false)
    @NotNull(message = "user is required")
    private User user;

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