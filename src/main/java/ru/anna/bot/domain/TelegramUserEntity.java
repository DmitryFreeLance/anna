package ru.anna.bot.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "telegram_users")
public class TelegramUserEntity extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long telegramUserId;

    @Column(nullable = false)
    private Long privateChatId;

    @Column(length = 255)
    private String username;

    @Column(length = 255)
    private String firstName;

    @Column(length = 255)
    private String lastName;

    @Column(length = 255)
    private String email;

    @Column(nullable = false)
    private Instant lastSeenAt = Instant.now();

    private Instant subscriptionEndsAt;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private TariffCode subscriptionTariffCode;

    @Column(nullable = false)
    private boolean subscriptionManaged = false;

    private Instant reminderSentForEndsAt;

    private Instant expirationProcessedForEndsAt;

    private Instant lastSuccessfulPaymentAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ConversationState conversationState = ConversationState.NONE;

    @Column(length = 255)
    private String conversationPayload;

    public Long getId() {
        return id;
    }

    public Long getTelegramUserId() {
        return telegramUserId;
    }

    public void setTelegramUserId(Long telegramUserId) {
        this.telegramUserId = telegramUserId;
    }

    public Long getPrivateChatId() {
        return privateChatId;
    }

    public void setPrivateChatId(Long privateChatId) {
        this.privateChatId = privateChatId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Instant getLastSeenAt() {
        return lastSeenAt;
    }

    public void setLastSeenAt(Instant lastSeenAt) {
        this.lastSeenAt = lastSeenAt;
    }

    public Instant getSubscriptionEndsAt() {
        return subscriptionEndsAt;
    }

    public void setSubscriptionEndsAt(Instant subscriptionEndsAt) {
        this.subscriptionEndsAt = subscriptionEndsAt;
    }

    public TariffCode getSubscriptionTariffCode() {
        return subscriptionTariffCode;
    }

    public void setSubscriptionTariffCode(TariffCode subscriptionTariffCode) {
        this.subscriptionTariffCode = subscriptionTariffCode;
    }

    public boolean isSubscriptionManaged() {
        return subscriptionManaged;
    }

    public void setSubscriptionManaged(boolean subscriptionManaged) {
        this.subscriptionManaged = subscriptionManaged;
    }

    public Instant getReminderSentForEndsAt() {
        return reminderSentForEndsAt;
    }

    public void setReminderSentForEndsAt(Instant reminderSentForEndsAt) {
        this.reminderSentForEndsAt = reminderSentForEndsAt;
    }

    public Instant getExpirationProcessedForEndsAt() {
        return expirationProcessedForEndsAt;
    }

    public void setExpirationProcessedForEndsAt(Instant expirationProcessedForEndsAt) {
        this.expirationProcessedForEndsAt = expirationProcessedForEndsAt;
    }

    public Instant getLastSuccessfulPaymentAt() {
        return lastSuccessfulPaymentAt;
    }

    public void setLastSuccessfulPaymentAt(Instant lastSuccessfulPaymentAt) {
        this.lastSuccessfulPaymentAt = lastSuccessfulPaymentAt;
    }

    public ConversationState getConversationState() {
        return conversationState;
    }

    public void setConversationState(ConversationState conversationState) {
        this.conversationState = conversationState;
    }

    public String getConversationPayload() {
        return conversationPayload;
    }

    public void setConversationPayload(String conversationPayload) {
        this.conversationPayload = conversationPayload;
    }
}
