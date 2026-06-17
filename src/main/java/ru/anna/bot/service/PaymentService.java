package ru.anna.bot.service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.anna.bot.domain.PaymentEntity;
import ru.anna.bot.domain.PaymentStatus;
import ru.anna.bot.domain.TariffEntity;
import ru.anna.bot.domain.TelegramUserEntity;
import ru.anna.bot.integration.yookassa.model.YooKassaPaymentResponse;
import ru.anna.bot.repository.PaymentRepository;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final Clock clock;

    public PaymentService(PaymentRepository paymentRepository, Clock clock) {
        this.paymentRepository = paymentRepository;
        this.clock = clock;
    }

    @Transactional
    public PaymentEntity createPendingPayment(TelegramUserEntity user, TariffEntity tariff) {
        PaymentEntity payment = new PaymentEntity();
        payment.setInternalPaymentId(UUID.randomUUID().toString());
        payment.setUser(user);
        payment.setTariffCode(tariff.getCode());
        payment.setTariffTitle(tariff.getTitle());
        payment.setDurationDays(tariff.getDurationDays());
        payment.setAmountMinor(tariff.getPriceMinor());
        payment.setReceiptEmail(user.getEmail());
        payment.setStatus(PaymentStatus.PENDING);
        return paymentRepository.save(payment);
    }

    @Transactional
    public PaymentEntity updateFromCreateResponse(PaymentEntity payment, YooKassaPaymentResponse response) {
        payment.setYookassaPaymentId(response.id());
        if (response.confirmation() != null) {
            payment.setConfirmationUrl(response.confirmation().confirmationUrl());
        }
        if ("succeeded".equalsIgnoreCase(response.status())) {
            payment.setStatus(PaymentStatus.SUCCEEDED);
            payment.setPaidAt(Instant.now(clock));
        } else if ("canceled".equalsIgnoreCase(response.status())) {
            payment.setStatus(PaymentStatus.CANCELED);
        }
        return paymentRepository.save(payment);
    }

    @Transactional(readOnly = true)
    public Optional<PaymentEntity> findByYooKassaPaymentId(String yookassaPaymentId) {
        return paymentRepository.findByYookassaPaymentId(yookassaPaymentId);
    }

    @Transactional(readOnly = true)
    public Optional<PaymentEntity> findByInternalPaymentId(String internalPaymentId) {
        return paymentRepository.findByInternalPaymentId(internalPaymentId);
    }

    @Transactional(readOnly = true)
    public Optional<PaymentEntity> findLatestForUser(TelegramUserEntity user) {
        return paymentRepository.findFirstByUserOrderByCreatedAtDesc(user);
    }

    @Transactional(readOnly = true)
    public List<PaymentEntity> findSuccessfulPayments() {
        return paymentRepository.findAllByStatusOrderByCreatedAtDesc(PaymentStatus.SUCCEEDED);
    }

    @Transactional
    public PaymentEntity markSucceeded(PaymentEntity payment) {
        payment.setStatus(PaymentStatus.SUCCEEDED);
        if (payment.getPaidAt() == null) {
            payment.setPaidAt(Instant.now(clock));
        }
        return paymentRepository.save(payment);
    }

    @Transactional
    public PaymentEntity markCanceled(PaymentEntity payment) {
        payment.setStatus(PaymentStatus.CANCELED);
        return paymentRepository.save(payment);
    }

    @Transactional
    public PaymentEntity save(PaymentEntity payment) {
        return paymentRepository.save(payment);
    }
}
