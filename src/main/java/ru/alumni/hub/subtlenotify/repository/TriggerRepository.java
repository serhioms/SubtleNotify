package ru.alumni.hub.subtlenotify.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.alumni.hub.subtlenotify.model.ActionType;
import ru.alumni.hub.subtlenotify.model.NotifyMessage;
import ru.alumni.hub.subtlenotify.model.Trigger;

import java.util.List;
import java.util.UUID;

@Repository
public interface TriggerRepository extends JpaRepository<Trigger, UUID> {

    @Query("select t from Trigger t where t.actionType = :actionType")
    List<Trigger> findByActionType(@Param("actionType") ActionType actionType);

    @Query("select t from Trigger t where t.notifyMessage = :notifyMessageNo")
    List<Trigger> findByNotifyMessage(@Param("notifyMessage") NotifyMessage notifyMessage);


}