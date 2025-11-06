package ru.alumni.hub.subtlenotify.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.alumni.hub.subtlenotify.service.ActionsService;
import ru.alumni.hub.subtlenotify.types.ActionRequest;
import ru.alumni.hub.subtlenotify.types.ActionResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/subtlenotify")
@RequiredArgsConstructor
public class SubtleNotifyController {

    private final ActionsService actionsService;

    @PostMapping("/action")
    public ResponseEntity<Map<String, Object>> createAction(@Valid @RequestBody ActionRequest actionRequest) {
        // Process the action
        actionsService.storeAction(actionRequest);

        Map<String, Object> response = Map.of(
                "status", "success",
                "message", "Action created successfully",
                "data", Map.of(
                        "userId", actionRequest.getUserId(),
                        "actionType", actionRequest.getActionType(),
                        "timestamp", actionRequest.getTimestamp().toString()
                )
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/actions")
    public ResponseEntity<List<ActionResponse>> getAllActions() {
        List<ActionResponse>  actionResponses = actionsService.getAllActions();
        return ResponseEntity.ok(actionResponses);
    }

    @GetMapping("/notifications")
    public ResponseEntity<List<Map<String, Object>>> getNotifications() {
        List<Map<String, Object>> notifications = List.of(
                Map.of(
                        "id", 1,
                        "title", "Welcome to SubtleNotify",
                        "message", "Your application is running successfully",
                        "timestamp", LocalDateTime.now().minusHours(2).toString(),
                        "read", false
                ),
                Map.of(
                        "id", 2,
                        "title", "System Update",
                        "message", "New features are available",
                        "timestamp", LocalDateTime.now().minusHours(1).toString(),
                        "read", false
                ),
                Map.of(
                        "id", 3,
                        "title", "Action Completed",
                        "message", "Your last action was executed successfully",
                        "timestamp", LocalDateTime.now().minusMinutes(30).toString(),
                        "read", true
                )
        );
        return ResponseEntity.ok(notifications);
    }
}