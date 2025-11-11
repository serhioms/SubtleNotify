package ru.alumni.hub.subtlenotify.types;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private String actionType;

}