package ru.anna.bot.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "payments")
public class PaymentEntity extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String internalPaymentId;

    @Column(unique = true, length = 100)
    private String yookassaPaymentId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private TelegramUserEntity user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TariffCode tariffCode;

    @Column(nullable = false, length = 255)
    private String tariffTitle;

    @Column(nullable = false)
    private int durationDays;

    @Column(nullable = false)
    private long amountMinor;

    @Column(nullable = false, length = 3)
    private String currency = "RUB";

    @Column(nullable = false, length = 255)
    private String receiptEmail;

    @Column(length = 2_048)
    private String confirmationUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status = PaymentStatus.PENDING;

    private Instant paidAt;

    private Instant subscriptionAppliedAt;

    public Long getId() {
        return id;
    }

    public String getInternalPaymentId() {
        return internalPaymentId;
    }

    public void setInternalPaymentId(String internalPaymentId) {
        this.internalPaymentId = internalPaymentId;
    }

    public String getYookassaPaymentId() {
        return yookassaPaymentId;
    }

    public void setYookassaPaymentId(String yookassaPaymentId) {
        this.yookassaPaymentId = yookassaPaymentId;
    }

    public TelegramUserEntity getUser() {
        return user;
    }

    public void setUser(TelegramUserEntity user) {
        this.user = user;
    }

    public TariffCode getTariffCode() {
        return tariffCode;
    }

    public void setTariffCode(TariffCode tariffCode) {
        this.tariffCode = tariffCode;
    }

    public String getTariffTitle() {
        return tariffTitle;
    }

    public void setTariffTitle(String tariffTitle) {
        this.tariffTitle = tariffTitle;
    }

    public int getDurationDays() {
        return durationDays;
    }

    public void setDurationDays(int durationDays) {
        this.durationDays = durationDays;
    }

    public long getAmountMinor() {
        return amountMinor;
    }

    public void setAmountMinor(long amountMinor) {
        this.amountMinor = amountMinor;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getReceiptEmail() {
        return receiptEmail;
    }

    public void setReceiptEmail(String receiptEmail) {
        this.receiptEmail = receiptEmail;
    }

    public String getConfirmationUrl() {
        return confirmationUrl;
    }

    public void setConfirmationUrl(String confirmationUrl) {
        this.confirmationUrl = confirmationUrl;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public Instant getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(Instant paidAt) {
        this.paidAt = paidAt;
    }

    public Instant getSubscriptionAppliedAt() {
        return subscriptionAppliedAt;
    }

    public void setSubscriptionAppliedAt(Instant subscriptionAppliedAt) {
        this.subscriptionAppliedAt = subscriptionAppliedAt;
    }
}
