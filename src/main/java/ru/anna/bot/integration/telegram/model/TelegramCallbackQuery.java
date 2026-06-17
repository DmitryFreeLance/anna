package ru.anna.bot.integration.telegram.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TelegramCallbackQuery(
    String id,
    TelegramUser from,
    TelegramMessage message,
    String data
) {
}
