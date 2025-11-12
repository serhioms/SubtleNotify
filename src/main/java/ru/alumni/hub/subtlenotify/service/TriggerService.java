package ru.alumni.hub.subtlenotify.service;


import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.alumni.hub.subtlenotify.model.ActionType;
import ru.alumni.hub.subtlenotify.model.NotifyMessage;
import ru.alumni.hub.subtlenotify.model.Trigger;
import ru.alumni.hub.subtlenotify.repository.TriggerRepository;
import ru.alumni.hub.subtlenotify.types.NotifyMoment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TriggerService {

    Logger LOGGER = LoggerFactory.getLogger(TriggerService.class);

    private final TriggerRepository triggerRepository;
    private final ActionTypeService actionTypeService;
    private final NotifyMessageService notifyMessageService;

    /**
     * Get trigger by ID
     * @param id the trigger UUID
     * @return Optional containing the Trigger if found
     */
    public Optional<Trigger> getTrigger(UUID id) {
        try {
            return triggerRepository.findById(id);
        } catch (Exception e) {
            LOGGER.error("Error while retrieving trigger with id: " + id, e);
        }
        return Optional.empty();
    }

    /**
     * Get all triggers
     * @return List of all Trigger objects
     */
    public List<Trigger> getAllTriggers() {
        try {
            return triggerRepository.findAll();
        } catch (Exception e) {
            LOGGER.error("Error while retrieving all triggers", e);
        }
        return List.of();
    }

    /**
     * Get triggers by action type
     * @param actionType the action type string
     * @return List of triggers for the action type
     */
    public List<Trigger> getTriggersByActionType(String actionType) {
        try {
            return actionTypeService.getActionType(actionType)
                    .map(triggerRepository::findByActionType)
                    .orElseGet(List::of);
        } catch (Exception e) {
            LOGGER.error("Error while retrieving triggers for action type: " + actionType, e);
        }
        return List.of();
    }

    /**
     * Get triggers by notify message
     * @param messageIdent the message identifier
     * @return List of triggers for the notify message
     */
    public List<Trigger> getTriggersByNotifyMessage(String messageIdent) {
        try {
            return notifyMessageService.getNotifyMessage(messageIdent)
                    .map(triggerRepository::findByNotifyMessage)
                    .orElseGet(List::of);
        } catch (Exception e) {
            LOGGER.error("Error while retrieving triggers for notify message: " + messageIdent, e);
        }
        return List.of();
    }

    /**
     * Get triggers by action type and notify message
     * @param actionType the action type string
     * @param messageIdent the message identifier
     * @return List of triggers matching the criteria
     */
    public List<Trigger> getTriggersByActionTypeAndNotifyMessage(String actionType, String messageIdent) {
        try {
            Optional<ActionType> actionTypeOpt = actionTypeService.getActionType(actionType);
            Optional<NotifyMessage> messageOpt = notifyMessageService.getNotifyMessage(messageIdent);

            if (actionTypeOpt.isPresent() && messageOpt.isPresent()) {
                return triggerRepository.findByActionTypeAndNotifyMessage(actionTypeOpt.get(), messageOpt.get());
            }
        } catch (Exception e) {
            LOGGER.error("Error while retrieving triggers for action type: " + actionType + " and message: " + messageIdent, e);
        }
        return List.of();
    }

    /**
     * Get triggers with flexible filters
     * @param actionType optional action type filter
     * @param messageIdent optional message identifier filter
     * @return List of triggers matching the criteria
     */
    public List<Trigger> getTriggers(String actionType, String messageIdent) {
        boolean hasActionType = !StringUtils.isBlank(actionType);
        boolean hasMessageIdent = !StringUtils.isBlank(messageIdent);

        if (hasActionType && hasMessageIdent) {
            return getTriggersByActionTypeAndNotifyMessage(actionType, messageIdent);
        } else if (hasActionType) {
            return getTriggersByActionType(actionType);
        } else if (hasMessageIdent) {
            return getTriggersByNotifyMessage(messageIdent);
        } else {
            return getAllTriggers();
        }
    }

    /**
     * Get triggers by notify moment
     * @param notifyMoment the NotifyMoment enum value
     * @return List of triggers with the specified notify moment
     */
    public List<Trigger> getTriggersByNotifyMoment(NotifyMoment notifyMoment) {
        try {
            return triggerRepository.findByNotifyMoment(notifyMoment);
        } catch (Exception e) {
            LOGGER.error("Error while retrieving triggers for notify moment: " + notifyMoment, e);
        }
        return List.of();
    }

    /**
     * Get triggers by missPreviousTime flag
     * @param missPreviousTime the flag value
     * @return List of triggers with the specified flag
     */
    public List<Trigger> getTriggersByMissPreviousTime(Boolean missPreviousTime) {
        try {
            return triggerRepository.findByMissPreviousTime(missPreviousTime);
        } catch (Exception e) {
            LOGGER.error("Error while retrieving triggers for missPreviousTime: " + missPreviousTime, e);
        }
        return List.of();
    }

    /**
     * Store a new trigger
     * @param trigger the Trigger entity to store
     * @return Optional containing the saved Trigger
     */
    @Transactional
    public Optional<Trigger> storeTrigger(Trigger trigger) {
        try {
            // Validate that the foreign key entities exist
            if (trigger.getNotifyMessage() == null || trigger.getActionType() == null) {
                LOGGER.error("Cannot store trigger - notifyMessage or actionType is null");
                return Optional.empty();
            }

            Trigger saved = triggerRepository.save(trigger);
            LOGGER.info("Created trigger with id: {} for action type: {}",
                    saved.getId(), saved.getActionType().getActionType());
            return Optional.of(saved);
        } catch (Exception e) {
            LOGGER.error("Error while storing trigger", e);
        }
        return Optional.empty();
    }

    /**
     * Store a new trigger with string parameters
     * @param messageIdent the notify message identifier
     * @param actionType the action type string
     * @param descr description
     * @param expectWeekDays expected week days
     * @param expectEveryDays expected every days
     * @param expectHowOften expected how often
     * @param expectFromHr expected from hour
     * @param expectToHr expected to hour
     * @param missPreviousTime miss previous time flag
     * @param notifyMoment notify moment enum
     * @return Optional containing the saved Trigger
     */
    @Transactional
    public Optional<Trigger> storeTrigger(String messageIdent, String actionType, String descr,
                                          String expectWeekDays, String expectEveryDays, Integer expectHowOften,
                                          Integer expectFromHr, Integer expectToHr, Boolean missPreviousTime,
                                          NotifyMoment notifyMoment) {
        try {
            Optional<NotifyMessage> messageOpt = notifyMessageService.getNotifyMessage(messageIdent);
            Optional<ActionType> actionTypeOpt = actionTypeService.getActionType(actionType);

            if (messageOpt.isPresent() && actionTypeOpt.isPresent()) {
                Trigger trigger = new Trigger();
                trigger.setNotifyMessage(messageOpt.get());
                trigger.setActionType(actionTypeOpt.get());
                trigger.setDescr(descr);
                trigger.setExpectWeekDays(expectWeekDays);
                trigger.setExpectEveryDays(expectEveryDays);
                trigger.setExpectHowOften(expectHowOften);
                trigger.setExpectFromHr(expectFromHr);
                trigger.setExpectToHr(expectToHr);
                trigger.setMissPreviousTime(missPreviousTime != null ? missPreviousTime : false);
                trigger.setNotifyMoment(notifyMoment);

                return storeTrigger(trigger);
            } else {
                LOGGER.error("Failed to create trigger - missing entities: message={}, actionType={}",
                        messageOpt.isPresent(), actionTypeOpt.isPresent());
            }
        } catch (Exception e) {
            LOGGER.error("Error while storing trigger", e);
        }
        return Optional.empty();
    }

    /**
     * Update an existing trigger
     * @param id the trigger UUID
     * @param trigger the updated Trigger data
     * @return Optional containing the updated Trigger
     */
    @Transactional
    public Optional<Trigger> updateTrigger(UUID id, Trigger trigger) {
        try {
            Optional<Trigger> existingOpt = triggerRepository.findById(id);
            if (existingOpt.isPresent()) {
                Trigger existing = existingOpt.get();

                if (trigger.getNotifyMessage() != null) {
                    existing.setNotifyMessage(trigger.getNotifyMessage());
                }
                if (trigger.getActionType() != null) {
                    existing.setActionType(trigger.getActionType());
                }
                if (trigger.getDescr() != null) {
                    existing.setDescr(trigger.getDescr());
                }
                if (trigger.getExpectWeekDays() != null) {
                    existing.setExpectWeekDays(trigger.getExpectWeekDays());
                }
                if (trigger.getExpectEveryDays() != null) {
                    existing.setExpectEveryDays(trigger.getExpectEveryDays());
                }
                if (trigger.getExpectHowOften() != null) {
                    existing.setExpectHowOften(trigger.getExpectHowOften());
                }
                if (trigger.getExpectFromHr() != null) {
                    existing.setExpectFromHr(trigger.getExpectFromHr());
                }
                if (trigger.getExpectToHr() != null) {
                    existing.setExpectToHr(trigger.getExpectToHr());
                }
                if (trigger.getMissPreviousTime() != null) {
                    existing.setMissPreviousTime(trigger.getMissPreviousTime());
                }
                if (trigger.getNotifyMoment() != null) {
                    existing.setNotifyMoment(trigger.getNotifyMoment());
                }

                Trigger saved = triggerRepository.save(existing);
                LOGGER.info("Updated trigger with id: {}", id);
                return Optional.of(saved);
            } else {
                LOGGER.warn("Trigger with id {} not found for update", id);
            }
        } catch (Exception e) {
            LOGGER.error("Error while updating trigger with id: " + id, e);
        }
        return Optional.empty();
    }

    /**
     * Delete trigger by ID
     * @param id the trigger UUID
     * @return true if deleted successfully, false otherwise
     */
    @Transactional
    public boolean deleteTrigger(UUID id) {
        try {
            Optional<Trigger> trigger = triggerRepository.findById(id);
            if (trigger.isPresent()) {
                triggerRepository.delete(trigger.get());
                LOGGER.info("Deleted trigger with id: {}", id);
                return true;
            }
            LOGGER.warn("Trigger with id {} not found for deletion", id);
            return false;
        } catch (Exception e) {
            LOGGER.error("Error while deleting trigger with id: " + id, e);
            return false;
        }
    }

    /**
     * Delete all triggers
     */
    @Transactional
    public void deleteAllTriggers() {
        try {
            triggerRepository.deleteAll();
            LOGGER.info("Deleted all triggers");
        } catch (Exception e) {
            LOGGER.error("Error while deleting all triggers", e);
        }
    }

}