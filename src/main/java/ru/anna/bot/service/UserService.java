package ru.anna.bot.service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.anna.bot.domain.ConversationState;
import ru.anna.bot.domain.TariffCode;
import ru.anna.bot.domain.TelegramUserEntity;
import ru.anna.bot.integration.telegram.model.TelegramUser;
import ru.anna.bot.repository.TelegramUserRepository;

@Service
public class UserService {

    private final TelegramUserRepository telegramUserRepository;
    private final Clock clock;

    public UserService(TelegramUserRepository telegramUserRepository, Clock clock) {
        this.telegramUserRepository = telegramUserRepository;
        this.clock = clock;
    }

    @Transactional
    public TelegramUserEntity upsertPrivateUser(Long privateChatId, TelegramUser telegramUser) {
        TelegramUserEntity user = telegramUserRepository.findByTelegramUserId(telegramUser.id())
            .orElseGet(TelegramUserEntity::new);
        user.setTelegramUserId(telegramUser.id());
        user.setPrivateChatId(privateChatId);
        user.setUsername(telegramUser.username());
        user.setFirstName(telegramUser.firstName());
        user.setLastName(telegramUser.lastName());
        user.setLastSeenAt(Instant.now(clock));
        return telegramUserRepository.save(user);
    }

    @Transactional(readOnly = true)
    public Optional<TelegramUserEntity> findByTelegramUserId(Long telegramUserId) {
        return telegramUserRepository.findByTelegramUserId(telegramUserId);
    }

    @Transactional(readOnly = true)
    public List<TelegramUserEntity> findAllUsers() {
        return telegramUserRepository.findAllByOrderByCreatedAtAsc();
    }

    @Transactional
    public TelegramUserEntity save(TelegramUserEntity user) {
        return telegramUserRepository.save(user);
    }

    @Transactional
    public void setAwaitingEmail(TelegramUserEntity user, TariffCode tariffCode) {
        user.setConversationState(ConversationState.AWAITING_EMAIL);
        user.setConversationPayload(tariffCode.name());
        telegramUserRepository.save(user);
    }

    @Transactional
    public void setAwaitingTariffPrice(TelegramUserEntity user, TariffCode tariffCode) {
        user.setConversationState(ConversationState.AWAITING_TARIFF_PRICE);
        user.setConversationPayload(tariffCode.name());
        telegramUserRepository.save(user);
    }

    @Transactional
    public void setAwaitingBroadcast(TelegramUserEntity user) {
        user.setConversationState(ConversationState.AWAITING_BROADCAST);
        user.setConversationPayload(null);
        telegramUserRepository.save(user);
    }

    @Transactional
    public void clearConversation(TelegramUserEntity user) {
        user.setConversationState(ConversationState.NONE);
        user.setConversationPayload(null);
        telegramUserRepository.save(user);
    }
}
