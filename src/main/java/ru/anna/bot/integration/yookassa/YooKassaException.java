package ru.anna.bot.integration.yookassa;

public class YooKassaException extends RuntimeException {

    public YooKassaException(String message) {
        super(message);
    }

    public YooKassaException(String message, Throwable cause) {
        super(message, cause);
    }
}
