package ru.anna.bot.integration.telegram.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TelegramApiResponse<T>(boolean ok, T result, String description) {
}
