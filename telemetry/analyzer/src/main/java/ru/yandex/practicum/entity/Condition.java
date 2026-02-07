package ru.yandex.practicum.entity;

import jakarta.persistence.*;
import lombok.*;
import ru.yandex.practicum.enums.ConditionType;
import ru.yandex.practicum.enums.ConditionTypeOperation;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "conditions")
public class Condition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private ConditionType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "operation")
    private ConditionTypeOperation operation;

    @Column(name = "value")
    private Integer value;
}
