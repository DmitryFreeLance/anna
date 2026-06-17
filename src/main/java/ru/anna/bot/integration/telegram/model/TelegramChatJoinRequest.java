package ru.anna.bot.integration.telegram.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TelegramChatJoinRequest(
    TelegramChat chat,
    TelegramUser from,
    @JsonProperty("user_chat_id") Long userChatId
) {
}
