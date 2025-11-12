package ru.alumni.hub.subtlenotify.service;


import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.alumni.hub.subtlenotify.health.ActionsMetrics;
import ru.alumni.hub.subtlenotify.model.ActionType;
import ru.alumni.hub.subtlenotify.model.Notification;
import ru.alumni.hub.subtlenotify.model.NotifyMessage;
import ru.alumni.hub.subtlenotify.model.User;
import ru.alumni.hub.subtlenotify.repository.NotificationRepository;
import ru.alumni.hub.subtlenotify.types.NotificationResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {

    Logger LOGGER = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final ActionsMetrics actionsMetrics;
    private final UserService userService;
    private final ActionTypeService actionTypeService;
    private final NotifyMessageService notifyMessageService;

    /**
     * Get notification by ID
     * @param id the notification UUID
     * @return Optional containing the Notification if found
     */
    public Optional<Notification> getNotification(UUID id) {
        try {
            return notificationRepository.findById(id);
        } catch (Exception e) {
            LOGGER.error("Error while retrieving notification with id: " + id, e);
        }
        return Optional.empty();
    }

    /**
     * Get all notifications
     * @return List of all Notification objects
     */
    public List<Notification> getAllNotifications() {
        try {
            return notificationRepository.findAll();
        } catch (Exception e) {
            LOGGER.error("Error while retrieving all notifications", e);
        }
        return List.of();
    }

    /**
     * Get notifications by user
     * @param userId the user ID
     * @return List of notifications for the user
     */
    public List<Notification> getNotificationsByUser(String userId) {
        try {
            return userService.getUser(userId)
                    .map(notificationRepository::findByUser)
                    .orElseGet(List::of);
        } catch (Exception e) {
            LOGGER.error("Error while retrieving notifications for user: " + userId, e);
        }
        return List.of();
    }

    /**
     * Get notifications by action type
     * @param actionType the action type string
     * @return List of notifications for the action type
     */
    public List<Notification> getNotificationsByActionType(String actionType) {
        try {
            return actionTypeService.getActionType(actionType)
                    .map(notificationRepository::findByActionType)
                    .orElseGet(List::of);
        } catch (Exception e) {
            LOGGER.error("Error while retrieving notifications for action type: " + actionType, e);
        }
        return List.of();
    }

    /**
     * Get notifications by user and action type
     * @param userId the user ID
     * @param actionType the action type string
     * @return List of notifications matching the criteria
     */
    public List<Notification> getNotificationsByUserAndActionType(String userId, String actionType) {
        try {
            Optional<User> userOpt = userService.getUser(userId);
            Optional<ActionType> actionTypeOpt = actionTypeService.getActionType(actionType);

            if (userOpt.isPresent() && actionTypeOpt.isPresent()) {
                return notificationRepository.findByUserAndActionType(userOpt.get(), actionTypeOpt.get());
            }
        } catch (Exception e) {
            LOGGER.error("Error while retrieving notifications for user: " + userId + " and action type: " + actionType, e);
        }
        return List.of();
    }

    /**
     * Get notifications with flexible filters
     * @param userId optional user ID filter
     * @param actionType optional action type filter
     * @return List of notifications matching the criteria
     */
    public List<Notification> getNotifications(String userId, String actionType) {
        boolean hasUserId = !StringUtils.isBlank(userId);
        boolean hasActionType = !StringUtils.isBlank(actionType);

        if (hasUserId && hasActionType) {
            return getNotificationsByUserAndActionType(userId, actionType);
        } else if (hasActionType) {
            return getNotificationsByActionType(actionType);
        } else if (hasUserId) {
            return getNotificationsByUser(userId);
        } else {
            return getAllNotifications();
        }
    }

    public void storeNotification(NotificationResponse notificationResponse) {
    }
    
    /**
     * Store a new notification
     * @param messageIdent the notify message identifier
     * @param userId the user ID
     * @param actionType the action type string
     * @param timestamp the notification timestamp
     * @return Optional containing the saved Notification
     */
    @Transactional
    public Optional<Notification> storeNotification(String messageIdent, String userId, String actionType, LocalDateTime timestamp) {
        try {
            Optional<NotifyMessage> messageOpt = notifyMessageService.getNotifyMessage(messageIdent);
            Optional<User> userOpt = userService.getUser(userId);
            Optional<ActionType> actionTypeOpt = actionTypeService.getActionType(actionType);

            if (messageOpt.isPresent() && userOpt.isPresent() && actionTypeOpt.isPresent()) {
                Notification notification = new Notification();
                notification.setNotifyMessage(messageOpt.get());
                notification.setUser(userOpt.get());
                notification.setActionType(actionTypeOpt.get());
                notification.setTimestamp(timestamp);

                Notification saved = notificationRepository.save(notification);
                LOGGER.info("Created notification with id: {} for user: {} and action type: {}",
                        saved.getId(), userId, actionType);
                return Optional.of(saved);
            } else {
                LOGGER.error("Failed to create notification - missing entities: message={}, user={}, actionType={}",
                        messageOpt.isPresent(), userOpt.isPresent(), actionTypeOpt.isPresent());
            }
        } catch (Exception e) {
            LOGGER.error("Error while storing notification", e);
        }
        return Optional.empty();
    }

    /**
     * Delete notification by ID
     * @param id the notification UUID
     * @return true if deleted successfully, false otherwise
     */
    @Transactional
    public boolean deleteNotification(UUID id) {
        try {
            Optional<Notification> notification = notificationRepository.findById(id);
            if (notification.isPresent()) {
                notificationRepository.delete(notification.get());
                LOGGER.info("Deleted notification with id: {}", id);
                return true;
            }
            LOGGER.warn("Notification with id {} not found for deletion", id);
            return false;
        } catch (Exception e) {
            LOGGER.error("Error while deleting notification with id: " + id, e);
            return false;
        }
    }

    /**
     * Delete all notifications
     */
    @Transactional
    public void deleteAllNotifications() {
        try {
            notificationRepository.deleteAll();
            LOGGER.info("Deleted all notifications");
        } catch (Exception e) {
            LOGGER.error("Error while deleting all notifications", e);
        }
    }

}