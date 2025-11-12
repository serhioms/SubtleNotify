package ru.alumni.hub.subtlenotify.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.alumni.hub.subtlenotify.model.NotifyMessage;

import java.util.Optional;

@Repository
public interface NotifyMessageRepository extends JpaRepository<NotifyMessage, String> {

    @Query("select nm from NotifyMessage nm where nm.ident = :ident")
    Optional<NotifyMessage> findByIdent(@Param("ident") String ident);

}