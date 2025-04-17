package ru.exyon.telegrambot.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@Data
public class BotConfig {
    @Value("${bot.name}")
    String botName;
    @Value("${bot.token}")
    String token;
    @Value("${bot.start-step}")
    String startStep;
    @Value("${bot.init-step}")
    String initStep;
    @Value("${bot.end-marker}")
    String endMarker;
    @Value("${bot.threads.core-pool-size}")
    int threadsCorePoolSize;
    @Value("${bot.threads.queue-capacity}")
    int threadsQueueCapacity;
    @Value("${bot.threads.max-pool-size}")
    int threadsMaxPoolSize;
}
