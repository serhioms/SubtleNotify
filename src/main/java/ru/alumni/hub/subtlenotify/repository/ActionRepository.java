package ru.alumni.hub.subtlenotify.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.alumni.hub.subtlenotify.model.Action;
import jakarta.validation.constraints.NotNull;
import ru.alumni.hub.subtlenotify.model.User;

import java.util.List;
import java.util.UUID;

@Repository
public interface ActionRepository extends JpaRepository<Action, UUID> {

    @Query("select a from Action a order by a.timestamp asc")
    @NotNull
    List<Action> findAll();

    @Query("select a from Action a where a.user = :user order by a.timestamp asc")
    List<Action> findByUserId(@Param("user") User user);

    // Find all actions by actionType
    @Query("select a from Action a where a.actionType = :actionType order by a.timestamp asc")
    List<Action> findByActionType(@Param("actionType") String actionType);

    // Find actions by userId and actionType
    @Query("select a from Action a where a.actionType = :actionType and a.user = :user order by a.timestamp asc")
    List<Action> findByUserIdAndActionTypeByDays(@Param("user") User user, @Param("actionType") String actionType);

    // Find actions by userId and actionType
    @Query("select a from Action a where a.actionType = :actionType and a.user = :user and a.dayOfYear in :dayList order by a.timestamp asc")
    List<Action> findByUserIdAndActionTypeByDays(@Param("user") User user, @Param("actionType") String actionType, @Param("dayList") List<Integer> dayList);

    // Find actions by userId and actionType
    @Query("select a from Action a where a.actionType = :actionType and a.user = :user and a.weekOfYear in :weekList order by a.timestamp asc")
    List<Action> findByUserIdAndActionTypeByWeeks(@Param("user") User user, @Param("actionType") String actionType, @Param("weekList") List<Integer> weekList);
}