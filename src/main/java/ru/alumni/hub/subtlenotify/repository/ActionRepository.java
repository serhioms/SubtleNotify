package ru.alumni.hub.subtlenotify.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.alumni.hub.subtlenotify.model.Action;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

@Repository
public interface ActionRepository extends JpaRepository<Action, UUID> {

    @Query("select a from Action a order by a.timestamp asc")
    @NotNull
    List<Action> findAll();

    @Query("select a from Action a where a.userId = :userId order by a.timestamp asc")
    List<Action> findByUserId(@Param("userId") String userId);

    // Find all actions by actionType
    @Query("select a from Action a where a.actionType = :actionType order by a.timestamp asc")
    List<Action> findByActionType(@Param("actionType") String actionType);

    // Find actions by userId and actionType
    @Query("select a from Action a where a.actionType = :actionType and a.userId = :userId order by a.timestamp asc")
    List<Action> findByUserIdAndActionTypeByDays(@Param("userId") String userId, @Param("actionType") String actionType);

    // Find actions by userId and actionType
    @Query("select a from Action a where a.actionType = :actionType and a.userId = :userId and a.dayOfYear in :dayList order by a.timestamp asc")
    List<Action> findByUserIdAndActionTypeByDays(@Param("userId") String userId, @Param("actionType") String actionType, @Param("dayList") List<Integer> dayList);

    // Find actions by userId and actionType
    @Query("select a from Action a where a.actionType = :actionType and a.userId = :userId and a.weekOfYear in :weekList order by a.timestamp asc")
    List<Action> findByUserIdAndActionTypeByWeeks(@Param("userId") String userId, @Param("actionType") String actionType, @Param("weekList") List<Integer> weekList);
}