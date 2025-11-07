package ru.alumni.hub.subtlenotify.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.alumni.hub.subtlenotify.model.Action;
import ru.alumni.hub.subtlenotify.service.ActionService;
import ru.alumni.hub.subtlenotify.service.SubtleNotifyService;
import ru.alumni.hub.subtlenotify.service.TriggerService;
import ru.alumni.hub.subtlenotify.types.ActionRequest;
import ru.alumni.hub.subtlenotify.types.ActionResponse;
import ru.alumni.hub.subtlenotify.types.NotificationResponse;
import ru.alumni.hub.subtlenotify.types.TriggerRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/subtlenotify")
@RequiredArgsConstructor
public class SubtleNotifyController {

    private final ActionService actionService;
    private final TriggerService triggerService;
    private final SubtleNotifyService subtleNotifyService;

    private static final List<NotificationResponse> notifications = new ArrayList<NotificationResponse>(64);

    @PostMapping("/action")
    public ResponseEntity<Map<String, Object>> createAction(@Valid @RequestBody ActionRequest actionRequest) {
        // Process the action
        Action action = actionService.storeAction(actionRequest);

        // Add notification if presented
        subtleNotifyService.processAction(action).ifPresent(notifications::add);

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
        List<ActionResponse>  actionResponses = actionService.getAllActions();
        return ResponseEntity.ok(actionResponses);
    }

    @GetMapping("/notifications")
    public ResponseEntity<List<NotificationResponse>> getNotifications() {
        return ResponseEntity.ok(notifications);
    }

    @PostMapping("/trigger")
    public void trigger(@Valid @RequestBody TriggerRequest triggerRequest) {
        triggerService.storeTrigger(triggerRequest);
    }

    @GetMapping("/triggers")
    public ResponseEntity<List<TriggerRequest>> triggers() {
        return ResponseEntity.status(HttpStatus.CREATED).body(triggerService.getAllTriggers());
    }


}