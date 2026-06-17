package ru.anna.bot.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.anna.bot.domain.PaymentEntity;
import ru.anna.bot.domain.PaymentStatus;
import ru.anna.bot.domain.TelegramUserEntity;

public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {

    @EntityGraph(attributePaths = "user")
    Optional<PaymentEntity> findByInternalPaymentId(String internalPaymentId);

    @EntityGraph(attributePaths = "user")
    Optional<PaymentEntity> findByYookassaPaymentId(String yookassaPaymentId);

    @EntityGraph(attributePaths = "user")
    List<PaymentEntity> findAllByStatusOrderByCreatedAtDesc(PaymentStatus status);

    @EntityGraph(attributePaths = "user")
    Optional<PaymentEntity> findFirstByUserOrderByCreatedAtDesc(TelegramUserEntity user);
}
