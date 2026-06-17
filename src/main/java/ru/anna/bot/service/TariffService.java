package ru.anna.bot.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.anna.bot.domain.TariffCode;
import ru.anna.bot.domain.TariffEntity;
import ru.anna.bot.repository.TariffRepository;

@Service
public class TariffService {

    private final TariffRepository tariffRepository;

    public TariffService(TariffRepository tariffRepository) {
        this.tariffRepository = tariffRepository;
    }

    @Transactional(readOnly = true)
    public List<TariffEntity> findAllOrdered() {
        return tariffRepository.findAllByOrderBySortOrderAsc();
    }

    @Transactional(readOnly = true)
    public TariffEntity getRequired(TariffCode code) {
        return tariffRepository.findById(code)
            .orElseThrow(() -> new IllegalArgumentException("Tariff not found: " + code));
    }

    @Transactional
    public TariffEntity updatePrice(TariffCode code, long priceMinor) {
        TariffEntity tariff = getRequired(code);
        tariff.setPriceMinor(priceMinor);
        return tariffRepository.save(tariff);
    }
}
