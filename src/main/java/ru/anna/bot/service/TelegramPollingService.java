package ru.anna.bot.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.anna.bot.config.AppProperties;
import ru.anna.bot.integration.telegram.TelegramApiClient;
import ru.anna.bot.integration.telegram.model.TelegramUpdate;

@Service
public class TelegramPollingService {

    private static final Logger log = LoggerFactory.getLogger(TelegramPollingService.class);
    private static final String OFFSET_KEY = "telegram.offset";

    private final AppProperties properties;
    private final AppStateService appStateService;
    private final TelegramApiClient telegramApiClient;
    private final TelegramBotService telegramBotService;

    public TelegramPollingService(
        AppProperties properties,
        AppStateService appStateService,
        TelegramApiClient telegramApiClient,
        TelegramBotService telegramBotService
    ) {
        this.properties = properties;
        this.appStateService = appStateService;
        this.telegramApiClient = telegramApiClient;
        this.telegramBotService = telegramBotService;
    }

    @Scheduled(fixedDelayString = "${app.telegram.poll-delay-ms}")
    public void poll() {
        if (properties.getTelegram().getBotToken().isBlank()) {
            return;
        }

        long offset = appStateService.getLong(OFFSET_KEY, 0L);
        try {
            List<TelegramUpdate> updates = telegramApiClient.getUpdates(offset, properties.getTelegram().getPollTimeoutSeconds());
            for (TelegramUpdate update : updates) {
                try {
                    telegramBotService.processUpdate(update);
                } catch (Exception exception) {
                    log.error("Failed to process update {}", update.updateId(), exception);
                } finally {
                    appStateService.putLong(OFFSET_KEY, update.updateId() + 1);
                }
            }
        } catch (Exception exception) {
            log.error("Telegram polling failed", exception);
        }
    }
}
