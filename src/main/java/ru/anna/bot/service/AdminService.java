package ru.anna.bot.service;

import java.time.ZoneId;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.anna.bot.domain.PaymentEntity;
import ru.anna.bot.domain.TelegramUserEntity;
import ru.anna.bot.util.FormattingUtils;

@Service
public class AdminService {

    private final UserService userService;
    private final PaymentService paymentService;
    private final SubscriptionService subscriptionService;
    private final ZoneId zoneId;

    public AdminService(
        UserService userService,
        PaymentService paymentService,
        SubscriptionService subscriptionService,
        ZoneId zoneId
    ) {
        this.userService = userService;
        this.paymentService = paymentService;
        this.subscriptionService = subscriptionService;
        this.zoneId = zoneId;
    }

    @Transactional(readOnly = true)
    public String statsText() {
        List<TelegramUserEntity> users = userService.findAllUsers();
        long activeSubscriptions = users.stream().filter(subscriptionService::hasActiveSubscription).count();
        List<PaymentEntity> successfulPayments = paymentService.findSuccessfulPayments();
        long revenue = successfulPayments.stream().mapToLong(PaymentEntity::getAmountMinor).sum();

        return """
            📊 Статистика

            👥 Всего пользователей: %d
            ✅ Активных подписок: %d
            💳 Успешных оплат: %d
            💰 Выручка: %s
            """.formatted(
            users.size(),
            activeSubscriptions,
            successfulPayments.size(),
            FormattingUtils.formatMoney(revenue)
        ).trim();
    }

    @Transactional(readOnly = true)
    public String paidUsersText() {
        List<PaymentEntity> successfulPayments = paymentService.findSuccessfulPayments();
        Set<Long> seenUsers = new LinkedHashSet<>();
        StringBuilder builder = new StringBuilder("✅ Оплатившие пользователи\n\n");
        int index = 1;
        for (PaymentEntity payment : successfulPayments) {
            TelegramUserEntity user = payment.getUser();
            if (!seenUsers.add(user.getTelegramUserId())) {
                continue;
            }
            builder.append(index++)
                .append(". ")
                .append(FormattingUtils.userDisplay(user.getFirstName(), user.getUsername(), user.getTelegramUserId()))
                .append(" | ")
                .append(user.getEmail() == null ? "email не указан" : user.getEmail());
            if (user.getSubscriptionEndsAt() != null) {
                builder.append(" | до ")
                    .append(FormattingUtils.formatDateTime(user.getSubscriptionEndsAt(), zoneId));
            }
            builder.append('\n');
        }
        if (seenUsers.isEmpty()) {
            builder.append("Пока никто не оплатил.");
        }
        return builder.toString().trim();
    }

    @Transactional(readOnly = true)
    public String allUsersText() {
        List<TelegramUserEntity> users = userService.findAllUsers();
        StringBuilder builder = new StringBuilder("👥 Все пользователи\n\n");
        int index = 1;
        for (TelegramUserEntity user : users) {
            builder.append(index++)
                .append(". ")
                .append(FormattingUtils.userDisplay(user.getFirstName(), user.getUsername(), user.getTelegramUserId()));
            if (user.getSubscriptionEndsAt() != null) {
                builder.append(" | подписка до ")
                    .append(FormattingUtils.formatDateTime(user.getSubscriptionEndsAt(), zoneId));
            }
            builder.append('\n');
        }
        if (users.isEmpty()) {
            builder.append("Пользователей пока нет.");
        }
        return builder.toString().trim();
    }
}
