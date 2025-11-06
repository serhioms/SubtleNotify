package ru.alumni.hub.subtlenotify.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.alumni.hub.subtlenotify.health.ActionsMetrics;
import ru.alumni.hub.subtlenotify.model.Notification;
import ru.alumni.hub.subtlenotify.repository.NotificationRepository;
import ru.alumni.hub.subtlenotify.types.NotificationRequest;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final ActionsMetrics actionsMetrics;

    @Transactional
    public Notification storeNotification(NotificationRequest request) {
        var timer = actionsMetrics.startTimer();
        try {
            Notification notification = new Notification();
            notification.setIdent(request.getIdent());
            notification.setDescr(request.getDescr());

            Notification saved = notificationRepository.save(notification);
            actionsMetrics.incrementActionsCreated();
            actionsMetrics.recordCreationTime(timer);
            return saved;
        } catch (Exception e) {
            actionsMetrics.incrementActionsFailed();
            throw e;
        }
    }
    public List<Notification> getNotificationsByIdent(String ident) {
        return notificationRepository.findByIdent(ident);
    }

    public List<Notification> getAllNotifications() {
        return notificationRepository.findAll();
    }

    private Notification convertToEntity(NotificationRequest request) {
        Notification notification = new Notification();
        notification.setIdent(request.getIdent());
        notification.setDescr(request.getDescr());
        return notification;
    }
}