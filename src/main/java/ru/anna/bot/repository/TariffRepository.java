package ru.anna.bot.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.anna.bot.domain.TariffCode;
import ru.anna.bot.domain.TariffEntity;

public interface TariffRepository extends JpaRepository<TariffEntity, TariffCode> {

    List<TariffEntity> findAllByOrderBySortOrderAsc();
}
