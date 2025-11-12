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

import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.Locale;
import java.util.UUID;


@Entity
@Table(name = "actions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"user", "actionType"})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Action {
    @Id
    @GeneratedValue
    @JsonIgnore
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", nullable = false)
    @NotNull(message = "user is required")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "action_type", referencedColumnName = "action_type", nullable = false)
    @NotNull(message = "actionType is required")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private ActionType actionType;

    @NotNull(message = "timestamp is required")
    @Column(nullable = false)
    private LocalDateTime timestamp;

    @NotNull(message = "dayOfYear is required")
    @Column(name = "day_of_year", nullable = false)
    @JsonIgnore
    private Integer dayOfYear;

    @NotNull(message = "weekOfYear is required")
    @Column(name = "week_of_year", nullable = false)
    @JsonIgnore
    private Integer weekOfYear;


    @PrePersist
    protected void onCreate() {
        dayOfYear = timestamp.getDayOfYear();
        weekOfYear = timestamp.get(WeekFields.of(Locale.getDefault()).weekOfYear());
    }

    // Custom getters for JSON serialization

    @JsonProperty("user")
    public String getUserId() {
        return user != null ? user.getUserId() : null;
    }

    @JsonProperty("actionType")
    public String getActionTypeName() {
        return actionType != null ? actionType.getActionType() : null;
    }

}