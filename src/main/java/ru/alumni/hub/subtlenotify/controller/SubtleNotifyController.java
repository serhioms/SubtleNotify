package ru.alumni.hub.subtlenotify.controller;

import io.micrometer.common.util.StringUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.alumni.hub.subtlenotify.model.Action;
import ru.alumni.hub.subtlenotify.model.Notification;
import ru.alumni.hub.subtlenotify.model.Trigger;
import ru.alumni.hub.subtlenotify.service.*;
import ru.alumni.hub.subtlenotify.types.ActionRequest;
import ru.alumni.hub.subtlenotify.types.TriggerRequest;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/subtlenotify")
@RequiredArgsConstructor
public class SubtleNotifyController {

    private final ActionService actionService;
    private final TriggerService triggerService;
    private final SubtleNotifyService subtleNotifyService;
    private final NotificationService notificationService;

    @PostMapping("/action")
    public ResponseEntity<Map<String, Object>> createAction(@Valid @RequestBody ActionRequest actionRequest) {

        Optional<Action> action = actionService.storeAction(actionRequest);

        // Fire and forget - returns immediately
        action.ifPresent(subtleNotifyService::generateNotification);

        // Response sent before async method completes
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "status", "success",
                "message", "Action created successfully",
                "data", Map.of(
                        "userId", actionRequest.getUserId(),
                        "actionType", actionRequest.getActionType(),
                        "timestamp", actionRequest.getTimestamp().toString()
                )
        ));
    }

    @GetMapping("/actions")
    public ResponseEntity<List<Action>> getAllActions(@RequestParam(required = false) String userId, @RequestParam(required = false)  String actionType) {
        return ResponseEntity.ok(actionService.getAllActions(userId, actionType));
    }

    @GetMapping("/notifications")
    public ResponseEntity<List<Notification>> getNotifications() {
        return ResponseEntity.ok(notificationService.getAllNotifications());
    }

    @PostMapping("/trigger")
    public void trigger(@Valid @RequestBody TriggerRequest triggerRequest) {
        triggerService.storeTrigger(triggerRequest);
    }

    @GetMapping("/triggers")
    public ResponseEntity<List<Trigger>> triggers(@RequestParam(required = false)  String actionType) {
        if(StringUtils.isBlank(actionType)) {
            return ResponseEntity.status(HttpStatus.CREATED).body(triggerService.getAllTriggers());
        } else {
            return ResponseEntity.status(HttpStatus.CREATED).body(triggerService.getTriggersByActionType(actionType));
        }
    }


    @DeleteMapping("/clean")
    public ResponseEntity<Map<String, Object>> cleanDatabase() {
        actionService.deleteAllActions();
        notificationService.deleteAllNotifications();

        Map<String, Object> response = Map.of(
                "status", "success",
                "message", "Database cleaned successfully",
                "data", Map.of(
                        "actionsDeleted", true,
                        "notificationsDeleted", true
                )
        );
        return ResponseEntity.ok(response);
    }

}