package ru.anna.bot.service;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import ru.anna.bot.domain.TariffEntity;
import ru.anna.bot.integration.telegram.TelegramApiClient;
import ru.anna.bot.integration.telegram.model.InlineKeyboardButton;
import ru.anna.bot.integration.telegram.model.InlineKeyboardMarkup;
import ru.anna.bot.integration.telegram.model.ReplyKeyboardMarkup;
import ru.anna.bot.util.FormattingUtils;

@Service
public class TelegramMarkupService {

    private final TelegramApiClient telegramApiClient;

    public TelegramMarkupService(TelegramApiClient telegramApiClient) {
        this.telegramApiClient = telegramApiClient;
    }

    public ReplyKeyboardMarkup replyKeyboard() {
        return telegramApiClient.defaultReplyKeyboard();
    }

    public InlineKeyboardMarkup tariffsKeyboard(List<TariffEntity> tariffs) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        for (TariffEntity tariff : tariffs) {
            rows.add(List.of(
                InlineKeyboardButton.callback(
                    priceLabel(tariff),
                    "tariff:" + tariff.getCode().name()
                )
            ));
        }
        return new InlineKeyboardMarkup(rows);
    }

    public InlineKeyboardMarkup paymentKeyboard(String payUrl, String internalPaymentId) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        if (payUrl != null && !payUrl.isBlank()) {
            rows.add(List.of(InlineKeyboardButton.url("💳 Оплатить", payUrl)));
        }
        rows.add(List.of(InlineKeyboardButton.callback("🔄 Обновить ссылку", "payment:refresh:" + internalPaymentId)));
        rows.add(List.of(InlineKeyboardButton.callback("✅ Проверить оплату", "payment:check:" + internalPaymentId)));
        return new InlineKeyboardMarkup(rows);
    }

    public InlineKeyboardMarkup adminMenu() {
        return new InlineKeyboardMarkup(List.of(
            List.of(
                InlineKeyboardButton.callback("💸 Тарифы", "admin:tariffs"),
                InlineKeyboardButton.callback("📊 Статистика", "admin:stats")
            ),
            List.of(
                InlineKeyboardButton.callback("✅ Оплатившие", "admin:paid"),
                InlineKeyboardButton.callback("👥 Все пользователи", "admin:users")
            ),
            List.of(InlineKeyboardButton.callback("📣 Рассылка", "admin:broadcast"))
        ));
    }

    public InlineKeyboardMarkup adminTariffsMenu(List<TariffEntity> tariffs) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        for (TariffEntity tariff : tariffs) {
            rows.add(List.of(
                InlineKeyboardButton.callback("✏️ " + priceLabel(tariff), "admin:price:" + tariff.getCode().name())
            ));
        }
        rows.add(List.of(InlineKeyboardButton.callback("↩️ Назад", "admin:back")));
        return new InlineKeyboardMarkup(rows);
    }

    public InlineKeyboardMarkup inviteKeyboard(String inviteUrl) {
        return new InlineKeyboardMarkup(List.of(
            List.of(InlineKeyboardButton.url("💒 Войти в закрытый чат", inviteUrl)),
            List.of(InlineKeyboardButton.callback("✨ Продлить подписку", "start:open"))
        ));
    }

    public InlineKeyboardMarkup feedbackKeyboard(String supportUsername) {
        return new InlineKeyboardMarkup(List.of(
            List.of(InlineKeyboardButton.url("💌 Написать", "https://t.me/" + supportUsername.replace("@", "")))
        ));
    }

    private String priceLabel(TariffEntity tariff) {
        return switch (tariff.getCode()) {
            case MONTH_1 -> "1 месяц " + FormattingUtils.formatMoney(tariff.getPriceMinor());
            case MONTH_3 -> "3 месяца " + FormattingUtils.formatMoney(tariff.getPriceMinor());
            case MONTH_6 -> "6 месяцев " + FormattingUtils.formatMoney(tariff.getPriceMinor());
        };
    }
}
