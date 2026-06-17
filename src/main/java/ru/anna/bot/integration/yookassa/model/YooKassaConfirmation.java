package ru.anna.bot.integration.yookassa.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record YooKassaConfirmation(String type, @JsonProperty("confirmation_url") String confirmationUrl) {
}
