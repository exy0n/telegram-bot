package ru.exyon.telegrambot.services;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.exyon.telegrambot.config.BotConfig;
import ru.exyon.telegrambot.core.ExcludeFromJacocoGeneratedReport;
import ru.exyon.telegrambot.models.Dialog;
import ru.exyon.telegrambot.models.Message;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class TelegramBotService extends TelegramLongPollingBot {
    private final BotConfig botConfig;
    @Getter
    private static final ConcurrentHashMap<Long, Dialog> activeDialogs = new ConcurrentHashMap<>();
    @Getter
    private final List<MessageService> messageServices;
    private final RedirectService redirectService;
    private final ValidateMessageService validateMessageService;
    private ThreadPoolTaskExecutor executorService;

    public TelegramBotService(BotConfig botConfig, List<MessageService> messageServices, RedirectService redirectService,
                              ValidateMessageService validateMessageService, ThreadPoolTaskExecutor executorService) {
        super(botConfig.getToken());
        this.botConfig = botConfig;
        this.messageServices = messageServices;
        this.redirectService = redirectService;
        this.validateMessageService = validateMessageService;
        this.executorService = executorService;
    }

    public void setExecutorService(ThreadPoolTaskExecutor executorService) {
        this.executorService = executorService;
    }

    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }

    @Override
    @ExcludeFromJacocoGeneratedReport
    public void onUpdateReceived(Update update) {
        runAsync(update);
    }

    public CompletableFuture<Void> runAsync(Update update){
        return CompletableFuture.runAsync(() -> processMessage(new Message(update)), executorService);
    }

    /**
     * Содержит основной алгоритм обработку входящего сообщения
     * 1. Делегирует проверку потребности в редиректе в начало диалога сервису redirectService
     * 2. Делегирует проверку введенных данных и полученных команд сервису validateMessageService
     * 3. Делегирует дальнейшую обработку сервису, ответственному за обработку конкретного типа сообщения
     *
     * @param message входящее сообщение
     */
    public void processMessage(Message message) {
        Dialog dialog = activeDialogs.getOrDefault(message.getChatId(), new Dialog());

        if (redirectService.isRedirectNeeded(message, dialog, this::processMessage)) return;

        if (!validateMessageService.isValid(message, dialog, this::buildAndSendMessage)) return;

        Optional<MessageService> messageServiceOptional = getMessageService(message);
        if (messageServiceOptional.isEmpty()) return;

        messageServiceOptional.get().processMessage(message, dialog, this::buildAndSendMessage);
    }

    private Optional<MessageService> getMessageService(Message message) {
        Optional<MessageService> messageServiceOptional = messageServices.stream()
                .filter(service -> service.getMessageType() == message.getType()).findFirst();
        if (messageServiceOptional.isEmpty()) {
            log.error("Username: {}. Cant select message service by message: {}", message.getUsername(), message);
        }
        return messageServiceOptional;
    }

    public SendMessage buildAndSendMessage(Long chatId, String textToSend, List<InlineKeyboardButton> buttons) {
        return sendMessage(buildSendMessage(chatId, textToSend, buttons));
    }

    private SendMessage sendMessage(SendMessage sendMessage) {
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Cant send message to bot, message: {}", sendMessage);
            e.printStackTrace();
            return new SendMessage();
        }
        return sendMessage;
    }

    private SendMessage buildSendMessage(Long chatId, String textToSend, List<InlineKeyboardButton> buttons) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(textToSend);
        sendMessage.setParseMode("Markdown");

        if (buttons != null && !buttons.isEmpty()) {
            InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
            inlineKeyboardMarkup.setKeyboard(buttons.stream().map(List::of).toList());
            sendMessage.setReplyMarkup(inlineKeyboardMarkup);
        }
        return sendMessage;
    }
}
