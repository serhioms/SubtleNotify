package ru.alumni.hub.subtlenotify.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.alumni.hub.subtlenotify.model.ActionType;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    @NotNull(message = "timestamp is required")
    private LocalDateTime timestamp;

    @NotNull(message = "notification is required")
    private String notification;

    @NotNull(message = "actionType is required")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private ActionType actionType;

    // Custom getters for JSON serialization

    @JsonProperty("actionType")
    public String getActionTypeName() {
        return actionType != null ? actionType.getActionType() : null;
    }
}