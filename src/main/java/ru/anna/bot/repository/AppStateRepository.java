package ru.anna.bot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.anna.bot.domain.AppStateEntity;

public interface AppStateRepository extends JpaRepository<AppStateEntity, String> {
}
