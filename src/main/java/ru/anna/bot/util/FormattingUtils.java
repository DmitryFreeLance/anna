package ru.anna.bot.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public final class FormattingUtils {

    private static final Locale RU_LOCALE = Locale.forLanguageTag("ru-RU");

    private FormattingUtils() {
    }

    public static String formatMoney(long minor) {
        NumberFormat format = NumberFormat.getNumberInstance(RU_LOCALE);
        format.setMinimumFractionDigits(0);
        format.setMaximumFractionDigits(2);
        return format.format(BigDecimal.valueOf(minor, 2)) + "₽";
    }

    public static String formatDateTime(Instant instant, ZoneId zoneId) {
        return DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm", RU_LOCALE)
            .withZone(zoneId)
            .format(instant);
    }

    public static String userDisplay(String firstName, String username, Long id) {
        String displayName = firstName != null && !firstName.isBlank() ? firstName : "Без имени";
        String usernamePart = username != null && !username.isBlank() ? " (@" + username + ")" : "";
        return displayName + usernamePart + " [" + id + "]";
    }

    public static List<String> splitForTelegram(String text) {
        int limit = 3900;
        if (text.length() <= limit) {
            return List.of(text);
        }

        StringBuilder chunk = new StringBuilder();
        java.util.ArrayList<String> result = new java.util.ArrayList<>();
        for (String line : text.split("\n")) {
            if (chunk.length() + line.length() + 1 > limit) {
                result.add(chunk.toString().trim());
                chunk.setLength(0);
            }
            chunk.append(line).append('\n');
        }
        if (!chunk.isEmpty()) {
            result.add(chunk.toString().trim());
        }
        return result;
    }

    public static long parseRublesToMinor(String rawValue) {
        String normalized = rawValue
            .replace("₽", "")
            .replace("р", "")
            .replace("Р", "")
            .replace(" ", "")
            .replace(",", ".")
            .trim();
        BigDecimal value = new BigDecimal(normalized);
        if (value.signum() <= 0) {
            throw new IllegalArgumentException("Price must be positive");
        }
        return value.movePointRight(2).setScale(0, RoundingMode.HALF_UP).longValueExact();
    }
}
