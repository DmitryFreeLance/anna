package ru.anna.bot.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tariffs")
public class TariffEntity extends AuditableEntity {

    @Id
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TariffCode code;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false, length = 120)
    private String buttonText;

    @Column(nullable = false, length = 255)
    private String description;

    @Column(nullable = false)
    private int durationDays;

    @Column(nullable = false)
    private long priceMinor;

    @Column(nullable = false)
    private int sortOrder;

    public TariffCode getCode() {
        return code;
    }

    public void setCode(TariffCode code) {
        this.code = code;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getButtonText() {
        return buttonText;
    }

    public void setButtonText(String buttonText) {
        this.buttonText = buttonText;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getDurationDays() {
        return durationDays;
    }

    public void setDurationDays(int durationDays) {
        this.durationDays = durationDays;
    }

    public long getPriceMinor() {
        return priceMinor;
    }

    public void setPriceMinor(long priceMinor) {
        this.priceMinor = priceMinor;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }
}
