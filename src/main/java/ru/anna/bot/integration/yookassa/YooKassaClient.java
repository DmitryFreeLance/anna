package ru.anna.bot.integration.yookassa;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import ru.anna.bot.config.AppProperties;
import ru.anna.bot.domain.PaymentEntity;
import ru.anna.bot.domain.TariffEntity;
import ru.anna.bot.integration.yookassa.model.YooKassaPaymentResponse;

@Component
public class YooKassaClient {

    private static final Logger log = LoggerFactory.getLogger(YooKassaClient.class);

    private final RestClient restClient;
    private final AppProperties properties;

    public YooKassaClient(AppProperties properties) {
        this.properties = properties;
        this.restClient = RestClient.builder()
            .baseUrl("https://api.yookassa.ru/v3")
            .defaultHeaders(headers -> headers.setBasicAuth(
                properties.getYookassa().getShopId(),
                properties.getYookassa().getSecretKey()
            ))
            .build();
    }

    public YooKassaPaymentResponse createPayment(PaymentEntity payment, TariffEntity tariff, String botUsername) {
        ensureConfigured();

        Map<String, Object> request = new LinkedHashMap<>();
        request.put("amount", amountMap(payment.getAmountMinor()));
        request.put("capture", true);
        request.put("description", "Подписка на чат стиля: " + tariff.getTitle());
        request.put("confirmation", Map.of(
            "type", "redirect",
            "return_url", resolveReturnUrl(botUsername)
        ));
        request.put("metadata", Map.of(
            "internal_payment_id", payment.getInternalPaymentId(),
            "telegram_user_id", String.valueOf(payment.getUser().getTelegramUserId()),
            "tariff_code", payment.getTariffCode().name()
        ));
        request.put("receipt", receiptMap(payment, tariff));

        try {
            return restClient.post()
                .uri("/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Idempotence-Key", UUID.randomUUID().toString())
                .body(request)
                .retrieve()
                .body(YooKassaPaymentResponse.class);
        } catch (Exception exception) {
            log.error("Failed to create YooKassa payment");
            throw new YooKassaException("Не удалось создать платеж в ЮKassa", exception);
        }
    }

    public YooKassaPaymentResponse getPayment(String paymentId) {
        ensureConfigured();
        try {
            return restClient.get()
                .uri("/payments/{paymentId}", paymentId)
                .retrieve()
                .body(YooKassaPaymentResponse.class);
        } catch (Exception exception) {
            throw new YooKassaException("Не удалось получить платеж из ЮKassa", exception);
        }
    }

    private Map<String, Object> receiptMap(PaymentEntity payment, TariffEntity tariff) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("description", "Подписка на закрытый чат стиля: " + tariff.getTitle());
        item.put("quantity", 1.0);
        item.put("amount", amountMap(payment.getAmountMinor()));
        item.put("vat_code", properties.getYookassa().getVatCode());
        item.put("payment_subject", properties.getYookassa().getPaymentSubject());
        item.put("payment_mode", properties.getYookassa().getPaymentMode());

        Map<String, Object> receipt = new LinkedHashMap<>();
        receipt.put("customer", Map.of("email", payment.getReceiptEmail()));
        receipt.put("items", List.of(item));
        if (properties.getYookassa().getTaxSystemCode() != null) {
            receipt.put("tax_system_code", properties.getYookassa().getTaxSystemCode());
        }
        return receipt;
    }

    private Map<String, Object> amountMap(long amountMinor) {
        return Map.of(
            "value", BigDecimal.valueOf(amountMinor, 2).toPlainString(),
            "currency", "RUB"
        );
    }

    private String resolveReturnUrl(String botUsername) {
        String configured = properties.getYookassa().getReturnUrl();
        if (configured != null && !configured.isBlank()) {
            return configured;
        }
        return "https://t.me/" + botUsername;
    }

    private void ensureConfigured() {
        if (!properties.getYookassa().isEnabled()) {
            throw new YooKassaException("ЮKassa отключена в конфигурации");
        }
        if (properties.getYookassa().getShopId().isBlank() || properties.getYookassa().getSecretKey().isBlank()) {
            throw new YooKassaException("Не заполнены YOOKASSA_SHOP_ID или YOOKASSA_SECRET_KEY");
        }
    }
}
