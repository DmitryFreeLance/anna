package ru.anna.bot.service;

import jakarta.annotation.PostConstruct;
import java.util.EnumMap;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.anna.bot.domain.TariffCode;
import ru.anna.bot.domain.TariffEntity;
import ru.anna.bot.repository.TariffRepository;

@Component
public class TariffBootstrapper {

    private final TariffRepository tariffRepository;

    public TariffBootstrapper(TariffRepository tariffRepository) {
        this.tariffRepository = tariffRepository;
    }

    @PostConstruct
    @Transactional
    public void init() {
        Map<TariffCode, TariffEntity> defaults = new EnumMap<>(TariffCode.class);
        defaults.put(TariffCode.MONTH_1, tariff(TariffCode.MONTH_1, "1 месяц", "1 месяц", "Подписка на 1 месяц", 30, 99_900, 1));
        defaults.put(TariffCode.MONTH_3, tariff(TariffCode.MONTH_3, "3 месяца", "3 месяца", "Подписка на 3 месяца", 90, 250_000, 2));
        defaults.put(TariffCode.MONTH_6, tariff(TariffCode.MONTH_6, "6 месяцев", "6 месяцев", "Подписка на 6 месяцев", 180, 500_000, 3));

        for (Map.Entry<TariffCode, TariffEntity> entry : defaults.entrySet()) {
            tariffRepository.findById(entry.getKey()).orElseGet(() -> tariffRepository.save(entry.getValue()));
        }
    }

    private TariffEntity tariff(
        TariffCode code,
        String title,
        String buttonText,
        String description,
        int durationDays,
        long priceMinor,
        int sortOrder
    ) {
        TariffEntity tariff = new TariffEntity();
        tariff.setCode(code);
        tariff.setTitle(title);
        tariff.setButtonText(buttonText);
        tariff.setDescription(description);
        tariff.setDurationDays(durationDays);
        tariff.setPriceMinor(priceMinor);
        tariff.setSortOrder(sortOrder);
        return tariff;
    }
}
