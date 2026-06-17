package ru.anna.bot.service;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.anna.bot.config.AppProperties;
import ru.anna.bot.domain.ConversationState;
import ru.anna.bot.domain.PaymentEntity;
import ru.anna.bot.domain.PaymentStatus;
import ru.anna.bot.domain.TariffCode;
import ru.anna.bot.domain.TariffEntity;
import ru.anna.bot.domain.TelegramUserEntity;
import ru.anna.bot.integration.telegram.TelegramApiClient;
import ru.anna.bot.integration.telegram.TelegramException;
import ru.anna.bot.integration.telegram.model.TelegramCallbackQuery;
import ru.anna.bot.integration.telegram.model.TelegramMessage;
import ru.anna.bot.integration.telegram.model.TelegramUpdate;
import ru.anna.bot.integration.yookassa.YooKassaClient;
import ru.anna.bot.integration.yookassa.YooKassaException;
import ru.anna.bot.integration.yookassa.model.YooKassaPaymentResponse;
import ru.anna.bot.util.EmailUtils;
import ru.anna.bot.util.FormattingUtils;

@Service
public class TelegramBotService {

    private static final Logger log = LoggerFactory.getLogger(TelegramBotService.class);

    private static final String START_TEXT = """
        Добро пожаловать!
        Стиль сближает - а чат стиля еще и объединяет 🫰🏼🤍

        Выбирай нужный тариф и присоединяйся в команду самых стильных девушек 💒
        """;

    private static final String VIP_TEXT = "🤍 Выберите желаемый для вас тарифный план:";
    private static final String PAYMENT_ERROR_TEXT = "Ошибка платежа. Попробуйте чуть позже.";

    private final AppProperties properties;
    private final UserService userService;
    private final TariffService tariffService;
    private final PaymentService paymentService;
    private final SubscriptionService subscriptionService;
    private final AdminService adminService;
    private final TelegramApiClient telegramApiClient;
    private final TelegramMarkupService telegramMarkupService;
    private final YooKassaClient yooKassaClient;
    private final ZoneId zoneId;
    private final Clock clock;

    public TelegramBotService(
        AppProperties properties,
        UserService userService,
        TariffService tariffService,
        PaymentService paymentService,
        SubscriptionService subscriptionService,
        AdminService adminService,
        TelegramApiClient telegramApiClient,
        TelegramMarkupService telegramMarkupService,
        YooKassaClient yooKassaClient,
        ZoneId zoneId,
        Clock clock
    ) {
        this.properties = properties;
        this.userService = userService;
        this.tariffService = tariffService;
        this.paymentService = paymentService;
        this.subscriptionService = subscriptionService;
        this.adminService = adminService;
        this.telegramApiClient = telegramApiClient;
        this.telegramMarkupService = telegramMarkupService;
        this.yooKassaClient = yooKassaClient;
        this.zoneId = zoneId;
        this.clock = clock;
    }

    @Transactional
    public void processUpdate(TelegramUpdate update) {
        if (update.message() != null) {
            processMessage(update.message());
        } else if (update.callbackQuery() != null) {
            processCallback(update.callbackQuery());
        }
    }

    @Transactional
    public void processMessage(TelegramMessage message) {
        if (message.chat() == null || !"private".equalsIgnoreCase(message.chat().type()) || message.from() == null) {
            return;
        }

        TelegramUserEntity user = userService.upsertPrivateUser(message.chat().id(), message.from());
        String text = message.text() == null ? "" : message.text().trim();

        if (user.getConversationState() == ConversationState.AWAITING_BROADCAST && isAdmin(user.getTelegramUserId())) {
            handleBroadcastMessage(user, message);
            return;
        }

        if ("/cancel".equalsIgnoreCase(text)) {
            userService.clearConversation(user);
            sendText(user.getPrivateChatId(), "Операция отменена ✨");
            return;
        }

        if (user.getConversationState() == ConversationState.AWAITING_EMAIL) {
            handleEmailInput(user, text);
            return;
        }

        if (user.getConversationState() == ConversationState.AWAITING_TARIFF_PRICE && isAdmin(user.getTelegramUserId())) {
            handleTariffPriceInput(user, text);
            return;
        }

        if (text.startsWith("/start")) {
            sendStart(user.getPrivateChatId());
            return;
        }

        if ("/vip".equalsIgnoreCase(text) || "📚 Перейти к гайдам и тарифу".equals(text)) {
            sendVipMenu(user.getPrivateChatId());
            return;
        }

        if ("/admin".equalsIgnoreCase(text)) {
            if (!isAdmin(user.getTelegramUserId())) {
                sendText(user.getPrivateChatId(), "Эта команда доступна только администраторам.");
                return;
            }
            sendAdminMenu(user.getPrivateChatId());
            return;
        }

        if ("/subscription".equalsIgnoreCase(text) || "💎 Подписка".equals(text)) {
            sendSubscriptionStatus(user);
            return;
        }

        if ("💬 Обратная связь".equals(text)) {
            telegramApiClient.sendMessage(
                user.getPrivateChatId(),
                "Для обратной связи используйте кнопку ниже 👇",
                telegramMarkupService.feedbackKeyboard(properties.getTelegram().getSupportUsername())
            );
            return;
        }

        sendText(user.getPrivateChatId(), "Выбери действие в меню ниже 👇");
    }

    @Transactional
    public void processCallback(TelegramCallbackQuery callbackQuery) {
        if (callbackQuery.message() == null || callbackQuery.message().chat() == null || callbackQuery.from() == null) {
            return;
        }
        if (!"private".equalsIgnoreCase(callbackQuery.message().chat().type())) {
            return;
        }

        TelegramUserEntity user = userService.upsertPrivateUser(callbackQuery.message().chat().id(), callbackQuery.from());
        String data = callbackQuery.data() == null ? "" : callbackQuery.data();

        try {
            if ("start:open".equals(data)) {
                sendVipMenu(user.getPrivateChatId());
                telegramApiClient.answerCallbackQuery(callbackQuery.id(), "Открываю тарифы ✨");
                return;
            }

            if (data.startsWith("tariff:")) {
                TariffCode code = TariffCode.valueOf(data.substring("tariff:".length()));
                handleTariffSelection(user, code);
                telegramApiClient.answerCallbackQuery(callbackQuery.id(), "Продолжаем оплату 💳");
                return;
            }

            if (data.startsWith("payment:check:")) {
                String internalPaymentId = data.substring("payment:check:".length());
                handlePaymentCheck(user, internalPaymentId);
                telegramApiClient.answerCallbackQuery(callbackQuery.id(), "Проверяю статус оплаты...");
                return;
            }

            if ("invite:refresh".equals(data)) {
                handleInviteRefresh(user);
                telegramApiClient.answerCallbackQuery(callbackQuery.id(), "Обновляю ссылку...");
                return;
            }

            if (data.startsWith("admin:") && isAdmin(user.getTelegramUserId())) {
                handleAdminCallback(user, data);
                telegramApiClient.answerCallbackQuery(callbackQuery.id(), "Готово ✨");
                return;
            }
        } catch (Exception exception) {
            log.error("Failed to handle callback {}", data, exception);
            telegramApiClient.answerCallbackQuery(callbackQuery.id(), "Не удалось обработать действие");
            sendText(user.getPrivateChatId(), "Что-то пошло не так. Попробуйте чуть позже.");
            return;
        }

        telegramApiClient.answerCallbackQuery(callbackQuery.id(), null);
    }

    public void notifyPaymentSucceeded(PaymentEntity payment, boolean forceInvite) {
        boolean shouldSendInvite = forceInvite || payment.getSubscriptionAppliedAt() == null;
        TelegramUserEntity user = subscriptionService.activatePayment(payment);
        paymentService.markSucceeded(payment);
        if (!shouldSendInvite) {
            return;
        }

        sendInviteMessage(
            user,
            "Оплата прошла успешно ✅\nПодписка активна до " +
                FormattingUtils.formatDateTime(user.getSubscriptionEndsAt(), zoneId) +
                "\nНиже твоя ссылка в закрытый чат 💒"
        );
    }

    public void notifyPaymentCanceled(PaymentEntity payment) {
        sendText(payment.getUser().getPrivateChatId(), "Платеж был отменен. Если хочешь, можно оформить его заново 👇");
        sendVipMenu(payment.getUser().getPrivateChatId());
    }

    public void sendSubscriptionReminder(TelegramUserEntity user) {
        if (user.getSubscriptionEndsAt() == null) {
            return;
        }
        telegramApiClient.sendMessage(
            user.getPrivateChatId(),
            "Напоминаю: подписка закончится через 3 дня ⏰\nОна активна до " +
                FormattingUtils.formatDateTime(user.getSubscriptionEndsAt(), zoneId) +
                "\nПродлить можно по кнопкам ниже.",
            telegramMarkupService.tariffsKeyboard(tariffService.findAllOrdered())
        );
    }

    public void notifySubscriptionExpired(TelegramUserEntity user) {
        telegramApiClient.sendMessage(
            user.getPrivateChatId(),
            "Подписка закончилась 💔\nЧтобы вернуться в закрытый чат, продли ее по одному из тарифов ниже.",
            telegramMarkupService.tariffsKeyboard(tariffService.findAllOrdered())
        );
    }

    private void handleTariffSelection(TelegramUserEntity user, TariffCode code) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            userService.setAwaitingEmail(user, code);
            sendText(
                user.getPrivateChatId(),
                "Для оплаты и отправки чека пришли, пожалуйста, email 📩"
            );
            return;
        }
        createPaymentAndSendLink(user, code);
    }

    private void handleEmailInput(TelegramUserEntity user, String text) {
        if (!EmailUtils.isValid(text)) {
            sendText(user.getPrivateChatId(), "Похоже, это не email. Пришли адрес в формате name@example.com");
            return;
        }

        TariffCode tariffCode = TariffCode.valueOf(user.getConversationPayload());
        user.setEmail(text.trim());
        userService.clearConversation(user);
        userService.save(user);
        createPaymentAndSendLink(user, tariffCode);
    }

    private void handleTariffPriceInput(TelegramUserEntity user, String text) {
        try {
            TariffCode tariffCode = TariffCode.valueOf(user.getConversationPayload());
            long priceMinor = FormattingUtils.parseRublesToMinor(text);
            TariffEntity updated = tariffService.updatePrice(tariffCode, priceMinor);
            userService.clearConversation(user);
            sendText(user.getPrivateChatId(), "Цена обновлена: " + updated.getTitle() + " — " + FormattingUtils.formatMoney(updated.getPriceMinor()));
            sendAdminMenu(user.getPrivateChatId());
        } catch (Exception exception) {
            sendText(user.getPrivateChatId(), "Не смогла распознать цену. Пример: 1490 или 1490.00");
        }
    }

    private void handleBroadcastMessage(TelegramUserEntity admin, TelegramMessage message) {
        List<TelegramUserEntity> users = userService.findAllUsers();
        int success = 0;
        int failed = 0;
        for (TelegramUserEntity user : users) {
            try {
                telegramApiClient.copyMessage(user.getPrivateChatId(), message.chat().id(), message.messageId());
                success++;
            } catch (TelegramException exception) {
                failed++;
                log.warn("Broadcast failed for user {}", user.getTelegramUserId(), exception);
            }
        }
        userService.clearConversation(admin);
        sendText(admin.getPrivateChatId(), "Рассылка завершена 📣\nУспешно: " + success + "\nС ошибкой: " + failed);
        sendAdminMenu(admin.getPrivateChatId());
    }

    private void handlePaymentCheck(TelegramUserEntity user, String internalPaymentId) {
        try {
            PaymentEntity payment = paymentService.findByInternalPaymentId(internalPaymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found"));
            YooKassaPaymentResponse response = yooKassaClient.getPayment(payment.getYookassaPaymentId());
            if ("succeeded".equalsIgnoreCase(response.status()) || Boolean.TRUE.equals(response.paid())) {
                paymentService.markSucceeded(payment);
                notifyPaymentSucceeded(payment, true);
                return;
            }
            if ("canceled".equalsIgnoreCase(response.status())) {
                paymentService.markCanceled(payment);
                notifyPaymentCanceled(payment);
                return;
            }
        } catch (YooKassaException exception) {
            log.warn("Payment status check failed for {}", internalPaymentId, exception);
            sendText(user.getPrivateChatId(), PAYMENT_ERROR_TEXT);
            return;
        } catch (Exception exception) {
            log.warn("Unexpected payment status check error for {}", internalPaymentId, exception);
            sendText(user.getPrivateChatId(), PAYMENT_ERROR_TEXT);
            return;
        }
        sendText(user.getPrivateChatId(), "Платеж пока еще не завершен. После оплаты нажми кнопку проверки еще раз ✨");
    }

    private void handleInviteRefresh(TelegramUserEntity user) {
        if (!subscriptionService.hasActiveSubscription(user)) {
            sendText(user.getPrivateChatId(), "Активной подписки нет. Открыть тарифы можно через /vip ✨");
            return;
        }
        sendInviteMessage(
            user,
            "Вот обновленная ссылка в закрытый чат 💒\nПодписка активна до " +
                FormattingUtils.formatDateTime(user.getSubscriptionEndsAt(), zoneId)
        );
    }

    private void handleAdminCallback(TelegramUserEntity user, String data) {
        switch (data) {
            case "admin:tariffs" -> telegramApiClient.sendMessage(
                user.getPrivateChatId(),
                "Выбери тариф, который нужно изменить 💸",
                telegramMarkupService.adminTariffsMenu(tariffService.findAllOrdered())
            );
            case "admin:stats" -> sendLongText(user.getPrivateChatId(), adminService.statsText());
            case "admin:paid" -> sendLongText(user.getPrivateChatId(), adminService.paidUsersText());
            case "admin:users" -> sendLongText(user.getPrivateChatId(), adminService.allUsersText());
            case "admin:broadcast" -> {
                userService.setAwaitingBroadcast(user);
                sendText(user.getPrivateChatId(), "Пришли следующим сообщением текст, фото или другой пост, который нужно разослать всем пользователям 📣");
            }
            case "admin:back" -> sendAdminMenu(user.getPrivateChatId());
            default -> {
                if (data.startsWith("admin:price:")) {
                    TariffCode tariffCode = TariffCode.valueOf(data.substring("admin:price:".length()));
                    userService.setAwaitingTariffPrice(user, tariffCode);
                    sendText(user.getPrivateChatId(), "Пришли новую цену для тарифа в рублях. Пример: 1290");
                }
            }
        }
    }

    private void createPaymentAndSendLink(TelegramUserEntity user, TariffCode code) {
        try {
            TariffEntity tariff = tariffService.getRequired(code);
            PaymentEntity payment = paymentService.createPendingPayment(user, tariff);
            YooKassaPaymentResponse response = yooKassaClient.createPayment(payment, tariff, properties.getTelegram().getBotUsername());
            payment = paymentService.updateFromCreateResponse(payment, response);

            if (payment.getStatus() == PaymentStatus.SUCCEEDED) {
                notifyPaymentSucceeded(payment, true);
                return;
            }

            telegramApiClient.sendMessage(
                user.getPrivateChatId(),
                "Тариф: " + tariff.getTitle() + "\nСтоимость: " + FormattingUtils.formatMoney(tariff.getPriceMinor()) +
                    "\nПосле оплаты я автоматически отправлю приглашение в закрытый чат 💒",
                telegramMarkupService.paymentKeyboard(payment.getConfirmationUrl(), payment.getInternalPaymentId())
            );
        } catch (YooKassaException exception) {
            log.warn("Payment creation failed for user {}", user.getTelegramUserId(), exception);
            sendText(user.getPrivateChatId(), PAYMENT_ERROR_TEXT);
        } catch (Exception exception) {
            log.warn("Unexpected payment creation error for user {}", user.getTelegramUserId(), exception);
            sendText(user.getPrivateChatId(), PAYMENT_ERROR_TEXT);
        }
    }

    private void sendVipMenu(Long chatId) {
        telegramApiClient.sendMessage(chatId, VIP_TEXT, telegramMarkupService.tariffsKeyboard(tariffService.findAllOrdered()));
    }

    private void sendStart(Long chatId) {
        try {
            telegramApiClient.sendPhoto(
                chatId,
                properties.getTelegram().getStartPhotoPath(),
                START_TEXT,
                telegramMarkupService.tariffsKeyboard(tariffService.findAllOrdered())
            );
        } catch (Exception exception) {
            log.warn("Failed to send start photo, falling back to text", exception);
            telegramApiClient.sendMessage(chatId, START_TEXT, telegramMarkupService.tariffsKeyboard(tariffService.findAllOrdered()));
        }
    }

    private void sendAdminMenu(Long chatId) {
        telegramApiClient.sendMessage(chatId, "Админ-панель ⚙️", telegramMarkupService.adminMenu());
    }

    private void sendSubscriptionStatus(TelegramUserEntity user) {
        if (subscriptionService.hasActiveSubscription(user)) {
            sendInviteMessage(
                user,
                "Текущая подписка активна ✅\nДо: " +
                    FormattingUtils.formatDateTime(user.getSubscriptionEndsAt(), zoneId) +
                    "\nНиже актуальная ссылка в закрытый чат."
            );
            return;
        }
        PaymentEntity latestPayment = paymentService.findLatestForUser(user).orElse(null);
        if (latestPayment != null && latestPayment.getStatus() == PaymentStatus.PENDING) {
            telegramApiClient.sendMessage(
                user.getPrivateChatId(),
                "У вас есть незавершенная оплата.",
                telegramMarkupService.paymentKeyboard(latestPayment.getConfirmationUrl(), latestPayment.getInternalPaymentId())
            );
            return;
        }
        if (user.getSubscriptionEndsAt() != null) {
            sendText(
                user.getPrivateChatId(),
                "Срок подписки закончился ❌\nПоследняя дата окончания: " +
                    FormattingUtils.formatDateTime(user.getSubscriptionEndsAt(), zoneId)
            );
            return;
        }
        sendText(user.getPrivateChatId(), "Активной подписки пока нет. Открыть тарифы можно через /vip ✨");
    }

    private void sendInviteMessage(TelegramUserEntity user, String text) {
        String inviteLink = telegramApiClient.createInviteLink(
            properties.getTelegram().getPrivateChatId(),
            "Подписка " + (user.getSubscriptionTariffCode() == null ? "чат" : user.getSubscriptionTariffCode().name())
        );
        telegramApiClient.sendMessage(
            user.getPrivateChatId(),
            text,
            telegramMarkupService.inviteKeyboard(inviteLink)
        );
    }

    private void sendText(Long chatId, String text) {
        telegramApiClient.sendMessage(chatId, text, telegramMarkupService.replyKeyboard());
    }

    private void sendLongText(Long chatId, String text) {
        for (String chunk : FormattingUtils.splitForTelegram(text)) {
            telegramApiClient.sendMessage(chatId, chunk, telegramMarkupService.replyKeyboard());
        }
    }

    private boolean isAdmin(Long telegramUserId) {
        return properties.getTelegram().getAdminIds().contains(telegramUserId);
    }
}
