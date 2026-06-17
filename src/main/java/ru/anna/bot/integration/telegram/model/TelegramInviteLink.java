package ru.anna.bot.integration.telegram.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TelegramInviteLink(@JsonProperty("invite_link") String inviteLink) {
}
