package ru.anna.bot.integration.yookassa.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record YooKassaAmount(String value, String currency) {
}
