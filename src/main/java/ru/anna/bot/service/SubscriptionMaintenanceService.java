package ru.anna.bot.service;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.anna.bot.config.AppProperties;
import ru.anna.bot.domain.TelegramUserEntity;
import ru.anna.bot.integration.telegram.TelegramApiClient;

@Service
public class SubscriptionMaintenanceService {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionMaintenanceService.class);

    private final UserService userService;
    private final SubscriptionService subscriptionService;
    private final TelegramBotService telegramBotService;
    private final TelegramApiClient telegramApiClient;
    private final AppProperties properties;
    private final Clock clock;

    public SubscriptionMaintenanceService(
        UserService userService,
        SubscriptionService subscriptionService,
        TelegramBotService telegramBotService,
        TelegramApiClient telegramApiClient,
        AppProperties properties,
        Clock clock
    ) {
        this.userService = userService;
        this.subscriptionService = subscriptionService;
        this.telegramBotService = telegramBotService;
        this.telegramApiClient = telegramApiClient;
        this.properties = properties;
        this.clock = clock;
    }

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void maintainSubscriptions() {
        if (properties.getTelegram().getBotToken().isBlank()) {
            return;
        }

        Instant now = Instant.now(clock);
        for (TelegramUserEntity user : userService.findAllUsers()) {
            processReminder(user, now);
            processExpiration(user, now);
        }
    }

    private void processReminder(TelegramUserEntity user, Instant now) {
        if (user.getSubscriptionEndsAt() == null || !user.getSubscriptionEndsAt().isAfter(now)) {
            return;
        }
        long daysLeft = ChronoUnit.DAYS.between(now, user.getSubscriptionEndsAt());
        if (daysLeft > 3 || daysLeft < 2) {
            return;
        }
        if (user.getReminderSentForEndsAt() != null && user.getReminderSentForEndsAt().equals(user.getSubscriptionEndsAt())) {
            return;
        }
        telegramBotService.sendSubscriptionReminder(user);
        user.setReminderSentForEndsAt(user.getSubscriptionEndsAt());
        userService.save(user);
    }

    private void processExpiration(TelegramUserEntity user, Instant now) {
        if (!user.isSubscriptionManaged() || user.getSubscriptionEndsAt() == null || user.getSubscriptionEndsAt().isAfter(now)) {
            return;
        }
        if (user.getExpirationProcessedForEndsAt() != null
            && user.getExpirationProcessedForEndsAt().equals(user.getSubscriptionEndsAt())) {
            return;
        }

        try {
            telegramApiClient.banChatMember(properties.getTelegram().getPrivateChatId(), user.getTelegramUserId());
            telegramApiClient.unbanChatMember(properties.getTelegram().getPrivateChatId(), user.getTelegramUserId());
        } catch (Exception exception) {
            log.warn("Failed to remove expired user {} from chat", user.getTelegramUserId(), exception);
        }

        telegramBotService.notifySubscriptionExpired(user);
        user.setExpirationProcessedForEndsAt(user.getSubscriptionEndsAt());
        userService.save(user);
    }
}
