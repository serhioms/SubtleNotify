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
import java.util.UUID;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"notifyMessage", "user", "actionType"})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Notification {

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
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", nullable = false)
    @NotNull(message = "user is required")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "action_type", referencedColumnName = "action_type", nullable = false)
    @NotNull(message = "actionType is required")
    @JsonIgnore
    private ActionType actionType;

    @NotNull(message = "timestamp is required")
    @Column(nullable = false)
    private LocalDateTime timestamp;

    @NotNull(message = "dayOfYear is required")
    @Column(nullable = false)
    @JsonIgnore
    private Integer dayOfYear;

    @PrePersist
    protected void onCreate() {
        dayOfYear = timestamp.getDayOfYear();
    }

    // Custom getters for JSON serialization
    @JsonProperty("user")
    public String getUserId() {
        return user != null ? user.getUserId() : null;
    }

    @JsonProperty("notifyMessage")
    public String getMessage() {
        return notifyMessage != null ? notifyMessage.getMessage() : null;
    }
}