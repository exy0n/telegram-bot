package ru.exyon.telegrambot.config;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@AllArgsConstructor
public class ExecutorConfig {
    private BotConfig botConfig;

    @Bean(name = "executorService")
    public ThreadPoolTaskExecutor executorService() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(botConfig.getThreadsCorePoolSize());   // Минимальное количество потоков
        executor.setQueueCapacity(botConfig.getThreadsQueueCapacity()); // Максимальное количество задач в очереди
        executor.setMaxPoolSize(botConfig.getThreadsMaxPoolSize());     // Максимальное при заполнении очереди
        executor.setThreadNamePrefix("ExecutorService-");                  // Префикс для имен потоков
        executor.initialize();
        return executor;
    }
}
