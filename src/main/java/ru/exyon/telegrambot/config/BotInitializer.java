package ru.exyon.telegrambot.config;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ru.exyon.telegrambot.services.TelegramBotService;

@Component
@AllArgsConstructor
@Slf4j
public class BotInitializer {
    private final TelegramBotService telegramBotService;

    @EventListener({ContextRefreshedEvent.class})
    public void init()throws TelegramApiException {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        try{
            telegramBotsApi.registerBot(telegramBotService);
        } catch (TelegramApiException e){
            log.error("Cant initialize bot");
            e.printStackTrace();
        }
    }
}
