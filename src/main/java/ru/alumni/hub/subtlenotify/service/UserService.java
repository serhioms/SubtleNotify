package ru.alumni.hub.subtlenotify.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.alumni.hub.subtlenotify.health.ActionsMetrics;
import ru.alumni.hub.subtlenotify.model.User;
import ru.alumni.hub.subtlenotify.repository.UserRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final ActionsMetrics actionsMetrics;

    /**
     * Store user if not exists, or return existing user
     * @param userId the user ID to store
     * @return Optional containing the User object (existing or newly created)
     */
    @Transactional
    public Optional<User> storeUser(String userId) {
        var timer = actionsMetrics.startTimer();
        try {
            User user = userRepository.findByUserId(userId)
                    .orElseGet(() -> {
                        User newUser = new User();
                        newUser.setUserId(userId);
                        User savedUser = userRepository.save(newUser);
                        LOGGER.info("Created new user with userId: {}", userId);
                        return savedUser;
                    });

            return Optional.of(user);
        } catch (Exception e) {
            actionsMetrics.incrementActionsFailed();
            LOGGER.error("Error while storing user with userId: " + userId, e);
        } finally {
            actionsMetrics.incrementActionsCreated();
            actionsMetrics.recordCreationTime(timer);
        }
        return Optional.empty();
    }

}