package ru.anna.bot.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "app_state")
public class AppStateEntity {

    @Id
    @Column(nullable = false, length = 120)
    private String stateKey;

    @Column(nullable = false, length = 1_000)
    private String stateValue;

    public AppStateEntity() {
    }

    public AppStateEntity(String stateKey, String stateValue) {
        this.stateKey = stateKey;
        this.stateValue = stateValue;
    }

    public String getStateKey() {
        return stateKey;
    }

    public void setStateKey(String stateKey) {
        this.stateKey = stateKey;
    }

    public String getStateValue() {
        return stateValue;
    }

    public void setStateValue(String stateValue) {
        this.stateValue = stateValue;
    }
}
