package ru.anna.bot.util;

import java.util.regex.Pattern;

public final class EmailUtils {

    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private EmailUtils() {
    }

    public static boolean isValid(String value) {
        return value != null && EMAIL_PATTERN.matcher(value.trim()).matches();
    }
}
