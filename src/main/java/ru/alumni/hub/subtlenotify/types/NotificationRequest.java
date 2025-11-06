package ru.alumni.hub.subtlenotify.types;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {

    @NotNull(message = "ident is required")
    private String ident;

    @NotNull(message = "descr is required")
    private String descr;
}