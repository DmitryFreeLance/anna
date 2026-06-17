package ru.anna.bot.integration.telegram.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record ReplyKeyboardMarkup(
    List<List<ReplyKeyboardButton>> keyboard,
    @JsonProperty("resize_keyboard") boolean resizeKeyboard,
    @JsonProperty("is_persistent") boolean isPersistent
) {
}
