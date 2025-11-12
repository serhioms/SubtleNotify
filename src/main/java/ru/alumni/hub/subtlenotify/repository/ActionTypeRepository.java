package ru.alumni.hub.subtlenotify.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.alumni.hub.subtlenotify.model.ActionType;

import java.util.Optional;

@Repository
public interface ActionTypeRepository extends JpaRepository<ActionType, String> {

    @Query("select at from ActionType at where at.actionType = :actionType")
    Optional<ActionType> findByActionType(@Param("actionType") String actionType);

}
