package ru.alumni.hub.subtlenotify.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.alumni.hub.subtlenotify.model.Notification;
import java.util.List;


@Repository
public interface NotificationRepository extends JpaRepository<Notification, String> {

    List<Notification> findByIdent(String ident);

}