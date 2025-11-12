package ru.alumni.hub.subtlenotify.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.alumni.hub.subtlenotify.health.ActionsMetrics;
import ru.alumni.hub.subtlenotify.model.NotifyMessage;
import ru.alumni.hub.subtlenotify.repository.NotifyMessageRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NotifyMessageService {

    Logger LOGGER = LoggerFactory.getLogger(NotifyMessageService.class);

    private final NotifyMessageRepository notifyMessageRepository;
    private final ActionsMetrics actionsMetrics;


    /**
     * Store notify message if not exists, or update existing message
     * @param ident the message identifier
     * @param message the message content
     * @return Optional containing the NotifyMessage object (existing or newly created)
     */
    @Transactional
    public Optional<NotifyMessage> storeNotifyMessage(String ident, String message) {
        var timer = actionsMetrics.startTimer();
        try {
            NotifyMessage notifyMessage = notifyMessageRepository.findByIdent(ident)
                    .map(existing -> {
                        existing.setMessage(message);
                        return existing;
                    })
                    .orElseGet(() -> {
                        NotifyMessage newMessage = new NotifyMessage();
                        newMessage.setIdent(ident);
                        newMessage.setMessage(message);
                        return newMessage;
                    });

            Optional<NotifyMessage> save = Optional.of(notifyMessageRepository.save(notifyMessage));
            save.ifPresentOrElse(a->{}, actionsMetrics::incrementActionsFailed);
            return save;
        } finally {
            actionsMetrics.incrementActionsCreated();
            actionsMetrics.recordCreationTime(timer);
        }
    }


    /**
     * Get notify message by ident
     * @param ident the message identifier
     * @return Optional containing the NotifyMessage if found
     */
    public Optional<NotifyMessage> getNotifyMessage(String ident) {
        return notifyMessageRepository.findByIdent(ident);
    }


    /**
     * Delete all notify messages
     */
    @Transactional
    public void deleteAllNotifyMessages() {
        notifyMessageRepository.deleteAll();
    }

}