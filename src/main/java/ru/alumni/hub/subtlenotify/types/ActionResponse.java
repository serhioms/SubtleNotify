package ru.alumni.hub.subtlenotify.types;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.alumni.hub.subtlenotify.model.Action;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActionResponse {
    private UUID id;
    private String userId;
    private String actionType;
    private LocalDateTime timestamp;

    // Constructor to convert from Actions entity
    public ActionResponse(Action action) {
        this.id = action.getId();
        this.userId = action.getUserId();
        this.actionType = action.getActionType();
        this.timestamp = action.getTimestamp();
    }

    // Static method to convert a single Actions entity
    public static ActionResponse fromEntity(Action action) {
        return new ActionResponse(action);
    }

    // Static method to convert a list of Actions entities
    public static List<ActionResponse> fromEntityList(List<Action> actions) {
        return actions.stream()
                .map(ActionResponse::new)
                .toList();
    }
}