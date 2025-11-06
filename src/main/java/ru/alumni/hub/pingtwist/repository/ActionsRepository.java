package ru.alumni.hub.subtlenotify.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.alumni.hub.subtlenotify.model.Actions;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ActionsRepository extends JpaRepository<Actions, Long> {

    // Find all actions by userId
    List<Actions> findByUserId(String userId);

    // Find all actions by actionType
    List<Actions> findByActionType(String actionType);

    // Find actions by userId and actionType
    List<Actions> findByUserIdAndActionType(String userId, String actionType);

    // Find actions within a time range
    List<Actions> findByTimestampBetween(LocalDateTime start, LocalDateTime end);

    // Find actions by userId within a time range
    List<Actions> findByUserIdAndTimestampBetween(String userId, LocalDateTime start, LocalDateTime end);

    // Find recent actions ordered by timestamp descending
    List<Actions> findTop10ByOrderByTimestampDesc();
}