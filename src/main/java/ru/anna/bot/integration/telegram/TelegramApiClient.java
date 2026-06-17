package ru.anna.bot.integration.telegram;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import ru.anna.bot.config.AppProperties;
import ru.anna.bot.integration.telegram.model.InlineKeyboardMarkup;
import ru.anna.bot.integration.telegram.model.ReplyKeyboardMarkup;
import ru.anna.bot.integration.telegram.model.TelegramApiResponse;
import ru.anna.bot.integration.telegram.model.TelegramInviteLink;
import ru.anna.bot.integration.telegram.model.TelegramUpdate;

@Component
public class TelegramApiClient {

    private static final Logger log = LoggerFactory.getLogger(TelegramApiClient.class);

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public TelegramApiClient(AppProperties properties, ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder()
            .baseUrl("https://api.telegram.org/bot" + properties.getTelegram().getBotToken())
            .build();
    }

    public List<TelegramUpdate> getUpdates(long offset, int timeoutSeconds) {
        Map<String, Object> request = Map.of(
            "offset", offset,
            "timeout", timeoutSeconds,
            "allowed_updates", List.of("message", "callback_query")
        );
        TelegramApiResponse<List<TelegramUpdate>> response = postJson(
            "/getUpdates",
            request,
            new ParameterizedTypeReference<>() {
            }
        );
        return response.result();
    }

    public void sendMessage(Long chatId, String text, Object replyMarkup) {
        Map<String, Object> request = new java.util.LinkedHashMap<>();
        request.put("chat_id", chatId);
        request.put("text", text);
        request.put("reply_markup", replyMarkup);
        request.put("disable_web_page_preview", true);
        postJson("/sendMessage", request, new ParameterizedTypeReference<TelegramApiResponse<Map<String, Object>>>() {
        });
    }

    public void sendPhoto(Long chatId, String photoPathOrUrl, String caption, Object replyMarkup) {
        File file = new File(photoPathOrUrl);
        if (file.exists() && file.isFile()) {
            MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
            form.add("chat_id", String.valueOf(chatId));
            form.add("caption", caption);
            if (replyMarkup != null) {
                form.add("reply_markup", toJson(replyMarkup));
            }
            form.add("photo", new FileSystemResource(file));
            postMultipart("/sendPhoto", form);
            return;
        }

        Map<String, Object> request = new java.util.LinkedHashMap<>();
        request.put("chat_id", chatId);
        request.put("caption", caption);
        request.put("photo", photoPathOrUrl);
        request.put("reply_markup", replyMarkup);
        postJson("/sendPhoto", request, new ParameterizedTypeReference<TelegramApiResponse<Map<String, Object>>>() {
        });
    }

    public void answerCallbackQuery(String callbackQueryId, String text) {
        Map<String, Object> request = new java.util.LinkedHashMap<>();
        request.put("callback_query_id", callbackQueryId);
        if (text != null && !text.isBlank()) {
            request.put("text", text);
        }
        postJson("/answerCallbackQuery", request, new ParameterizedTypeReference<TelegramApiResponse<Boolean>>() {
        });
    }

    public String createSingleUseInviteLink(Long chatId, String name, Instant expiresAt) {
        Map<String, Object> request = Map.of(
            "chat_id", chatId,
            "name", name,
            "expire_date", expiresAt.getEpochSecond(),
            "member_limit", 1
        );
        TelegramApiResponse<TelegramInviteLink> response = postJson(
            "/createChatInviteLink",
            request,
            new ParameterizedTypeReference<>() {
            }
        );
        return response.result().inviteLink();
    }

    public void banChatMember(Long chatId, Long userId) {
        Map<String, Object> request = Map.of("chat_id", chatId, "user_id", userId);
        postJson("/banChatMember", request, new ParameterizedTypeReference<TelegramApiResponse<Boolean>>() {
        });
    }

    public void unbanChatMember(Long chatId, Long userId) {
        Map<String, Object> request = Map.of("chat_id", chatId, "user_id", userId, "only_if_banned", true);
        postJson("/unbanChatMember", request, new ParameterizedTypeReference<TelegramApiResponse<Boolean>>() {
        });
    }

    public void copyMessage(Long toChatId, Long fromChatId, Integer messageId) {
        Map<String, Object> request = Map.of(
            "chat_id", toChatId,
            "from_chat_id", fromChatId,
            "message_id", messageId
        );
        postJson("/copyMessage", request, new ParameterizedTypeReference<TelegramApiResponse<Map<String, Object>>>() {
        });
    }

    public ReplyKeyboardMarkup defaultReplyKeyboard() {
        return new ReplyKeyboardMarkup(
            List.of(
                List.of(new ru.anna.bot.integration.telegram.model.ReplyKeyboardButton("📚 Перейти к гайдам и тарифу")),
                List.of(
                    new ru.anna.bot.integration.telegram.model.ReplyKeyboardButton("💎 Подписка"),
                    new ru.anna.bot.integration.telegram.model.ReplyKeyboardButton("💬 Обратная связь")
                )
            ),
            true,
            true
        );
    }

    public InlineKeyboardMarkup singleButton(String text, String url) {
        return new InlineKeyboardMarkup(List.of(List.of(ru.anna.bot.integration.telegram.model.InlineKeyboardButton.url(text, url))));
    }

    private void postMultipart(String uri, MultiValueMap<String, Object> form) {
        TelegramApiResponse<Map<String, Object>> response = restClient.post()
            .uri(uri)
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(form)
            .retrieve()
            .body(new ParameterizedTypeReference<>() {
            });
        if (response == null || !response.ok()) {
            throw new TelegramException("Telegram multipart request failed for " + uri);
        }
    }

    private <T> TelegramApiResponse<T> postJson(String uri, Object body, ParameterizedTypeReference<TelegramApiResponse<T>> responseType) {
        try {
            TelegramApiResponse<T> response = restClient.post()
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(responseType);
            if (response == null || !response.ok()) {
                throw new TelegramException("Telegram request failed for " + uri + ": " + (response == null ? "empty response" : response.description()));
            }
            return response;
        } catch (Exception exception) {
            log.error("Telegram API error on {}", uri, exception);
            throw new TelegramException("Telegram API error on " + uri, exception);
        }
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new TelegramException("Failed to serialize Telegram reply markup", exception);
        }
    }
}
