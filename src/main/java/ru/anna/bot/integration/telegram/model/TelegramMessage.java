package ru.anna.bot.integration.telegram.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TelegramMessage(
    @JsonProperty("message_id") Integer messageId,
    TelegramUser from,
    TelegramChat chat,
    String text
) {
}
