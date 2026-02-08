package ru.yandex.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.model.Action;

public interface ActionDao extends JpaRepository<Action, Long> {
}
