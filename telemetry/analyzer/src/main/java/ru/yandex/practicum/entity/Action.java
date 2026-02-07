package ru.yandex.practicum.entity;

import jakarta.persistence.*;
import lombok.*;
import ru.yandex.practicum.enums.ActionType;

@Entity
@Table(name = "actions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Action{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private ActionType type;

    @Column(name = "value", nullable = false)
    private Integer value;

}
