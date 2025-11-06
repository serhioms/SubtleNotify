package ru.alumni.hub.subtlenotify.types;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.alumni.hub.subtlenotify.model.Actions;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActionResponse {
    private Long id;
    private String userId;
    private String actionType;
    private LocalDateTime timestamp;

    // Constructor to convert from Actions entity
    public ActionResponse(Actions action) {
        this.id = action.getId();
        this.userId = action.getUserId();
        this.actionType = action.getActionType();
        this.timestamp = action.getTimestamp();
    }

    // Static method to convert a single Actions entity
    public static ActionResponse fromEntity(Actions action) {
        return new ActionResponse(action);
    }

    // Static method to convert a list of Actions entities
    public static List<ActionResponse> fromEntityList(List<Actions> actions) {
        return actions.stream()
                .map(ActionResponse::new)
                .toList();
    }
}