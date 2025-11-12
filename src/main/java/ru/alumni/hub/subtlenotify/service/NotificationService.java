package ru.alumni.hub.subtlenotify.service;


import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.alumni.hub.subtlenotify.health.ActionsMetrics;
import ru.alumni.hub.subtlenotify.model.Notification;
import ru.alumni.hub.subtlenotify.repository.NotificationRepository;

import java.util.List;
import java.util.Optional;

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
     * Store a new notification
     * @param notification the notification
     * @return Optional containing the saved Notification
     */
    @Transactional
    public Optional<Notification> storeNotification(Notification notification) {
        return notificationRepository.findByUserAndActionTypeAndDayOfYear(notification.getUser(), notification.getActionType(), notification.getDayOfYear()).isEmpty()
                ? Optional.of(notificationRepository.save(notification))
                : Optional.empty();
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