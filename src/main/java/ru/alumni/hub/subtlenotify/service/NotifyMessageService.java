package ru.alumni.hub.subtlenotify.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.alumni.hub.subtlenotify.model.NotifyMessage;
import ru.alumni.hub.subtlenotify.repository.NotifyMessageRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NotifyMessageService {

    Logger LOGGER = LoggerFactory.getLogger(NotifyMessageService.class);

    private final NotifyMessageRepository notifyMessageRepository;

    /**
     * Get notify message by ident
     * @param ident the message identifier
     * @return Optional containing the NotifyMessage if found
     */
    public Optional<NotifyMessage> getNotifyMessage(String ident) {
        try {
            return notifyMessageRepository.findByIdent(ident);
        } catch (Exception e) {
            LOGGER.error("Error while retrieving notify message with ident: " + ident, e);
        }
        return Optional.empty();
    }

    /**
     * Get all notify messages
     * @return List of all NotifyMessage objects
     */
    public List<NotifyMessage> getAllNotifyMessages() {
        try {
            return notifyMessageRepository.findAll();
        } catch (Exception e) {
            LOGGER.error("Error while retrieving all notify messages", e);
        }
        return List.of();
    }

    /**
     * Store notify message if not exists, or update existing message
     * @param ident the message identifier
     * @param message the message content
     * @return Optional containing the NotifyMessage object (existing or newly created)
     */
    @Transactional
    public Optional<NotifyMessage> storeNotifyMessage(String ident, String message) {
        try {
            NotifyMessage notifyMessage = notifyMessageRepository.findByIdent(ident)
                    .map(existing -> {
                        existing.setMessage(message);
                        LOGGER.info("Updated notify message with ident: {}", ident);
                        return existing;
                    })
                    .orElseGet(() -> {
                        NotifyMessage newMessage = new NotifyMessage();
                        newMessage.setIdent(ident);
                        newMessage.setMessage(message);
                        LOGGER.info("Created new notify message with ident: {}", ident);
                        return newMessage;
                    });

            return Optional.of(notifyMessageRepository.save(notifyMessage));
        } catch (Exception e) {
            LOGGER.error("Error while storing notify message with ident: " + ident, e);
        }
        return Optional.empty();
    }

    /**
     * Store notify message entity
     * @param notifyMessage the NotifyMessage entity to store
     * @return Optional containing the saved NotifyMessage object
     */
    @Transactional
    public Optional<NotifyMessage> storeNotifyMessage(NotifyMessage notifyMessage) {
        return storeNotifyMessage(notifyMessage.getIdent(), notifyMessage.getMessage());
    }

    /**
     * Delete notify message by ident
     * @param ident the message identifier to delete
     * @return true if deleted successfully, false otherwise
     */
    @Transactional
    public boolean deleteNotifyMessage(String ident) {
        try {
            Optional<NotifyMessage> message = notifyMessageRepository.findByIdent(ident);
            if (message.isPresent()) {
                notifyMessageRepository.delete(message.get());
                LOGGER.info("Deleted notify message with ident: {}", ident);
                return true;
            }
            LOGGER.warn("Notify message with ident {} not found for deletion", ident);
            return false;
        } catch (Exception e) {
            LOGGER.error("Error while deleting notify message with ident: " + ident, e);
            return false;
        }
    }

    /**
     * Delete all notify messages
     */
    @Transactional
    public void deleteAllNotifyMessages() {
        try {
            notifyMessageRepository.deleteAll();
            LOGGER.info("Deleted all notify messages");
        } catch (Exception e) {
            LOGGER.error("Error while deleting all notify messages", e);
        }
    }

}