package ru.alumni.hub.subtlenotify.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notify_messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotifyMessage {

    @Id
    @NotNull(message = "ident is required")
    @Column(name = "ident", nullable = false, unique = true)
    private String ident;

    @NotNull(message = "message is required")
    @Column(name = "message", nullable = false)
    private String message;

}