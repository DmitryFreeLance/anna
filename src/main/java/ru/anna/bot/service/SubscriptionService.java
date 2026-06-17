package ru.anna.bot.service;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.anna.bot.domain.PaymentEntity;
import ru.anna.bot.domain.TelegramUserEntity;
import ru.anna.bot.repository.TelegramUserRepository;

@Service
public class SubscriptionService {

    private final TelegramUserRepository telegramUserRepository;
    private final Clock clock;

    public SubscriptionService(TelegramUserRepository telegramUserRepository, Clock clock) {
        this.telegramUserRepository = telegramUserRepository;
        this.clock = clock;
    }

    @Transactional
    public TelegramUserEntity activatePayment(PaymentEntity payment) {
        if (payment.getSubscriptionAppliedAt() != null) {
            return payment.getUser();
        }

        TelegramUserEntity user = payment.getUser();
        Instant now = Instant.now(clock);
        Instant base = user.getSubscriptionEndsAt() != null && user.getSubscriptionEndsAt().isAfter(now)
            ? user.getSubscriptionEndsAt()
            : now;

        Instant newEnd = base.plus(payment.getDurationDays(), ChronoUnit.DAYS);
        user.setSubscriptionEndsAt(newEnd);
        user.setSubscriptionTariffCode(payment.getTariffCode());
        user.setSubscriptionManaged(true);
        user.setReminderSentForEndsAt(null);
        user.setExpirationProcessedForEndsAt(null);
        user.setLastSuccessfulPaymentAt(now);
        payment.setSubscriptionAppliedAt(now);
        telegramUserRepository.save(user);
        return user;
    }

    @Transactional(readOnly = true)
    public boolean hasActiveSubscription(TelegramUserEntity user) {
        return user.getSubscriptionEndsAt() != null && user.getSubscriptionEndsAt().isAfter(Instant.now(clock));
    }

    @Transactional(readOnly = true)
    public List<TelegramUserEntity> findAllUsers() {
        return telegramUserRepository.findAllByOrderByCreatedAtAsc();
    }
}
