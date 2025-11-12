package ru.alumni.hub.subtlenotify.controller;

import io.micrometer.common.util.StringUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.alumni.hub.subtlenotify.model.Action;
import ru.alumni.hub.subtlenotify.service.ActionService;
import ru.alumni.hub.subtlenotify.service.NotifficationService;
import ru.alumni.hub.subtlenotify.service.TriggerServiceOld;
import ru.alumni.hub.subtlenotify.types.ActionRequest;
import ru.alumni.hub.subtlenotify.types.NotificationResponse;
import ru.alumni.hub.subtlenotify.types.TriggerRequest;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/subtlenotify")
@RequiredArgsConstructor
public class SubtleNotifyController {

    private final ActionService actionService;
    private final TriggerServiceOld triggerServiceOld;
    private final NotifficationService notificationService;


    @PostMapping("/action")
    public ResponseEntity<Map<String, Object>> createAction(@Valid @RequestBody ActionRequest actionRequest) {

        Optional<Action> action = actionService.storeAction(actionRequest);

        action.ifPresent(notificationService::generateNotification);

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
    public ResponseEntity<List<Action>> getAllActions(@RequestParam(required = false) String userId, @RequestParam(required = false)  String actionType) {
        return ResponseEntity.ok(actionService.getActions(userId, actionType));
    }

    @GetMapping("/notifications")
    public ResponseEntity<List<NotificationResponse>> getNotifications() {
        return ResponseEntity.ok(notificationService.retrieveNotifications());
    }

    @PostMapping("/trigger")
    public void trigger(@Valid @RequestBody TriggerRequest triggerRequest) {
        triggerServiceOld.storeTrigger(triggerRequest);
    }

    @GetMapping("/triggers")
    public ResponseEntity<List<TriggerRequest>> triggers(@RequestParam(required = false)  String actionType) {
        if(StringUtils.isBlank(actionType)) {
            return ResponseEntity.status(HttpStatus.CREATED).body(triggerServiceOld.getAllTriggers());
        } else {
            return ResponseEntity.status(HttpStatus.CREATED).body(triggerServiceOld.getTriggersByIdent(actionType));
        }
    }


    @DeleteMapping("/clean")
    public ResponseEntity<Map<String, Object>> cleanDatabase() {
        actionService.deleteAllActions();
        //triggerService.deleteAllTriggers();
        notificationService.deleteAllNotifications();

        Map<String, Object> response = Map.of(
                "status", "success",
                "message", "Database cleaned successfully",
                "data", Map.of(
                        "actionsDeleted", true,
                        "triggersDeleted", true,
                        "notificationsDeleted", true
                )
        );
        return ResponseEntity.ok(response);
    }

}