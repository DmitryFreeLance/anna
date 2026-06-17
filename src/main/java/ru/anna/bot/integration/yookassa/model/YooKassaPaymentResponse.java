package ru.anna.bot.integration.yookassa.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record YooKassaPaymentResponse(
    String id,
    String status,
    Boolean paid,
    YooKassaAmount amount,
    YooKassaConfirmation confirmation,
    Map<String, String> metadata
) {
}
