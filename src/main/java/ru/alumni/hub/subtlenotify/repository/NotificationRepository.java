package ru.alumni.hub.subtlenotify.repository;


import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.alumni.hub.subtlenotify.model.ActionType;
import ru.alumni.hub.subtlenotify.model.Notification;
import ru.alumni.hub.subtlenotify.model.User;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    @Query("select n from Notification n where n.user = :user and n.actionType = :actionType and n.dayOfYear = :dayOfYear order by n.timestamp desc")
    List<Notification> findByUserAndActionTypeAndDayOfYear(@Param("user") User user, @Param("actionType") ActionType actionType, @Param("dayOfYear") Integer dayOfYear);





    @Query("select n from Notification n order by n.timestamp desc")
    @NotNull
    List<Notification> findAll();

    @Query("select n from Notification n where n.user = :user order by n.timestamp desc")
    List<Notification> findByUser(@Param("user") User user);

    @Query("select n from Notification n where n.actionType = :actionType order by n.timestamp desc")
    List<Notification> findByActionType(@Param("actionType") ActionType actionType);



    @Query("select n from Notification n where n.timestamp >= :fromTimestamp order by n.timestamp desc")
    List<Notification> findByTimestampAfter(@Param("fromTimestamp") java.time.LocalDateTime fromTimestamp);

}