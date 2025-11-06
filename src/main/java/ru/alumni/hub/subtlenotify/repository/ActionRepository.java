package ru.alumni.hub.subtlenotify.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.alumni.hub.subtlenotify.model.Action;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ActionRepository extends JpaRepository<Action, UUID> {

    // Find all actions by userId
    List<Action> findByUserId(String userId);

    // Find all actions by actionType
    List<Action> findByActionType(String actionType);

    // Find actions by userId and actionType
    List<Action> findByUserIdAndActionType(String userId, String actionType);

    // Find actions within a time range
    List<Action> findByTimestampBetween(LocalDateTime start, LocalDateTime end);

    // Find actions by userId within a time range
    List<Action> findByUserIdAndTimestampBetween(String userId, LocalDateTime start, LocalDateTime end);

    // Find recent actions ordered by timestamp descending
    List<Action> findTop10ByOrderByTimestampDesc();
}