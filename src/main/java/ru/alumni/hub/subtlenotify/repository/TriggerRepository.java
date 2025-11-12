package ru.alumni.hub.subtlenotify.repository;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.alumni.hub.subtlenotify.model.ActionType;
import ru.alumni.hub.subtlenotify.model.NotifyMessage;
import ru.alumni.hub.subtlenotify.model.Trigger;
import ru.alumni.hub.subtlenotify.types.NotifyMoment;

import java.util.List;
import java.util.UUID;

@Repository
public interface TriggerRepository extends JpaRepository<Trigger, UUID> {

    @Query("select t from Trigger t order by t.createdAt desc")
    @NotNull
    List<Trigger> findAll();

    @Query("select t from Trigger t where t.actionType = :actionType order by t.createdAt desc")
    List<Trigger> findByActionType(@Param("actionType") ActionType actionType);

    @Query("select t from Trigger t where t.notifyMessage = :notifyMessage order by t.createdAt desc")
    List<Trigger> findByNotifyMessage(@Param("notifyMessage") NotifyMessage notifyMessage);

    @Query("select t from Trigger t where t.actionType = :actionType and t.notifyMessage = :notifyMessage order by t.createdAt desc")
    List<Trigger> findByActionTypeAndNotifyMessage(@Param("actionType") ActionType actionType, @Param("notifyMessage") NotifyMessage notifyMessage);

    @Query("select t from Trigger t where t.notifyMoment = :notifyMoment order by t.createdAt desc")
    List<Trigger> findByNotifyMoment(@Param("notifyMoment") NotifyMoment notifyMoment);

    @Query("select t from Trigger t where t.missPreviousTime = :missPreviousTime order by t.createdAt desc")
    List<Trigger> findByMissPreviousTime(@Param("missPreviousTime") Boolean missPreviousTime);

}