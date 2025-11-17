package ru.alumni.hub.subtlenotify.service;

import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.alumni.hub.subtlenotify.health.ActionsMetrics;
import ru.alumni.hub.subtlenotify.model.Action;
import ru.alumni.hub.subtlenotify.model.ActionType;
import ru.alumni.hub.subtlenotify.model.Notification;
import ru.alumni.hub.subtlenotify.model.NotifyMessage;
import ru.alumni.hub.subtlenotify.model.Trigger;
import ru.alumni.hub.subtlenotify.model.User;
import ru.alumni.hub.subtlenotify.types.NotifyMoment;

import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubtleNotifyServiceTest {

    @Mock
    private ActionsMetrics actionsMetrics;

    @Mock
    private ActionService actionService;

    @Mock
    private TriggerService triggerService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private Timer.Sample timerSample;

    @InjectMocks
    private SubtleNotifyService subtleNotifyService;

    private Action testAction;
    private User testUser;
    private ActionType testActionType;
    private Trigger testTrigger;
    private NotifyMessage testNotifyMessage;

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = new User();
        testUser.setUserId("user123");

        // Setup test action type
        testActionType = new ActionType();
        testActionType.setActionType("coffee_purchase");

        // Setup test action
        testAction = new Action();
        testAction.setId(UUID.randomUUID());
        testAction.setUser(testUser);
        testAction.setActionType(testActionType);
        testAction.setTimestamp(LocalDateTime.now());
        testAction.setDayOfYear(testAction.getTimestamp().getDayOfYear());
        testAction.setWeekOfYear(testAction.getTimestamp().get(WeekFields.of(Locale.getDefault()).weekOfYear()));

        // Setup test notify message
        testNotifyMessage = new NotifyMessage();
        testNotifyMessage.setIdent("coffee_msg");
        testNotifyMessage.setMessage("Time for coffee! ☕");

        // Setup test trigger
        testTrigger = new Trigger();
        testTrigger.setId(UUID.randomUUID());
        testTrigger.setNotifyMessage(testNotifyMessage);
        testTrigger.setActionType(testActionType);
        testTrigger.setDescr("Coffee purchase trigger");
        testTrigger.setExpectWeekDays("MON,WED,FRI");
        testTrigger.setExpectHowOften(2);
        testTrigger.setExpectFromHr(9);
        testTrigger.setExpectToHr(11);
        testTrigger.setMissPreviousTime(false);
        testTrigger.setNotifyMoment(NotifyMoment.immediately);

        // Setup timer mock
        when(actionsMetrics.startTimer()).thenReturn(timerSample);
    }

    @Test
    void testGenerateNotification_WithValidTrigger_ShouldCreateNotification() {
        // Arrange
        List<Trigger> triggers = List.of(testTrigger);
        when(triggerService.getTriggersByActionType("coffee_purchase")).thenReturn(triggers);

        List<Action> historicalActions = createHistoricalActions(3);
        when(actionService.getActionsForTimeRange(any(), any(), anyList(), anyList()))
                .thenReturn(historicalActions);

        // Act
        subtleNotifyService.generateNotification(testAction);

        // Assert
        verify(actionsMetrics).startTimer();
        verify(triggerService).getTriggersByActionType("coffee_purchase");
        verify(actionsMetrics).incrementActionsCreated();
        verify(actionsMetrics).recordCreationTime(timerSample);
        verify(notificationService, atLeastOnce()).storeNotificationWithoutDuplication(any(Notification.class));
    }

    @Test
    void testGenerateNotification_NoTriggersFound_ShouldThrowException() {
        // Arrange
        when(triggerService.getTriggersByActionType("coffee_purchase")).thenReturn(List.of());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            subtleNotifyService.generateNotification(testAction);
        });

        verify(actionsMetrics).startTimer();
        verify(actionsMetrics).incrementActionsFailed();
        verify(actionsMetrics).incrementActionsCreated();
        verify(actionsMetrics).recordCreationTime(timerSample);
        verify(notificationService, never()).storeNotificationWithoutDuplication(any());
    }

    @Test
    void testGenerateNotification_WithMissPreviousTimeTrigger_ShouldCallGenerateMissedNotification() {
        // Arrange
        testTrigger.setMissPreviousTime(true);
        testTrigger.setExpectEveryDays(1);
        testTrigger.setExpectWeekDays(null);

        List<Trigger> triggers = List.of(testTrigger);
        when(triggerService.getTriggersByActionType("coffee_purchase")).thenReturn(triggers);

        when(actionService.getActionsForTimeRange(any(), any(), anyList(), anyList()))
                .thenReturn(List.of());

        // Act
        subtleNotifyService.generateNotification(testAction);

        // Assert
        verify(triggerService).getTriggersByActionType("coffee_purchase");
        verify(actionsMetrics).incrementActionsCreated();
        verify(actionsMetrics).recordCreationTime(timerSample);
    }

    @Test
    void testGenerateNotification_WithMultipleTriggers_ShouldProcessAll() {
        // Arrange
        Trigger trigger2 = new Trigger();
        trigger2.setId(UUID.randomUUID());
        trigger2.setNotifyMessage(testNotifyMessage);
        trigger2.setActionType(testActionType);
        trigger2.setExpectEveryDays(2);
        trigger2.setExpectHowOften(3);
        trigger2.setExpectFromHr(13);
        trigger2.setExpectToHr(17);
        trigger2.setMissPreviousTime(false);
        trigger2.setNotifyMoment(NotifyMoment.next_time);

        List<Trigger> triggers = List.of(testTrigger, trigger2);
        when(triggerService.getTriggersByActionType("coffee_purchase")).thenReturn(triggers);

        List<Action> historicalActions = createHistoricalActions(3);
        when(actionService.getActionsForTimeRange(any(), any(), anyList(), anyList()))
                .thenReturn(historicalActions);

        // Act
        subtleNotifyService.generateNotification(testAction);

        // Assert
        verify(triggerService).getTriggersByActionType("coffee_purchase");
        verify(actionsMetrics).incrementActionsCreated();
        verify(actionsMetrics).recordCreationTime(timerSample);
    }

    @Test
    void testGenerateNotification_WithException_ShouldStillRecordMetrics() {
        // Arrange
        when(triggerService.getTriggersByActionType("coffee_purchase"))
                .thenThrow(new RuntimeException("Database error"));

        // Act
        assertThrows(RuntimeException.class, () -> {
            subtleNotifyService.generateNotification(testAction);
        });

        // Assert - Metrics should still be recorded in finally block
        verify(actionsMetrics).startTimer();
        verify(actionsMetrics).incrementActionsCreated();
        verify(actionsMetrics).recordCreationTime(timerSample);
    }

    @Test
    void testGenerateNotification_WithEmptyHistoricalActions_ShouldNotCreateNotification() {
        // Arrange
        List<Trigger> triggers = List.of(testTrigger);
        when(triggerService.getTriggersByActionType("coffee_purchase")).thenReturn(triggers);

        // Return empty list - pattern not matched
        when(actionService.getActionsForTimeRange(any(), any(), anyList(), anyList()))
                .thenReturn(List.of());

        // Act
        subtleNotifyService.generateNotification(testAction);

        // Assert
        verify(triggerService).getTriggersByActionType("coffee_purchase");
        verify(actionsMetrics).incrementActionsCreated();
        verify(notificationService, never()).storeNotificationWithoutDuplication(any());
    }

    // Helper method to create historical actions
    private List<Action> createHistoricalActions(int count) {
        List<Action> actions = new ArrayList<>();
        LocalDateTime baseTime = LocalDateTime.now().minusDays(count);

        for (int i = 0; i < count; i++) {
            Action action = new Action();
            action.setId(UUID.randomUUID());
            action.setUser(testUser);
            action.setActionType(testActionType);
            action.setTimestamp(baseTime.plusDays(i).withHour(10).withMinute(0));
            action.setDayOfYear(action.getTimestamp().getDayOfYear());
            action.setWeekOfYear(action.getTimestamp().get(WeekFields.of(Locale.getDefault()).weekOfYear()));
            actions.add(action);
        }

        return actions;
    }
}