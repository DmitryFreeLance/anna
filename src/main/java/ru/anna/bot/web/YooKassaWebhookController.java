package ru.anna.bot.web;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.anna.bot.domain.PaymentEntity;
import ru.anna.bot.integration.yookassa.YooKassaClient;
import ru.anna.bot.integration.yookassa.model.YooKassaPaymentResponse;
import ru.anna.bot.service.PaymentService;
import ru.anna.bot.service.TelegramBotService;

@RestController
public class YooKassaWebhookController {

    private static final Logger log = LoggerFactory.getLogger(YooKassaWebhookController.class);

    private final PaymentService paymentService;
    private final YooKassaClient yooKassaClient;
    private final TelegramBotService telegramBotService;

    public YooKassaWebhookController(
        PaymentService paymentService,
        YooKassaClient yooKassaClient,
        TelegramBotService telegramBotService
    ) {
        this.paymentService = paymentService;
        this.yooKassaClient = yooKassaClient;
        this.telegramBotService = telegramBotService;
    }

    @PostMapping("${app.yookassa.webhook-path}")
    public ResponseEntity<String> handle(@RequestBody JsonNode payload) {
        String event = payload.path("event").asText("");
        String paymentId = payload.path("object").path("id").asText("");
        if (paymentId.isBlank()) {
            return ResponseEntity.ok("ok");
        }

        try {
            YooKassaPaymentResponse actualPayment = yooKassaClient.getPayment(paymentId);
            PaymentEntity payment = paymentService.findByYooKassaPaymentId(paymentId).orElse(null);
            if (payment == null) {
                log.warn("Webhook received for unknown payment {}", paymentId);
                return ResponseEntity.ok("ok");
            }

            if ("payment.succeeded".equalsIgnoreCase(event) || "succeeded".equalsIgnoreCase(actualPayment.status())) {
                paymentService.markSucceeded(payment);
                telegramBotService.notifyPaymentSucceeded(payment, false);
            } else if ("payment.canceled".equalsIgnoreCase(event) || "canceled".equalsIgnoreCase(actualPayment.status())) {
                paymentService.markCanceled(payment);
                telegramBotService.notifyPaymentCanceled(payment);
            }
        } catch (Exception exception) {
            log.error("Failed to process YooKassa webhook", exception);
        }
        return ResponseEntity.ok("ok");
    }
}
