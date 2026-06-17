package ru.anna.bot.integration.telegram;

public class TelegramException extends RuntimeException {

    public TelegramException(String message) {
        super(message);
    }

    public TelegramException(String message, Throwable cause) {
        super(message, cause);
    }
}
