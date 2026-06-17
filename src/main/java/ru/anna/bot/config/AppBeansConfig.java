package ru.anna.bot.config;

import java.time.Clock;
import java.time.ZoneId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppBeansConfig {

    @Bean
    public ZoneId appZoneId(AppProperties properties) {
        return ZoneId.of(properties.getZoneId());
    }

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
