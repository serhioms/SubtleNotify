package ru.alumni.hub.subtlenotify.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "action_types")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "actions")
public class ActionType {

    @Id
    @NotNull(message = "actionType is required")
    @Column(name = "action_type", nullable = false, unique = true)
    private String actionType;

    @OneToMany(mappedBy = "actionType", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Action> actions = new ArrayList<>();

}
