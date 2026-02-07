package ru.yandex.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.entity.Condition;

public interface ConditionDao extends JpaRepository<Condition, Long> {
}
