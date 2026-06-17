package ru.anna.bot.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.anna.bot.domain.TelegramUserEntity;

public interface TelegramUserRepository extends JpaRepository<TelegramUserEntity, Long> {

    Optional<TelegramUserEntity> findByTelegramUserId(Long telegramUserId);

    List<TelegramUserEntity> findAllByOrderByCreatedAtAsc();
}
