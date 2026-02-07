package ru.yandex.practicum.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "sensors")
public class Sensor {
    @Id
    @NotNull
    private String id;

    @Column(name = "hub_id")
    @NotNull
    private String hubId;
}
