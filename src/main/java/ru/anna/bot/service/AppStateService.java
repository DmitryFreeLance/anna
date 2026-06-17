package ru.anna.bot.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.anna.bot.domain.AppStateEntity;
import ru.anna.bot.repository.AppStateRepository;

@Service
public class AppStateService {

    private final AppStateRepository appStateRepository;

    public AppStateService(AppStateRepository appStateRepository) {
        this.appStateRepository = appStateRepository;
    }

    @Transactional(readOnly = true)
    public String get(String key, String defaultValue) {
        return appStateRepository.findById(key)
            .map(AppStateEntity::getStateValue)
            .orElse(defaultValue);
    }

    @Transactional(readOnly = true)
    public long getLong(String key, long defaultValue) {
        try {
            return Long.parseLong(get(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException exception) {
            return defaultValue;
        }
    }

    @Transactional
    public void put(String key, String value) {
        appStateRepository.save(new AppStateEntity(key, value));
    }

    @Transactional
    public void putLong(String key, long value) {
        put(key, String.valueOf(value));
    }
}
