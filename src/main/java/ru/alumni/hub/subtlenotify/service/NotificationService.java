package ru.alumni.hub.subtlenotify.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.alumni.hub.subtlenotify.model.Notification;
import ru.alumni.hub.subtlenotify.repository.NotificationRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    /**
     * Store a new notification without duplications
     * @param notification the notification
     * @return Optional containing the saved Notification
     */
    @Transactional
    public void storeNotificationWithoutDuplication(Notification notification) {
        if( notificationRepository.findByUserAndActionTypeAndDayOfYear(notification.getUser(), notification.getActionType(), notification.getDayOfYear()).isEmpty() ){
            notificationRepository.save(notification);
        }
    }

    /**
     * Get all notifications
     * @return List of all Notification objects
     */
    public List<Notification> getAllNotifications() {
        return notificationRepository.findAll();
    }

    /**
     * Delete all notifications
     */
    @Transactional
    public void deleteAllNotifications() {
        notificationRepository.deleteAll();
    }

}