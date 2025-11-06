package ru.alumni.hub.subtlenotify.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "notification")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    @Id
    @NotNull(message = "ident is required")
    private String ident;

    @NotNull(message = "descr is required")
    @Column(name = "descr", nullable = false)
    private String descr;
}