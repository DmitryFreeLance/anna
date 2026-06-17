package ru.anna.bot.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public class AppProperties {

    @NotBlank
    private String zoneId = "Europe/Moscow";

    private final Telegram telegram = new Telegram();
    private final YooKassa yookassa = new YooKassa();

    public String getZoneId() {
        return zoneId;
    }

    public void setZoneId(String zoneId) {
        this.zoneId = zoneId;
    }

    public Telegram getTelegram() {
        return telegram;
    }

    public YooKassa getYookassa() {
        return yookassa;
    }

    public static class Telegram {

        @NotBlank
        private String botToken = "";

        @NotBlank
        private String botUsername = "";

        @NotNull
        private Long privateChatId = 0L;

        @NotBlank
        private String supportUsername = "AreninaAnna";

        @NotBlank
        private String startPhotoPath = "assets/1.jpg";

        private List<Long> adminIds = new ArrayList<>();

        private int pollTimeoutSeconds = 50;
        private long pollDelayMs = 1_500L;
        private int inviteLinkExpireHours = 72;

        public String getBotToken() {
            return botToken;
        }

        public void setBotToken(String botToken) {
            this.botToken = botToken;
        }

        public String getBotUsername() {
            return botUsername;
        }

        public void setBotUsername(String botUsername) {
            this.botUsername = botUsername;
        }

        public Long getPrivateChatId() {
            return privateChatId;
        }

        public void setPrivateChatId(Long privateChatId) {
            this.privateChatId = privateChatId;
        }

        public String getSupportUsername() {
            return supportUsername;
        }

        public void setSupportUsername(String supportUsername) {
            this.supportUsername = supportUsername;
        }

        public String getStartPhotoPath() {
            return startPhotoPath;
        }

        public void setStartPhotoPath(String startPhotoPath) {
            this.startPhotoPath = startPhotoPath;
        }

        public List<Long> getAdminIds() {
            return adminIds;
        }

        public void setAdminIds(List<Long> adminIds) {
            this.adminIds = adminIds;
        }

        public int getPollTimeoutSeconds() {
            return pollTimeoutSeconds;
        }

        public void setPollTimeoutSeconds(int pollTimeoutSeconds) {
            this.pollTimeoutSeconds = pollTimeoutSeconds;
        }

        public long getPollDelayMs() {
            return pollDelayMs;
        }

        public void setPollDelayMs(long pollDelayMs) {
            this.pollDelayMs = pollDelayMs;
        }

        public int getInviteLinkExpireHours() {
            return inviteLinkExpireHours;
        }

        public void setInviteLinkExpireHours(int inviteLinkExpireHours) {
            this.inviteLinkExpireHours = inviteLinkExpireHours;
        }
    }

    public static class YooKassa {

        private boolean enabled = true;

        @NotBlank
        private String shopId = "";

        @NotBlank
        private String secretKey = "";

        @NotBlank
        private String returnUrl = "";

        @NotBlank
        private String webhookPath = "/api/webhooks/yookassa";

        private int vatCode = 1;
        private Integer taxSystemCode;

        @NotBlank
        private String paymentSubject = "service";

        @NotBlank
        private String paymentMode = "full_payment";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getShopId() {
            return shopId;
        }

        public void setShopId(String shopId) {
            this.shopId = shopId;
        }

        public String getSecretKey() {
            return secretKey;
        }

        public void setSecretKey(String secretKey) {
            this.secretKey = secretKey;
        }

        public String getReturnUrl() {
            return returnUrl;
        }

        public void setReturnUrl(String returnUrl) {
            this.returnUrl = returnUrl;
        }

        public String getWebhookPath() {
            return webhookPath;
        }

        public void setWebhookPath(String webhookPath) {
            this.webhookPath = webhookPath;
        }

        public int getVatCode() {
            return vatCode;
        }

        public void setVatCode(int vatCode) {
            this.vatCode = vatCode;
        }

        public Integer getTaxSystemCode() {
            return taxSystemCode;
        }

        public void setTaxSystemCode(Integer taxSystemCode) {
            this.taxSystemCode = taxSystemCode;
        }

        public String getPaymentSubject() {
            return paymentSubject;
        }

        public void setPaymentSubject(String paymentSubject) {
            this.paymentSubject = paymentSubject;
        }

        public String getPaymentMode() {
            return paymentMode;
        }

        public void setPaymentMode(String paymentMode) {
            this.paymentMode = paymentMode;
        }
    }
}
