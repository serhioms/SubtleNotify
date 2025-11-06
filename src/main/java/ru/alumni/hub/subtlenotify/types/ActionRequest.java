package ru.alumni.hub.subtlenotify.types;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActionRequest {
    @NotNull(message = "userId is required")
    private String userId;

    @NotNull(message = "actionType is required")
    private String actionType;

    @NotNull(message = "timestamp is required")
    private LocalDateTime timestamp;
}